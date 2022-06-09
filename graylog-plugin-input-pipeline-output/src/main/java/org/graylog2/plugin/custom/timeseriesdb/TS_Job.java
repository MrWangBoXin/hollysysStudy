package org.graylog2.plugin.custom.timeseriesdb;

import com.alibaba.fastjson.JSONObject;
import org.graylog2.plugin.custom.MyTools;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 获取队列数据处理任务
 */
public class TS_Job implements Job {
    private static final Logger LOG = LoggerFactory.getLogger(TS_Job.class);
    /**
     * Key：为每个设备的3个指标IP_cpu、IP_mem、IP_disk
     * Value：每个指标的发生报警时的时间戳
     */
    static Map<String, Date> l_Queue = new Hashtable<>();

    @Override
    public void execute(JobExecutionContext var1) throws JobExecutionException {
//      System.out.println("--------------告警队列 " + l_Queue.size() + "--------------");

        if (MyTools.concurrentLinkedQueue.isEmpty()) {
            System.out.println("--------------性能队列 is empty--------------");
            return;
        }

        /*
        从pg读取系统告警的阈值数据
         */
        TS_PgInit ts_pgInit = new TS_PgInit();

        try {
            ts_pgInit.GetSystemAlertConfig();
        } catch (Exception e) {
            System.out.println("从pg读取系统告警的阈值数据 错误:" + e.getMessage());
        }

        String strDBName = "sec";
        TS_DbInit ts_dbInit = new TS_DbInit();

        while (!MyTools.concurrentLinkedQueue.isEmpty()) {
            System.out.println("--------------队列大小：" + MyTools.concurrentLinkedQueue.size() + "--------------");
//            System.out.println("debug 1");

            try {
                TS_ValueObj ts_valueObj = MyTools.concurrentLinkedQueue.poll();
                if (ts_valueObj == null) {
                    LOG.info("获取队列数据为null");
                    return;
                }

//                //反序列化
//                JSONObject jsonObject = new JSONObject();
//                String strTmp = jsonObject.toJSONString(ts_valueObj);
//                System.out.println(strTmp);

                /**
                 * 情况1：
                 *    一个主机多个网卡（不是服务器）【测试127.0.0.2、127.0.0.3、127.0.0.4】
                 *    固定超级表 t_super，子表 ip，不增加动态端口TAG（端口号、值）
                 * 情况2：
                 *    一个主机多个网卡（是服务器）【测试127.0.0.5、127.0.0.6、127.0.0.7】
                 *    固定超级表 t_super_127_0_0_1，子表 ip，不增加动态端口TAG（端口号、值）
                 * 情况3：
                 *    一个IP多个端口（交换机等）【测试127.0.0.8】
                 *    固定超级表 t_super_特定IP，子表 ip，增加动态端口TAG（端口号、值）【每个IP对应端口数量不固定，上限48】
                 */

                ArrayList<TS_ValueChildObj> childObjs = ts_valueObj.ts_valueChildObjs;

                if (childObjs.size() == 0) {
                    LOG.info("子表TS_ValueChildObj为空");
                    return;
                }

                TS_ValueChildObj ts_valueChildObj = childObjs.get(0);

                // 默认创建固定 t_127_0_0_1 超级表
                String strSuperTableName = "";

                if (ts_valueObj.IsHost == "127.0.0.1") {
                    //情况2：一个主机多个网卡（服务器）
                    //固定超级表 t_127_0_0_1
                    strSuperTableName = "t_super_127_0_0_1";
                } else if (!ts_valueObj.IsHost.equals("127.0.0.1") && ts_valueObj.IsHost != "0") {
                    //情况1：一个主机多个网卡（不是服务器）
                    //固定超级表 t_super
                    strSuperTableName = "t_super";
                } else if (ts_valueObj.IsHost == "0") {
                    //情况3：一个IP多个端口（交换机等）(考虑一个交换机1个ip的情况）
                    //固定超级表 t_特定IP
                    strSuperTableName = "t_super_" + TS_Common.FormatIpAddress(ts_valueChildObj.IpAddress);
                }

                // 拆分主表数据
                float f_CPU_Percent = Float.parseFloat(ts_valueObj.f_CPU_Percent.toString());
                float f_Mem_Percent = Float.parseFloat(ts_valueObj.f_Mem_Percent.toString());
                float f_Mem_Surplus = Float.parseFloat(ts_valueObj.f_Mem_Surplus.toString());
                float f_Disk_Percent = Float.parseFloat(ts_valueObj.f_Disk_Percent.toString());
                float f_Disk_Surplus = Float.parseFloat(ts_valueObj.f_Disk_Surplus.toString());

                String strTableName = "";
                String MacAddress = "";
                String MacName = "";
                String NetIsEnable = "";
                float f_NetIn = 0f;
                float f_NetOut = 0f;

                //情况3：一个IP多个端口（交换机等）(考虑一个交换机1个ip的情况）
                if (ts_valueObj.IsHost == "0") {
                    try {
                        // Step 1：组合子表名
                        // ip地址 格式化为 t_ip 为表名
                        strTableName = "t_" + TS_Common.FormatIpAddress(ts_valueChildObj.IpAddress);

                        // Step 2：拆分 子数据
                        f_NetIn = Float.parseFloat(ts_valueChildObj.f_NetIn.toString());
                        f_NetOut = Float.parseFloat(ts_valueChildObj.f_NetOut.toString());
                        MacAddress = ts_valueChildObj.MacAddress.toString();
                        MacName = ts_valueChildObj.MacName.toString();
                        NetIsEnable = ts_valueChildObj.NetIsEnable.toString();

                        String strPortListSQL = ",";

                        ArrayList<TS_ValuePortChildObj> PortChildObjs = ts_valueObj.ts_valuePortChildObjs;
                        if (PortChildObjs.size() == 0) {
                            System.out.println("交换机端口数据为空，则不执行数据增加操作。");
                            return;
                        }

                        int i_Tmp = PortChildObjs.size();
                        for (int i = 1; i <= 48; i++) {
                            if (i < i_Tmp) {
                                TS_ValuePortChildObj data = PortChildObjs.get(i);
                                strPortListSQL += data.p_In + ", " + data.p_Out + ", " + data.p_Status + ",";
                            } else {
                                strPortListSQL += "-1, -1, -1,";
                            }
                        }
                        if (strPortListSQL.endsWith(",")) {
                            strPortListSQL = strPortListSQL.substring(0, strPortListSQL.length() - 1);
                        }

                        // 判断是否存在 strSuperTableName 超级表
                        if (!ts_dbInit.IsExistsSTable(strSuperTableName)) {
                            // Step 6：创建指定超级表下增加Tag
                            // 不存在则创建超级表

                            ArrayList<String> Tmp = new ArrayList<>();

                            for (int i = 1; i <= 48; i++) {
                                Tmp.add("c_" + i + "_In FLOAT ");
                                Tmp.add("c_" + i + "_Out FLOAT ");
                                Tmp.add("c_" + i + "_State INT ");
                            }

                            String[] strArrayTrue = (String[]) Tmp.toArray(new String[0]);

                            int i_Result = ts_dbInit.CreateTable(strSuperTableName, strArrayTrue);
                            if (i_Result == -1) {
                                System.out.println("动态创建超级表 " + strDBName + "." + strSuperTableName + " 错误");
                                return;
                            }
                        }

                        // Step 7：构建SQL语句
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                        String ts = formatter.format(ts_valueObj.i_TimeStamp);

                        String strSQL = "insert into " + strDBName + "." + strTableName +
                                " using " + strDBName + "." + strSuperTableName +
                                " tags ( " +
                                " '" + ts_valueChildObj.IpAddress + "', " +
                                " '" + ts_valueChildObj.MacAddress.toString() + "', " +
                                " '" + ts_valueChildObj.MacName.toString() + "' " +
                                " ) " +
                                " values " +
                                " (" +
                                " '" + ts + "', " +
                                f_CPU_Percent + ", " +
                                f_Mem_Percent + ", " +
                                f_Mem_Surplus + "," +
                                f_Disk_Percent + ", " +
                                f_Disk_Surplus + ", " +
                                f_NetIn + ", " +
                                f_NetOut + ", " +
                                ts_valueChildObj.NetIsEnable.toString() +
                                strPortListSQL +
                                " );";

                        LOG.info("will insert switch data {}", strTableName);
                        // Step 8：批量插入原始数据
                        int i_OptResult = ts_dbInit.AddData(strSQL);

                        //插入原始数据表失败
                        if (i_OptResult == -1) {
                            LOG.info("switch批量插入原始数据失败！");
                        }
                    } catch (Exception ex) {
                        System.out.println("switch其它情况错误：" + ex.getMessage());
                    }
                } else {
                    try {
                        // 判断是否存在 strSuperTableName 超级表
                        if (!ts_dbInit.IsExistsSTable(strSuperTableName)) {
                            // 不存在则创建超级表
                            int i_Result = ts_dbInit.CreateSTable(strSuperTableName);
                            if (i_Result == -1) {
                                System.out.println("创建超级表 " + strDBName + "." + strSuperTableName + " 错误");
                                return;
                            }
                        }

                        //其它情况
                        System.out.println("childObjs数量：" + childObjs.size());
                        for (TS_ValueChildObj childObj : childObjs) {
                            f_NetIn = 0f;
                            f_NetOut = 0f;
                            LOG.info("ip is:{}", childObj.IpAddress);
                            // Step 1：组合子表名
                            // ip地址 格式化为 表名
                            //if (childObj.IpAddress.isEmpty() || childObj.IpAddress.length() == 0) {
                            //    continue;
                            //}
                            strTableName = "t_" + childObj.MacAddress.toString().replace(":","_");

                            // Step 2：拆分 子数据
                            f_NetIn = Float.parseFloat(childObj.f_NetIn.toString());
                            f_NetOut = Float.parseFloat(childObj.f_NetOut.toString());
                            MacAddress = childObj.MacAddress.toString();
                            MacName = childObj.MacName.toString();
                            NetIsEnable = childObj.NetIsEnable.toString();

                            // Step 3：构建SQL语句
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                            String ts = formatter.format(ts_valueObj.i_TimeStamp);

                            String strSQL = "insert into " + strDBName + "." + strTableName +
                                    " using " + strDBName + "." + strSuperTableName +
                                    " tags ( " +
                                    " '" + childObj.MacAddress.toString() + "', " +
                                    " '" + childObj.MacName.toString() + "' " +
                                    " ) " +
                                    " values " +
                                    " (" +
                                    " '" + ts + "', " +
                                    " '" + childObj.IpAddress + "', " +
                                    f_CPU_Percent + ", " +
                                    f_Mem_Percent + ", " +
                                    f_Mem_Surplus + "," +
                                    f_Disk_Percent + "," +
                                    f_Disk_Surplus + ", " +
                                    f_NetIn + ", " +
                                    f_NetOut + ", " +
                                    childObj.NetIsEnable.toString() +
                                    " );";
                            LOG.info("will insert data {}", strSQL);
                            // Step 4：批量插入原始数据
                            int i_OptResult = ts_dbInit.AddData(strSQL);

                            //插入原始数据表失败
                            if (i_OptResult == -1) {
                                System.out.println("批量插入原始数据失败！");
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("其它情况错误：" + ex.getMessage());
                    }
                }


                // 如果是服务器（ip为127.0.0.1），则从PG获取规则，然后过滤数据，产生告警插入PG库
                if (strSuperTableName == "t_super_127_0_0_1") {
                    alertGenerate(ts_valueObj, ts_valueChildObj, strSuperTableName, f_CPU_Percent, f_Mem_Percent, f_Disk_Percent);
                }

            } catch (Exception ex) {
                LOG.warn("获取队列数据错误：{}", ex.getMessage());
            }
        }
    }

    /**
     * 如果是服务器（ip为127.0.0.1），则从PG获取规则，然后过滤数据，产生告警插入PG库
     *
     * @param ts_valueObj      父模板类
     * @param ts_valueChildObj 子模板类
     * @param strTableName     超级表的表名，固定为 t_super_127_0_0_1
     * @param f_CPU_Percent    cpu
     * @param f_Mem_Percent    内存
     * @param f_Disk_Percent   磁盘
     * @throws ParseException 空
     */
    private void alertGenerate(TS_ValueObj ts_valueObj, TS_ValueChildObj ts_valueChildObj, String strTableName, float f_CPU_Percent, float f_Mem_Percent, float f_Disk_Percent) throws ParseException, SQLException, FileNotFoundException {
        /*
        把告警写入pg
         */
        TS_PgInit ts_pgInit = new TS_PgInit();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        System.out.println("【" + strTableName + "】获取：" + formatter.format(ts_valueObj.i_TimeStamp) + " cpu值：" + ts_valueObj.f_CPU_Percent + " mem值：" + ts_valueObj.f_Mem_Percent + " disk值：" + ts_valueObj.f_Disk_Surplus);

        // 判断字典是否包含"表名_cpu"
        if (!l_Queue.containsKey(strTableName + "_cpu")) {
            l_Queue.put(strTableName + "_cpu", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }
        Date cpu_TS = l_Queue.get(strTableName + "_cpu");
        // 如果 cpu百分比 > cpu配置阈值
        if (f_CPU_Percent > TS_PgInit.cpulimit) {
            if (cpu_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {// 如果时间为空，则插入
                l_Queue.put(strTableName + "_cpu", ts_valueObj.i_TimeStamp);//插入l_Queue字典
            }
        } else {
            // 清空队列
            l_Queue.put(strTableName + "_cpu", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }

        if (!cpu_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {
            Date now = new Date();
            //当前时间 - 字典发生报警时的时间
            int seconds = TS_Common.getDistanceTime(ts_valueObj.i_TimeStamp, cpu_TS);
            //如果 时间差 > cpu持续时间
            if (seconds > TS_PgInit.cpucontinue) {
                System.out.println("==========" + strTableName + " cpu发生告警:   计算开始时间【" + formatter.format(ts_valueObj.i_TimeStamp) + "】 - 采集时间【" + cpu_TS + "】 " + seconds + "秒 ==========");

                int m = (seconds % 3600) / 60;
                int s = (seconds % 3600) % 60;
                String strContent = "";
                if (m == 0)
                    strContent = s + "秒";
                else
                    strContent = m + "分" + s + "秒";
                float m1 = (TS_PgInit.cpulimit % 3600) / 60;
                float s1 = (TS_PgInit.cpulimit % 3600) % 60;
                String strLimit = "";
                if (m1 == 0 || m1 > 0)
                    strLimit = s1 + "秒";
                else
                    strLimit = m1 + "分" + s1 + "秒";
                String str = String.valueOf(TS_PgInit.cpulimit);
                int idx = str.lastIndexOf(".");
                String strNum = str.substring(0, idx);
                int cpulimitTmp = Integer.valueOf(strNum);
                ts_pgInit.InsertSysAlert("Cpu", ts_valueObj.i_TimeStamp, "CPU使用率持续超过阈值（" + cpulimitTmp + "%），持续时间" + strContent);

                // 清空队列
                l_Queue.put(strTableName + "_cpu", TS_Common.StringToDate("2000-01-01 00:00:00"));
            }
        }

        // 判断字典是否包含"表名_mem"
        if (!l_Queue.containsKey(strTableName + "_mem")) {
            l_Queue.put(strTableName + "_mem", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }
        // 判断字典是否包含"表名_mem"
        Date mem_TS = l_Queue.get(strTableName + "_mem");
        // 如果 mem百分比 > mem配置阈值
        if (f_Mem_Percent > TS_PgInit.memlimit) {
            if (mem_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {// 如果时间为空，则插入
                l_Queue.put(strTableName + "_mem", ts_valueObj.i_TimeStamp);//插入l_Queue字典
            }
        } else {
            // 清空队列
            l_Queue.put(strTableName + "_mem", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }

        if (!mem_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {
            Date now = new Date();
            //当前时间 - 字典发生报警时的时间
            int seconds = TS_Common.getDistanceTime(ts_valueObj.i_TimeStamp, mem_TS);
            //如果 时间差 > mem持续时间
            if (seconds > TS_PgInit.memcontinue) {
                System.out.println("==========" + strTableName + " mem发生告警:   计算开始时间【" + formatter.format(ts_valueObj.i_TimeStamp) + "】 - 采集时间【" + mem_TS + "】 " + seconds + "秒 ==========");

                int m = (seconds % 3600) / 60;
                int s = (seconds % 3600) % 60;
                String strContent = "";
                if (m == 0)
                    strContent = s + "秒";
                else
                    strContent = m + "分" + s + "秒";
                float m1 = (TS_PgInit.memlimit % 3600) / 60;
                float s1 = (TS_PgInit.memlimit % 3600) % 60;
                String strLimit = "";
                if (m1 == 0 || m1 > 0)
                    strLimit = s1 + "秒";
                else
                    strLimit = m1 + "分" + s1 + "秒";
                String str = String.valueOf(TS_PgInit.memlimit);
                int idx = str.lastIndexOf(".");
                String strNum = str.substring(0, idx);
                int memlimitTmp = Integer.valueOf(strNum);
                ts_pgInit.InsertSysAlert("Mem", ts_valueObj.i_TimeStamp, "内存使用率持续超过阈值（" + memlimitTmp + "%），持续时间" + strContent);

                // 清空队列
                l_Queue.put(strTableName + "_mem", TS_Common.StringToDate("2000-01-01 00:00:00"));
            }
        }

        // 判断字典是否包含"表名_disk"
        if (!l_Queue.containsKey(strTableName + "_disk")) {
            l_Queue.put(strTableName + "_disk", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }
        // 判断字典是否包含"表名_disk"
        Date disk_TS = l_Queue.get(strTableName + "_disk");
        // 如果 disk百分比 > disk配置阈值
        if (f_Disk_Percent > TS_PgInit.disklimit) {
            if (disk_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {// 如果时间为空，则插入
                l_Queue.put(strTableName + "_disk", ts_valueObj.i_TimeStamp);//插入l_Queue字典
            }
        } else {
            // 清空队列
            l_Queue.put(strTableName + "_disk", TS_Common.StringToDate("2000-01-01 00:00:00"));
        }

        if (!disk_TS.equals(TS_Common.StringToDate("2000-01-01 00:00:00"))) {
            Date now = new Date();
            //当前时间 - 字典发生报警时的时间
            int seconds = TS_Common.getDistanceTime(ts_valueObj.i_TimeStamp, disk_TS);
            //如果 时间差 > disk持续时间
            if (seconds > TS_PgInit.diskdellimit) {
                System.out.println("==========" + strTableName + " disk发生告警:   计算开始时间【" + formatter.format(ts_valueObj.i_TimeStamp) + "】 - 采集时间【" + disk_TS + "】 " + seconds + "秒 ==========");

                int m = (seconds % 3600) / 60;
                int s = (seconds % 3600) % 60;
                String strContent = "";
                if (m == 0)
                    strContent = s + "秒";
                else
                    strContent = m + "分" + s + "秒";
                float m1 = (TS_PgInit.disklimit % 3600) / 60;
                float s1 = (TS_PgInit.disklimit % 3600) % 60;
                String strLimit = "";
                if (m1 == 0 || m1 > 0)
                    strLimit = s1 + "秒";
                else
                    strLimit = m1 + "分" + s1 + "秒";
                String str = String.valueOf(TS_PgInit.disklimit);
                int idx = str.lastIndexOf(".");
                String strNum = str.substring(0, idx);
                int disklimitTmp = Integer.valueOf(strNum);
                ts_pgInit.InsertSysAlert("Disk", ts_valueObj.i_TimeStamp, "磁盘容量持续超过阈值（" + disklimitTmp + "%），持续时间" + strContent);

                // 清空队列
                l_Queue.put(strTableName + "_disk", TS_Common.StringToDate("2000-01-01 00:00:00"));
            }
        }
    }
}
