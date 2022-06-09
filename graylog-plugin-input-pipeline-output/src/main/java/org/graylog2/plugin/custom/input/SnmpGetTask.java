package org.graylog2.plugin.custom.input;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog2.plugin.custom.MyTools;
import org.graylog2.plugin.custom.graphql.AssetSnmpBean;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.graphql.DevStatusBean;
import org.graylog2.plugin.custom.graphql.NetInterfaceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.graylog2.plugin.custom.output.RelayOutput.GetAddressList;

public class SnmpGetTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpGetTask.class);
    private static String collectorId = null;
    private static Map<String, List<NetInterfaceBean>> m_netPortStat = new HashMap<>();
    static Long UINT32_MAXVALUE = 4294967296L;

    public SnmpGetTask(String collectorId) {
        this.collectorId = collectorId;
    }

    @Override
    public void run() {
        LOG.info("snmp get task collectorId:{} ............", collectorId);
        if (collectorId != null && collectorId != "") {
            List<AssetSnmpBean> snmpBeans = CGraphql.GraphqlGetSnmpParam(collectorId);
            if (snmpBeans.isEmpty()) {
                return;
            }

            try {
                TransportMapping transport = new DefaultUdpTransportMapping();
                transport.listen();
                Snmp snmp = new Snmp(transport);
                for (AssetSnmpBean snmpBean : snmpBeans) {
                    if (connectSnmp(snmp, snmpBean.getIp(), snmpBean.getSnmpCommunity(), snmpBean.getSnmpVersion())) {
                        getDevStatus(snmp, snmpBean);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public CommunityTarget getTarget(String ip, String community, String version) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        String strAddr = String.format("udp:%s/161", ip);
        Address targetAddress = GenericAddress.parse(strAddr);
        target.setAddress(targetAddress);
        // 通信不成功时的重试次数
        target.setRetries(1);
        // 超时时间
        target.setTimeout(8000);
        if (version.equals("SNMPv1")) {
            target.setVersion(SnmpConstants.version1);
        } else if (version.equals("SNMPv2C")) {
            target.setVersion(SnmpConstants.version2c);
        } else if (version.equals("SNMPv3")) {
            target.setVersion(SnmpConstants.version3);
        } else {
            LOG.info("no match snmp version:{}", version);
        }
        return target;
    }

    public Boolean hasChild(Snmp snmp, CommunityTarget target, String oid) {
        PDU pdu = new PDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));

        ResponseEvent respEvnt = null;
        try {
            respEvnt = snmp.getNext(pdu, target);
            PDU respone = respEvnt.getResponse();
            if (respone != null) {
                VariableBinding vb = respone.get(0);
                System.out.println("vb is:" + vb.getOid() + "   " + vb.getVariable());
                if (vb.getOid().startsWith(targetOID)) {
                    System.out.println("find child");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Integer getListSum(List<Long> varList) {
        Long sum = 0L;
        for (Long i : varList) {
            sum += i / 1000;
        }
        return sum.intValue();
    }

    public boolean getDevStatus(Snmp snmp, AssetSnmpBean snmpBean) {

        DevStatusBean devStat = new DevStatusBean(snmpBean.getIp(), -1, -1, -1);
        try {
            /**1.发送snmp get请求获取设备状态**/
            CommunityTarget target = getTarget(snmpBean.getIp(), snmpBean.getSnmpCommunity(), snmpBean.getSnmpVersion());
            /**获取cpu使用率**/
            if (snmpBean.getSnmpPara() == null) {
                LOG.info("get {} snmpParam is null", snmpBean.getIp());
                return false;
            }
            String stroid = snmpBean.getSnmpPara().getCpuUsageRateOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setCpuUsageRate(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setCpuUsageRate(sum.intValue());
                }
            }
            /**内存**/
            stroid = snmpBean.getSnmpPara().getRamIdleOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setRamIdle(var.toInt());
                }
            } else {


                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setRamIdle(sum.intValue());
                }
            }

            stroid = snmpBean.getSnmpPara().getRamSizeOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setRamSize(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setRamSize(sum.intValue());
                }
            }

            stroid = snmpBean.getSnmpPara().getRamUsageOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setRamUsage(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setRamUsage(sum.intValue());
                }
            }

            /**内存使用率**/
            stroid = snmpBean.getSnmpPara().getRamUsageRateOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setRamUsageRate(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setRamUsageRate(sum.intValue());
                }
            }
            /**b/c/las新版新增:当没有内存使用率时尝试通过已知计算未知**/
            if (MyTools.isVersionBCLas() && devStat.getRamUsageRate() < 0) {
                if (devStat.getRamIdle() > 0 && devStat.getRamUsage() > 0) {
                    Float rate = devStat.getRamUsage() * 1.0f / (devStat.getRamIdle() + devStat.getRamUsage());
                    devStat.setRamUsageRate(Math.round(rate * 100));
                } else if (devStat.getRamIdle() > 0 && devStat.getRamSize() > 0) {
                    Float rate = (devStat.getRamSize() - devStat.getRamIdle()) * 1.0f / devStat.getRamSize();
                    devStat.setRamUsageRate(Math.round(rate * 100));
                } else if (devStat.getRamSize() > 0 && devStat.getRamUsage() > 0) {
                    Float rate = devStat.getRamUsage() * 1.0f / devStat.getRamSize();
                    devStat.setRamUsageRate(Math.round(rate * 100));
                }
            }
            /**获取磁盘剩余空间**/
            stroid = snmpBean.getSnmpPara().getDiskIdleOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setDiskIdle(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setDiskIdle(sum.intValue());
                }
            }

            stroid = snmpBean.getSnmpPara().getDiskSizeOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setDiskSize(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setDiskSize(sum.intValue());
                }
            }

            stroid = snmpBean.getSnmpPara().getDiskUsageOid();
            if (!hasChild(snmp, target, stroid)) {
                Variable var = sendPDU(snmp, target, stroid);
                if (var != null) {
                    devStat.setDiskUsage(var.toInt());
                }
            } else {
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Long sum = 0L;
                    for (Map.Entry<String, Long> entry : varList.entrySet()) {
                        sum += entry.getValue();
                    }
                    devStat.setDiskUsage(sum.intValue());
                }
            }
            /**b/c/las新版本**/
            if (MyTools.isVersionBCLas()) {
                if (devStat.getDiskIdle() == -1) {
                    if (devStat.getDiskSize() > 0 && devStat.getDiskUsage() > 0) {
                        devStat.setDiskIdle(devStat.getDiskSize() - devStat.getDiskUsage());
                    }
                }

                //获取网络流量
                ArrayList<NetInterfaceBean> interfaceList = new ArrayList<>();
                String strStatOid = snmpBean.getSnmpPara().getStatusOid();
                if (strStatOid != null && !strStatOid.isEmpty()) {
                    int begin = strStatOid.indexOf(':');
                    if (begin > 0) {
                        int end = strStatOid.indexOf(':', begin + 1);
                        if (end > begin + 1) {
                            String maxValue = strStatOid.substring(begin + 1, end);
                            if (maxValue.equals("Counter")) {
                                devStat.setMax(UINT32_MAXVALUE);
                            } else if (maxValue.equals("Gauge")) {
                                devStat.setMax(Long.MAX_VALUE);
                            } else if (maxValue.equals("Average")) {
                                devStat.setMax(0L);
                            }

                            String interfaceIds = strStatOid.substring(end + 1);
                            LOG.info("interface max is:{},Ids is:{}", maxValue, interfaceIds);
                            String[] interfaceArray = interfaceIds.split(",");
                            for (String inId : interfaceArray) {
                                NetInterfaceBean netInterfaceBean = new NetInterfaceBean();
                                netInterfaceBean.setOid(inId);
                                interfaceList.add(netInterfaceBean);
                            }
                        }
                    }
                    /**获取接口状态**/
                    Map<String, Long> varList = snmpWalk(snmp, target, strStatOid.substring(0, begin));
                    if (interfaceList.size() > 0 && varList.size() > 0) {
                        ArrayList<NetInterfaceBean> tempList = new ArrayList<>();
                        for (NetInterfaceBean bean : interfaceList) {
                            for (Map.Entry<String, Long> entry : varList.entrySet()) {
                                if (entry.getKey().contains(bean.getOid())) {
                                    if (entry.getValue() == 1) {
                                        bean.setStat(1);//up
                                    } else {
                                        bean.setStat(2);//down
                                    }
                                    tempList.add(bean);
                                    break;
                                }
                            }
                        }
                        interfaceList = tempList;
                        LOG.info("match oid interfaceList size is:{}", interfaceList.size());
                    }

                }
                /**获取网络输入流量**/
                String strInOid = snmpBean.getSnmpPara().getNetInputOid();
                if (strInOid != null && !strInOid.isEmpty()) {
                    Map<String, Long> varList = snmpWalk(snmp, target, strInOid);
                    if (interfaceList.size() > 0 && varList.size() > 0) {
                        for (int i = 0; i < interfaceList.size(); i++) {
                            NetInterfaceBean bean = interfaceList.get(i);
                            for (Map.Entry<String, Long> entry : varList.entrySet()) {
                                if (entry.getKey().contains(bean.getOid())) {
                                    bean.setInFlow(entry.getValue());
                                    break;
                                }
                            }
                        }
                    }
                }
                //获取网络输出流量
                String outStroid = snmpBean.getSnmpPara().getNetOutputOid();
                if (outStroid != null && !outStroid.isEmpty()) {
                    Map<String, Long> varList = snmpWalk(snmp, target, outStroid);
                    if (interfaceList.size() > 0 && varList.size() > 0) {
                        for (int i = 0; i < interfaceList.size(); i++) {
                            NetInterfaceBean bean = interfaceList.get(i);
                            for (Map.Entry<String, Long> entry : varList.entrySet()) {
                                if (entry.getKey().contains(bean.getOid())) {
                                    bean.setOutFlow(entry.getValue());
                                    break;
                                }
                            }
                        }
                    }
                }

                devStat.setInterfaceList(interfaceList);

                devStat.setIp(snmpBean.getIp());
                devStat.setOnline("true");
                String strLog = JSON.toJSONString(devStat);
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String dc = formatter.format(new Date());
                String snmpKeyword = "snmp-status";
                String onelog = String.format("<30> %s graylog[1]::snmp:cpuUsageRate:%d,ramUsageRate:%d,%s:%s,source_ip:%s", dc, devStat.getCpuUsageRate(), devStat.getRamUsageRate(), snmpKeyword, strLog, snmpBean.getIp());
                LOG.info("snmp devstat strjson is:{}", onelog);
                MyTools.setValue(onelog, snmpBean.getIp(), "0", devStat);

                updateStat(devStat);
                //转发性能日志
                List<InetSocketAddress> addrList = GetAddressList();
                if (!addrList.isEmpty()) {
                    MyTools.relayStatus(onelog, addrList);
                }

            } else {
                /**A_ISA版本的获取网络输入输出流量**/
                //获取网络输入流量
                String strIn = "[";
                stroid = snmpBean.getSnmpPara().getNetInputOid();
                Map<String, Long> varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Integer netflow = getListSum((List<Long>) varList.values());
                    devStat.setNetInput(netflow);
                    devStat.setInPorts((List<Long>) varList.values());
                }
                //获取网络输出流量
                String strOut = "[";
                stroid = snmpBean.getSnmpPara().getNetOutputOid();
                varList = snmpWalk(snmp, target, stroid);
                if (varList.size() > 0) {
                    Integer netflow = getListSum((List<Long>) varList.values());
                    devStat.setNetOutput(netflow);
                    devStat.setOutPorts((List<Long>) varList.values());
                }

                //2.将设备状态作为syslog发送给安管平台
                ObjectMapper mapper = new ObjectMapper();
                String strjson = mapper.writeValueAsString(devStat);
                LOG.info("devstat strjson is:{}", strjson);
                MyTools.sendSyslog("snmp-status:" + strjson, snmpBean.getIp(), "127.0.0.1");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /**
     * b/c/las版本更新设备列表中的设备状态
     **/
    public static synchronized void updateStat(DevStatusBean bean) {
        if (bean.getRamUsageRate() < 0) {
            if (bean.getRamIdle() > 0 && bean.getRamUsage() > 0) {
                Float rate = bean.getRamUsage() * 1.0f / (bean.getRamIdle() + bean.getRamUsage());
                bean.setRamUsageRate(Math.round(rate * 100));
            } else if (bean.getRamIdle() > 0 && bean.getRamSize() > 0) {
                Float rate = (bean.getRamSize() - bean.getRamIdle()) * 1.0f / bean.getRamSize();
                bean.setRamUsageRate(Math.round(rate * 100));
            } else if (bean.getRamSize() > 0 && bean.getRamUsage() > 0) {
                Float rate = bean.getRamUsage() * 1.0f / bean.getRamSize();
                bean.setRamUsageRate(Math.round(rate * 100));
            }
        }
        if (bean.getMax() == null) {
            CGraphql.GraphqlUpdateDevStatus(bean);
        } else if (bean.getMax() == 0L) {
            Integer totalIn = 0;
            Integer totalOut = 0;
            for (NetInterfaceBean netbean : bean.getInterfaceList()) {
                Long in = netbean.getInFlow() / 1024;
                netbean.setInFlow(in);
                totalIn += in.intValue();
                Long out = netbean.getOutFlow() / 1024;
                netbean.setOutFlow(out);
                totalOut += out.intValue();
            }
            bean.setNetInput(totalIn);
            bean.setNetOutput(totalOut);
            CGraphql.GraphqlUpdateDevStatus(bean);
        } else {
            List<NetInterfaceBean> temp = new ArrayList<>();
            for (NetInterfaceBean net : bean.getInterfaceList()) {
                temp.add(new NetInterfaceBean(net));
            }
            LOG.info("------synchronized updateStat:{}", bean.getIp());
            if (m_netPortStat.containsKey(bean.getIp())) {
                if (bean.getInterfaceList().size() == m_netPortStat.get(bean.getIp()).size()) {
                    Integer totalIn = 0;
                    Integer totalOut = 0;
                    Integer scale = 60 * (1024 / 8);
                    LOG.info("foreach m_netPortStat : {}", JSON.toJSONString(m_netPortStat));
                    for (int i = 0; i < bean.getInterfaceList().size(); i++) {
                        NetInterfaceBean netnew = bean.getInterfaceList().get(i);
                        NetInterfaceBean netold = m_netPortStat.get(bean.getIp()).get(i);
                        Long in = 0L;
                        if (netnew.getInFlow() >= netold.getInFlow()) {
                            in = (netnew.getInFlow() - netold.getInFlow()) / scale;
                            netnew.setInFlow(in);
                        } else {
                            in = (bean.getMax() - netold.getInFlow() + netnew.getInFlow()) / scale;
                            netnew.setInFlow(in);
                        }
                        totalIn += in.intValue();
                        Long out = 0L;
                        if (netnew.getOutFlow() >= netold.getOutFlow()) {
                            out = (netnew.getOutFlow() - netold.getOutFlow()) / scale;
                            netnew.setOutFlow(out);
                        } else {
                            out = (bean.getMax() - netold.getOutFlow() + netnew.getOutFlow()) / scale;
                            netnew.setOutFlow(out);
                        }
                        totalOut += out.intValue();
                    }
                    bean.setNetInput(totalIn);
                    bean.setNetOutput(totalOut);
                }
            } else {
                for (NetInterfaceBean netbean : bean.getInterfaceList()) {
                    netbean.setInFlow(0L);
                    netbean.setOutFlow(0L);
                }
            }
            m_netPortStat.put(bean.getIp(), temp);
            LOG.info("m_netPortStat.put {}", JSON.toJSONString(m_netPortStat));
            CGraphql.GraphqlUpdateDevStatus(bean);
        }
    }

    public boolean connectSnmp(Snmp snmp, String ip, String community, String version) {
        try {
            CommunityTarget target = getTarget(ip, community, version);
            String stroid = "1.3.6.1.2.1.1.1.0";
            Variable var = sendPDU(snmp, target, stroid);
            if (var != null) {
                return true;
            }
            LOG.info("snmp connect {} oid 1.3.6.1.2.1.1.1.0 faild.---------------------------------------", ip);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Variable sendPDU(Snmp snmp, CommunityTarget target, String oid) throws IOException {
        if (oid == null || oid.length() == 0) {
            return null;
        }
        // 创建 PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        // 向Agent发送PDU，并接收Response
        ResponseEvent respEvnt = snmp.get(pdu, target);
        PDU response = respEvnt.getResponse();
        // 解析Response
        if (response != null) {
            Vector<VariableBinding> recVBs = (Vector<VariableBinding>) response.getVariableBindings();
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                LOG.info("OID-VALUE is:{},{}", recVB.getOid(), recVB.getVariable());
                if (Null.noSuchInstance.compareTo(recVB.getVariable()) == 0) {
                    continue;
                }
                return recVB.getVariable();
            }
        }
        return null;
    }

    public Variable getNextPDU(Snmp snmp, CommunityTarget target, String oid) throws IOException {
        // 创建 PDU
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        // 向Agent发送PDU，并接收Response
        ResponseEvent respEvnt = snmp.getNext(pdu, target);
        PDU response = respEvnt.getResponse();
        // 解析Response
        if (response != null) {
            Vector<VariableBinding> recVBs = (Vector<VariableBinding>) response.getVariableBindings();
            for (int i = 0; i < recVBs.size(); i++) {
                VariableBinding recVB = recVBs.elementAt(i);
                LOG.info("OID-VALUE next is:{},{}", recVB.getOid(), recVB.getVariable());
                if (Null.noSuchInstance.compareTo(recVB.getVariable()) == 0) {
                    continue;
                }
                return recVB.getVariable();
            }
        }
        return null;
    }

    public static Counter64 convertValue(Variable var) {
        Counter64 varRet = new Counter64();
        switch (var.getSyntax()) {
            case 2:  //Integer32
            case 65: //Counter32
            case 66: //Gauge32
            case 70: //Counter32
                varRet.setValue(var.toLong());
                break;
            case 4: //OctetString
                String tmp = var.toString();
                if (tmp.contains(".")) {
                    varRet.setValue(Float.valueOf(tmp).longValue());
                } else {
                    varRet.setValue(Long.valueOf(var.toString()).longValue());
                }
                break;
            default:
                varRet.setValue(-1);
        }
        return varRet;
    }

    public static Map<String, Long> snmpWalk(Snmp snmp, Target target, String oid) {
        Map<String, Long> pduList = new HashMap<>();
        if (oid == null || oid.length() == 0) {
            return pduList;
        }

        PDU pdu = new PDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));
        boolean finished = false;
        while (!finished) {
            try {
                ResponseEvent respEvent = snmp.getNext(pdu, target);
                PDU respone = respEvent.getResponse();
                if (respone == null) {
                    break;
                } else {
                    VariableBinding vb = respone.get(0);

                    LOG.info("vb is:{}={}", vb.getOid(), vb.getVariable());

                    if (Null.noSuchInstance.compareTo(vb.getVariable()) == 0) {
                        continue;
                    }
                    if (!vb.getOid().startsWith(targetOID)) {
                        finished = true;
                    }
                    if (!finished) {
                        LOG.info("vb is:{}={}", vb.getOid().toString(), vb.getVariable());
                        Counter64 flow = convertValue(vb.getVariable());
                        pduList.put(vb.getOid().toString(), flow.toLong());
                        //set up the variable binding for the next entry.
                        pdu.setRequestID(new Integer32(0));
                        pdu.set(0, vb);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pduList;
    }


}
