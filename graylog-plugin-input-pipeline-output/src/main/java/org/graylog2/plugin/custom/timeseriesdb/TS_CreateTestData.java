package org.graylog2.plugin.custom.timeseriesdb;

import org.graylog2.plugin.custom.MyTools;

import java.util.*;

/**
 * 创建测试数据
 */
public class TS_CreateTestData extends TimerTask {
    public static int t_super_num = 0;//情况1
    public static int t_super_127_0_0_1 = 0;//情况2
    public static int t_super_127_0_0_8 = 0;//情况3
    public static int t_super_172_21_33_99 = 0;//情况3
    public static int t_super_172_21_33_988 = 0;//情况3
    public static int t_super_172_21_33_100 = 0;//情况3

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        Random random = new Random();

        //端口进
        ArrayList<TS_ValuePortChildObj> l_port = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            int tmp = 0;
            if (i % 2 == 0)
                tmp = 1;
            TS_ValuePortChildObj port = new TS_ValuePortChildObj(String.valueOf(i), Float.valueOf(random.nextInt(1000)), Float.valueOf(random.nextInt(1000)), tmp);
            l_port.add(port);
        }

        Date i_TimeStamp = new Date();//时间戳

        String macAddress_1 = "14:AB:A6:32:20:9D";//网卡1
        String macAddress_2 = "23:49:B6:E4:2C:61";//网卡2
        String macAddress_3 = "3F:ED:7B:2A:A4:0D";//网卡3

        String macName_1 = "以太网";//网卡1 名称
        String macName_2 = "WLAN";//网卡2 名称
        String macName_3 = "办公网";//网卡3 名称

        ArrayList<TS_ValueChildObj> ts_valueChildObjs = new ArrayList<>();
        TS_ValueChildObj childObj = new TS_ValueChildObj();

        //一个主机多个网卡（不是服务器）
        try {
            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("127.0.0.2", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("127.0.0.3", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("127.0.0.4", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "0");
            ts_valueChildObjs.add(childObj);
            TS_ValueObj valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000),
                    ts_valueChildObjs, "2", l_port);
            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("192.168.16.2", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("192.168.16.3", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "0");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("192.168.16.4", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "2", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("172.21.33.1", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "0");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("172.21.33.3", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("172.21.33.5", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "2", l_port);
            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("172.21.33.100", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("172.21.33.102", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "0");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("172.21.33.103", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "2", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("192.18.18.1", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "0");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("192.18.18.2", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("192.18.18.3", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "2", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }


            //一个主机多个网卡（是服务器）
            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("127.0.0.5", random.nextInt(10000), random.nextInt(10000), macAddress_3, macName_3, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("127.0.0.6", random.nextInt(10000), random.nextInt(10000), macAddress_2, macName_2, "1");
            ts_valueChildObjs.add(childObj);
            childObj = new TS_ValueChildObj("127.0.0.7", random.nextInt(10000), random.nextInt(10000), macAddress_1, macName_1, "0");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "127.0.0.1", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }


            //一个IP多个端口（交换机）
            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("127.0.0.8", 0, 0, macAddress_1, macName_1, "0");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "0", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("172.21.33.99", 0, 0, macAddress_2, macName_2, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "0", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("172.21.33.988", 0, 0, macAddress_3, macName_3, "0");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "0", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }

            ts_valueChildObjs = new ArrayList<>();
            childObj = new TS_ValueChildObj();
            i_TimeStamp = new Date();//时间戳

            childObj = new TS_ValueChildObj("172.33.33.100", 0, 0, macAddress_1, macName_1, "1");
            ts_valueChildObjs.add(childObj);
            valueObj = new TS_ValueObj(i_TimeStamp, random.nextInt(99), random.nextInt(99), random.nextInt(10000), random.nextInt(99), random.nextInt(100000), ts_valueChildObjs, "0", l_port);

            if (!MyTools.concurrentLinkedQueue.offer(valueObj)) {
                System.out.println("队列已满，未插入！");
                return;
            }
        } catch (Exception ex) {
            System.out.println("put数据错误：" + ex.getMessage());
        }

        t_super_num = t_super_num + 15;
        t_super_127_0_0_1 = t_super_127_0_0_1 + 3;
        t_super_127_0_0_8 = t_super_127_0_0_8 + 1;
        t_super_172_21_33_99 = t_super_172_21_33_99 + 1;
        t_super_172_21_33_988 = t_super_172_21_33_988 + 1;
        t_super_172_21_33_100 = t_super_172_21_33_100 + 1;
        System.out.println("t_super【" + t_super_num + "】  " +
                "t_super_127_0_0_1【" + t_super_127_0_0_1 + "】  " +
                "t_super_127_0_0_8【" + t_super_127_0_0_8 + "】  " +
                "t_super_172_21_33_99【" + t_super_172_21_33_99 + "】  " +
                "t_super_172_21_33_988【" + t_super_172_21_33_988 + "】  " +
                "t_super_172_21_33_100【" + t_super_172_21_33_100 + "】  ");
    }
}
