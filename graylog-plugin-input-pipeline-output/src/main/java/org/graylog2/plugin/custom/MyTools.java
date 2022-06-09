package org.graylog2.plugin.custom;

import org.apache.commons.lang3.StringUtils;
import org.graylog2.plugin.custom.graphql.CGraphql;
import org.graylog2.plugin.custom.graphql.DevStatusBean;
import org.graylog2.plugin.custom.graphql.NetInterfaceBean;
import org.graylog2.plugin.custom.timeseriesdb.TS_ValueChildObj;
import org.graylog2.plugin.custom.timeseriesdb.TS_ValueObj;
import org.graylog2.plugin.custom.timeseriesdb.TS_ValuePortChildObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTools {
    private static final Logger LOG = LoggerFactory.getLogger(MyTools.class);
    //密钥key为16位、24位或32位，分别对应AES-128, AES-192和 AES-256。
    private static String strkey="hollysys_1234567";
    private static String striv="hollysys_1234567";
    private static String VERSION =null;
    //存放处理数据的队列
    public static LinkedBlockingQueue<TS_ValueObj> concurrentLinkedQueue = new LinkedBlockingQueue<TS_ValueObj>(1000);

    public static void sendSyslog(String strLog,String sourceip,String targetip){
        try {
            Map<String,String[]> ipList = CGraphql.getIpList();
            if (!ipList.isEmpty()){
                final String constType = "sourcelogdevicetype=";
                if(!strLog.contains(constType)){
                    String[] typeRole = ipList.get(sourceip);
                    if(typeRole != null) {
                        strLog = String.format("%s,%s%s", strLog, constType, typeRole[0]);
                    }
                }
            }
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dc = formatter.format(new Date());
            String onelog = "<30> "+dc+" graylog[1]:"+strLog+",source_ip:"+sourceip;
            //String inetip = InetAddress.getLocalHost().getHostAddress();
            InetSocketAddress addr = new InetSocketAddress(targetip,514);
            byte[] bs = onelog.getBytes();
            java.net.DatagramPacket udppacket = new java.net.DatagramPacket(bs,bs.length, addr);
            java.net.DatagramSocket socket = new java.net.DatagramSocket();
            socket.send(udppacket);
            socket.close();
        } catch (Exception  e) {
            e.printStackTrace();
        }
    }
    //转发日志
    public static void relayStatus(String onelog, List<InetSocketAddress> addrList){
        for(InetSocketAddress addr : addrList){
            byte[] bs = onelog.getBytes();
            java.net.DatagramPacket udppacket = new java.net.DatagramPacket(bs, bs.length, addr);
            try {
                java.net.DatagramSocket socket = new java.net.DatagramSocket();
                socket.send(udppacket);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Boolean setValue(String msg, String ip,String hostFlag, DevStatusBean devStatus){
        ArrayList<TS_ValueChildObj> ts_valueChildObjs = new ArrayList<>();
        ArrayList<TS_ValuePortChildObj> ts_valuePortChildObjs = new ArrayList<>();
        Integer mem_idle = 0;
        Integer hd_usage = 0;
        //解析流量
        if(hostFlag.equals("127.0.0.1")){
            int begin = msg.indexOf("netflow:");
            if(begin < 0){
                return false;
            }
            begin += "netflow:".length();
            int end = msg.indexOf(";,",begin);
            if(end < 0){
                return false;
            }
            String netflow = msg.substring(begin,end);
            if(netflow.isEmpty()){
                return false;
            }
            //获取磁盘使用率(%)和内存剩余空间(MB)
            //String pattern = ".*hdSpaceUsage:([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9]),memIdle:([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9]),.*";//小数
            String pattern = ".*hdSpaceUsage:(\\d+),memIdle:(\\d+),.*";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(msg);
            LOG.info("groupCount is:{}",m.groupCount());
            if (m.find() && m.groupCount() == 2) {
                LOG.info("group is:{},{}",m.group(1),m.group(2));
                try{
                    hd_usage = Integer.parseInt(m.group(1));
                    mem_idle = Integer.parseInt(m.group(2));
                    LOG.info("hd_usage is:{}, mem_idle is:{}",hd_usage,mem_idle);
                }catch (Exception e){
                    LOG.warn("float parse err:{}",e.getMessage());
                }
            }
            //enp2s0,172.21.34.16,6c:4b:90:9e:5a:80,yes,0.99,0.00
            String arr[] = netflow.split(";");
            for(String flow : arr){
                String flowArr[] = flow.split(",");
                if(flowArr.length == 6){
                    TS_ValueChildObj childObj = new TS_ValueChildObj();
                    if(flowArr[1].equals("")){
                        //ip为空则网卡未启用
                        childObj.f_NetIn = -1F;
                        childObj.f_NetOut = -1F;
                        childObj.MacName = flowArr[0];
                        childObj.IpAddress = flowArr[1];
                        childObj.MacAddress = flowArr[2];
                        childObj.NetIsEnable = 0;

                    }else{
                        childObj.MacName = flowArr[0];
                        childObj.IpAddress = flowArr[1];
                        childObj.MacAddress = flowArr[2];
                        if(flowArr[3].equals("yes")){
                            childObj.NetIsEnable = 1;
                        }else{
                            childObj.NetIsEnable = 0;
                        }
                        childObj.f_NetIn = flowArr[4];
                        childObj.f_NetOut = flowArr[5];
                        if(ip.equals(childObj.IpAddress)){
                            devStatus.setNetInput(Math.round(Float.parseFloat((String)childObj.f_NetIn)));
                            devStatus.setNetOutput(Math.round(Float.parseFloat((String)childObj.f_NetOut)));
                        }
                    }
                    ts_valueChildObjs.add(childObj);
                }
            }
        }else if(hostFlag.equals("0")){
            //switch
            ArrayList<NetInterfaceBean> interfaceList = devStatus.getInterfaceList();
            LOG.info("interface size is:{}",interfaceList.size());
            for(NetInterfaceBean netInterfaceBean: interfaceList){
                if(netInterfaceBean != null){
                    String oid = netInterfaceBean.getOid();
                    Long inflow = netInterfaceBean.getInFlow();
                    Long outflow = netInterfaceBean.getOutFlow();
                    Integer stat = netInterfaceBean.getStat();
                    LOG.debug("bean is:{},{},{},{}",oid,inflow,outflow,stat);
                    TS_ValuePortChildObj ts_valuePortChildObj = new TS_ValuePortChildObj(oid,inflow,outflow,stat);
                    ts_valuePortChildObjs.add(ts_valuePortChildObj);
                }
            }

        }else{
            //agent
            TS_ValueChildObj childObj = new TS_ValueChildObj();
            childObj.f_NetIn = -1F;
            childObj.f_NetOut = -1F;
            childObj.MacName = "";
            childObj.IpAddress = ip;
            childObj.MacAddress = "";
            ts_valueChildObjs.add(childObj);
        }

        TS_ValueObj valueObj = new TS_ValueObj(new Date(), devStatus.getCpuUsageRate(),devStatus.getRamUsageRate(),mem_idle,devStatus.getDiskIdle(), hd_usage, ts_valueChildObjs, hostFlag,ts_valuePortChildObjs);
        try {
            LOG.info("offer data to queue");
            concurrentLinkedQueue.offer(valueObj, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }
    /**
     * 一次性读取全部文件数据
     * @param strFilePath
     */
    public static String ReadFile(String strFilePath){
        try{
            InputStream is = new FileInputStream(strFilePath);
            int iAvail = is.available();
            byte[] bytes = new byte[iAvail];
            is.read(bytes);
            String context = new String(bytes);
            is.close();
            return context;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成密钥对象
     */
    private static SecretKey generateKey(byte[] key) throws Exception {
        // 根据指定的 RNG 算法, 创建安全随机数生成器
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        // 设置 密钥key的字节数组 作为安全随机数生成器的种子
        random.setSeed(key);

        // 创建 AES算法生成器
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        // 初始化算法生成器
        gen.init(128, random);

        // 生成 AES密钥对象, 也可以直接创建密钥对象: return new SecretKeySpec(key, ALGORITHM);
        return gen.generateKey();
    }

    /**
     * 数据加密: 明文 -> 密文
     */
    public static byte[] Encrypt(byte[] plainBytes) throws Exception {
        // 生成密钥对象
        SecretKey secKey = generateKey(strkey.getBytes());
        IvParameterSpec iv = new IvParameterSpec(striv.getBytes());
        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance("AES/CFB/NOPADDING");
        // 初始化密码器（加密模型）
        cipher.init(Cipher.ENCRYPT_MODE, secKey, iv);

        // 加密数据, 返回密文
        return cipher.doFinal(plainBytes);
    }

    /**
     * 数据解密: 密文 -> 明文
     */
    public static byte[] Decrypt(byte[] cipherBytes) throws Exception {
        // 生成密钥对象
        SecretKey secKey = generateKey(strkey.getBytes());
        IvParameterSpec iv = new IvParameterSpec(striv.getBytes());
        // 获取 AES 密码器
        Cipher cipher = Cipher.getInstance("AES/CFB/NOPADDING");
        // 初始化密码器（解密模型）
        cipher.init(Cipher.DECRYPT_MODE, secKey,iv);

        // 解密数据, 返回明文
        return cipher.doFinal(cipherBytes);
    }

    /**
     *  判断是否B、C、las版本
     *  目前只有B、C、las版本 和 A、isa版本
     * @return: boolean 是返回true，否则返回false
     * @author lishengcai
     * @date: 2022/5/31 13:55
     */
    public static boolean isVersionBCLas(){
        if(VERSION==null){
            VERSION=CGraphql.getPlatVersion();
        }
//        VERSION="smp-c";
        LOG.info("version is: {}",VERSION);
        if(StringUtils.containsIgnoreCase(VERSION,"smp-c") || StringUtils.containsIgnoreCase(VERSION,"smp-b")
                ||StringUtils.containsIgnoreCase(VERSION,"las") ){
            return true;
        }
        return false;
    }
}
