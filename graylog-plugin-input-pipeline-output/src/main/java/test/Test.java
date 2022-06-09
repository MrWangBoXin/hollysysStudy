package test;

import org.graylog2.plugin.custom.graphql.AssetSnmpBean;
import org.graylog2.plugin.custom.graphql.NetInterfaceBean;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test{

    public static CommunityTarget getTarget(String ip,String version,String community){
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        String strAddr = String.format("udp:%s/161", ip);
        System.out.println("snmpget--------------------------------------------------{%s}"+strAddr);
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
        }
        return target;
    }

    public boolean connectSnmp(Snmp snmp,AssetSnmpBean snmpBean) {
        try {
            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(snmpBean.getSnmpCommunity()));
            String strAddr = String.format("udp:%s/161", snmpBean.getIp());
            String log = String.format("snmpget--------------------------------------------------{%s}",strAddr);
            System.out.println(log);
            Address targetAddress = GenericAddress.parse(strAddr);
            target.setAddress(targetAddress);
            // 通信不成功时的重试次数
            target.setRetries(1);
            // 超时时间
            target.setTimeout(8000);
            if (snmpBean.getSnmpVersion().equals("SNMPv1")) {
                target.setVersion(SnmpConstants.version1);
            } else if (snmpBean.getSnmpVersion().equals("SNMPv2c")) {
                target.setVersion(SnmpConstants.version2c);
            } else if (snmpBean.getSnmpVersion().equals("SNMPv3")) {
                target.setVersion(SnmpConstants.version3);
            }

            String stroid = "1.3.6.1.2.1.1.1.0";
            Variable var = sendPDU(snmp, target, stroid);
            if (var != null) {
                return true;
            }
            System.out.println("snmp connect oid 1.3.6.1.2.1.1.1.0 faild.---------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Variable sendPDU(Snmp snmp, CommunityTarget target, String oid) throws IOException {
        if(oid == null || oid.length() == 0){
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
                System.out.println("OID-VALUE is:"+ recVB.getOid()+ ", " +recVB.getVariable());
                if(Null.noSuchInstance.compareTo(recVB.getVariable()) == 0){
                    continue;
                }
                return recVB.getVariable();
            }
        }
        return null;
    }
    public boolean TestConnectSnmp(String ip , String version,String community) {
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            AssetSnmpBean snmpBean = new AssetSnmpBean();
            snmpBean.setIp(ip);
            snmpBean.setSnmpVersion(version);
            snmpBean.setSnmpCommunity(community);
            return connectSnmp(snmp,snmpBean);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Boolean hasChild(Snmp snmp,CommunityTarget target,String oid){
        PDU pdu = new PDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));

        ResponseEvent respEvnt = null;
        try {
            respEvnt = snmp.getNext(pdu, target);
            PDU respone = respEvnt.getResponse();
            if(respone != null){
                VariableBinding vb = respone.get(0);
                System.out.println("vb is:"+vb.getOid()+"   "+vb.getVariable());
                if(vb.getOid().startsWith(targetOID)){
                    System.out.println("find child");
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static List<Long> snmpWalk(Snmp snmp, Target target, String oid){
        List<Long> pduList = new ArrayList<>();
        if(oid == null || oid.length() == 0){
            return pduList;
        }

        PDU pdu = new PDU();
        OID targetOID = new OID(oid);
        pdu.add(new VariableBinding(targetOID));
        boolean finished = false;
        while(!finished){
            try {
                ResponseEvent respEvent = snmp.getNext(pdu,target);
                PDU respone = respEvent.getResponse();
                if(respone == null){
                    break;
                }else{
                    VariableBinding vb = respone.get(0);
                    System.out.println("vb is:"+vb.getOid()+"  , "+vb.getVariable());
                    if(Null.noSuchInstance.compareTo(vb.getVariable()) == 0){
                        continue;
                    }
                    if(!vb.getOid().startsWith(targetOID)){
                        finished = true;
                    }
                    if(!finished){
                        Counter64 flow = convertValue(vb.getVariable());
                        pduList.add(flow.toLong()/1000);
                        //set up the variable binding for the next entry.
                        pdu.setRequestID(new Integer32(0));
                        pdu.set(0,vb);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return pduList;
    }
    public static Counter64 convertValue(Variable var){
        Counter64 varRet = new Counter64();
        switch (var.getSyntax()){
            case 2:  //Integer32
            case 65: //Counter32
            case 66: //Gauge32
            case 70: //Counter32
                varRet.setValue(var.toLong());
                break;
            case 4: //OctetString
                String tmp = var.toString();
                if(tmp.contains(".")){
                    varRet.setValue(Float.valueOf(tmp).longValue());
                }else{
                    varRet.setValue(Long.valueOf(var.toString()).longValue());
                }
                break;
            default:
                varRet.setValue(-1);
        }
        return varRet;
    }
    public static void testSnmp(){
        String ip = "172.21.33.210";
        String version = "SNMPv1";
        String community = "public";
        Test test = new Test();
        test.TestConnectSnmp(ip,version,community);

        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            transport.listen();
            Snmp snmp = new Snmp(transport);
            CommunityTarget target = getTarget(ip,version,community);
            String oid = "1.3.6.1.4.1.19849.6.2.2.0";
            if(hasChild(snmp, target, oid )) {
                test.snmpWalk(snmp, target, oid);
            }else{
                Variable var = sendPDU(snmp,target,oid);
                if(var != null){
                    System.out.println(var.toInt());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static String getStamp(){
        String flag = "batchStamp:";
        String str = "agentWindows[1]:batchStamp:2021-06-09 05:04:08,baselinecheck:1,1,0,0;";
        Integer begin = str.indexOf(flag);
        if(begin > 0) {
            Integer end = str.indexOf(",", begin);
            String stamp = str.substring(begin+flag.length(),end);
            return stamp;
        }
        return null;
    }

    static String addDate(String utc,int hour){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = sdf.parse(utc);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR , hour);
            date = calendar.getTime();
            String strTime = sdf.format(date);
            return strTime;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return utc;
    }
    static void netflow(){
        String msg = "2021/09/24 14:32:58 root platform[32762]:cpuUsage:3%,memUsage:40%,freeHdSpace:8761811,netflow:enp2s0,172.21.34.16/24,6c:4b:90:9e:5a:80,0.00,0.06;enp3s0,,6c:4b:90:9e:5a:80,0.00,0.00;,source_ip:127.0.0.1";
        int begin = msg.indexOf("netflow:")+"netflow:".length();
        int end = msg.indexOf(";,",begin);
        String netflow = msg.substring(begin,end);
        System.out.println("netflow is:"+netflow);
        String arr[] = netflow.split(";");
        for(String flow : arr){
            String flowArr[] = flow.split(",");
            if(flowArr.length == 5){
                if(flowArr[1].equals("")){

                    System.out.println("ip is null");
                }else{

                }
            }
        }

    }
    static void regex(){
        String msg = "2021/10/19 19:36:42 root platform-self[26465]:cpuUsage:3%,memUsage:65%,freeHdSpace:1839943,hdSpaceUsage:5,memIdle:5437,netflow:p4p1,,b0:26:28:9d:79:60,no,0.00,0.00;p4p2,,b0:26:28:9d:79:61,no,0.00,0.00;p4p3,,b0:26:28:9d:79:62,no,0.00,0.00;p4p4,172.21.34.2,b0:26:28:9d:79:63,yes,101.25,105.70;,source_ip:172.21.34.2";
        String pattern = ".*hdSpaceUsage:(\\d+),memIdle:(\\d+),.*";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(msg);
        System.out.println("groupCount is:"+m.groupCount());
        if (m.find() && m.groupCount() == 2) {
            System.out.println("group is:"+ m.group(2));
        }
    }
    static void list(){
        ArrayList<NetInterfaceBean> list = new ArrayList<>();
        NetInterfaceBean bean1 = new NetInterfaceBean();
        bean1.setInFlow(1L);
        list.add(bean1);
        System.out.println("inflow1 is:"+list.get(0).getInFlow());
        NetInterfaceBean bean2 = new NetInterfaceBean();
        bean2.setInFlow(2L);
        list.add(bean2);
        System.out.println("inflow2 is:"+list.get(1).getInFlow());
        ArrayList<NetInterfaceBean> list1 = (ArrayList<NetInterfaceBean>)list.clone();

        list.get(0).setInFlow(12L);
        System.out.println("inflow3 is:"+list.get(0).getInFlow());
        System.out.println("inflow4 is:"+list1.get(0).getInFlow());

        NetInterfaceBean bean3 = list.get(1);
        System.out.println("inflow is:"+list.get(1).getInFlow());
        NetInterfaceBean bean4 = bean3;
        NetInterfaceBean bean5 = new NetInterfaceBean(bean3);
        try {
            NetInterfaceBean bean6 = (NetInterfaceBean)bean3.clone();
            System.out.println("inflow6 is:"+bean6.getInFlow());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        list.add(bean3);
        bean3.setInFlow(22L);
        System.out.println("inflow is:"+list.get(1).getInFlow());
        System.out.println("inflow is:"+bean4.getInFlow());
        System.out.println("inflow is:"+bean5.getInFlow());
        System.out.println("inflow is:"+list.get(2).getInFlow());

    }
    static void GetOid(){
        String strStatOid = "1.3.6.1.2.1.2.2.1.8:Counter:10001,10002,10003,10004,10005,10006,10007,10008,10009,10010,10011,10012,10013,10014,10015,10016,10017,10018,10019,10020,10021,10022,10023,10024";
        int begin = strStatOid.indexOf(':');
        if (begin > 0 ) {
            int end = strStatOid.indexOf(':',begin+1);
            System.out.println("b.. "+begin+"..."+end);
            if(end > begin + 1) {
                String maxValue = strStatOid.substring(begin+1,end);
                if(maxValue.equals("Counter")){

                }else if(maxValue.equals("Gauge")){

                }
                String interfaceIds = strStatOid.substring(end + 1);
                System.out.println(String.format("interface max is:%s,Ids is:%s", maxValue,interfaceIds));

            }
        }
    }
    public static void main(String[] args) {
        GetOid();
        Integer a = Integer.MAX_VALUE;
        long b = Long.MAX_VALUE;
        System.out.println("b... "+b);
        //list();
        //regex();

        //netflow();
        //testSnmp();
        //String stamp = getStamp();
       // if(!stamp.equals(null)) {
        //    System.out.println(stamp);
       // }
        String date = addDate("2021-06-21 12:12:09",8);
        System.out.println("Hello World. "+date);
    }
}

