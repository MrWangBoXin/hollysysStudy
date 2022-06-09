package org.graylog2.plugin.custom.timeseriesdb;

import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Pg数据库初始化
 */
public class TS_PgInit {
    //cpu告警阈值
    public static float cpulimit = 95f;
    //cpu告警持续
    public static float cpucontinue = 300f;
    //内存告警阈值
    public static float memlimit = 95f;
    //内存告警持续
    public static float memcontinue = 300f;
    //磁盘容量告警阈值
    public static float disklimit = 80f;
    //磁盘容量告警持续
    public static float diskdellimit = 300f;

    /**
     * 获取系统告警配置信息
     *
     * @return 1成功、-1不成功
     */
    public int GetSystemAlertConfig() throws FileNotFoundException, SQLException {
        try {
            TS_PgOpt ts_pgOpt = new TS_PgOpt();
            //查询系统告警阈值表
            String strSQL = "SELECT t.* FROM \"ThresholdValueConfig\" t;";
            ResultSet resultSet = TS_PgOpt.Query(strSQL);
            if (resultSet == null) {
                System.out.println("执行语句错误：" + strSQL);
                return -1;
            }
            List<Map> list = TS_PgOpt.convertList(resultSet);
            if (list.isEmpty()) {
                System.out.println("执行语句 " + strSQL + " 结果为空");
                return -1;
            }

            //读取系统告警阈值表的值
            for (Map data : list) {
                Iterator<Map.Entry> entries = data.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    if (entry.getKey().equals("cpuLimit")) {
                        cpulimit = Float.parseFloat(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("cpuContinue")) {
                        cpucontinue = Float.parseFloat(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("memLimit")) {
                        memlimit = Float.parseFloat(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("memContinue")) {
                        memcontinue = Float.parseFloat(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("diskLimit")) {
                        disklimit = Float.parseFloat(entry.getValue().toString());
                    }
                    if (entry.getKey().equals("diskDelLimit")) {
                        diskdellimit = Float.parseFloat(entry.getValue().toString());
                    }
                }
            }

            System.out.println("√");
            //System.out.println(cpulimit + " " + cpucontinue + " " + memlimit + " " + memcontinue + " " + disklimit + " " + diskdellimit);
            return 1;
        } catch (Exception e) {
            System.out.println("获取配置GetSystemAlertConfig()错误：" + e.getMessage() + "\n");
            return -1;
        }
    }

    /**
     * 向PG插入告警数据
     *
     * @param indexType    包含"cpu"、"mem"、"disk"
     * @param alertTime    发生持续告警最后一次时间
     * @param alertContent 对告警内容进行内容拼接，包括告警发生值和持续时间，例：磁盘容量告警持续超过阈值（80%），持续时间120分
     * @return
     * @throws FileNotFoundException
     * @throws SQLException
     */
    public int InsertSysAlert(String indexType, Date alertTime, String alertContent) throws FileNotFoundException, SQLException {
        try {
            TS_PgOpt ts_pgOpt = new TS_PgOpt();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strNowDate = formatter.format(new Date());

            String strSQL = "INSERT INTO \"SysAlert\" (\"id\", \"indexType\", \"alertTime\", \"alertContent\", \"createdAt\", \"updatedAt\", \"unread\") " +
                    " VALUES " +
                    " ( " +
                    " '" + UUID.randomUUID() + "', " +
                    " '" + indexType + "', " +
                    " '" + formatter.format(alertTime) + "', " +
                    " '" + alertContent + "', " +
                    " '" + strNowDate + "', " +
                    " '" + strNowDate + "', " +
                    " 0 "+
                    " );";
            int resultSet = TS_PgOpt.Insert(strSQL);
            if (resultSet == -1) {
                System.out.println("执行语句错误：" + strSQL);
                return -1;
            }
            System.out.println("PG Insert SysAlert OK\n");
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }
}
