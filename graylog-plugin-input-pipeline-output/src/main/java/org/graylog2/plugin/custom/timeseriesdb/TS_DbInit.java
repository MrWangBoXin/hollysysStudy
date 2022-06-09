package org.graylog2.plugin.custom.timeseriesdb;

import java.io.FileNotFoundException;
import java.sql.ParameterMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 时序数据库初始化
 */
public class TS_DbInit {
    /**
     * 构造 Connction 链接
     *
     * @param strDbName 数据库名称
     */
    public TS_DbInit(String strDbName) throws SQLException {
        TS_HikariOpt.DBName = strDbName;
        TS_HikariOpt opt = new TS_HikariOpt();
    }

    public TS_DbInit() {
    }

    /**
     * 创建数据库
     * 默认数据库名为：Sec
     */
    public void CreateDB() throws SQLException, FileNotFoundException {
        String strSQL = "";

        String strCurrentDbName = "";

        // 查询所有数据库
        strSQL = "show databases;";

        TS_HikariOpt opt = new TS_HikariOpt();
        ResultSet resultSet = TS_HikariOpt.Query(strSQL);
        if (resultSet == null) {
            System.out.println("执行语句错误：" + strSQL);
            return;
        }

        List<Map> list = TS_HikariOpt.convertList(resultSet);
        if (list.isEmpty()) {
            System.out.println("执行语句 " + strSQL + " 结果为空");
            return;
        }

        //判断数据库 Sec 是否存在
        for (Map data : list) {
            Iterator<Map.Entry> entries = data.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (entry.getKey().equals("name") && ((String) entry.getValue()).equals("Sec")) {
                    strCurrentDbName = (String) entry.getValue();
                    break;
                }
            }
        }

        //不存在 Sec 数据库
        if (strCurrentDbName == "") {
            strSQL = "create database sec;";
            int i_resultSet = TS_HikariOpt.Update(strSQL);
            if (i_resultSet == -1) {
                System.out.println("创建数据库错误");
                return;
            }
            System.out.println("创建数据库 " + strCurrentDbName + "成功");
        } else {
            System.out.println("找到匹配数据库：" + strCurrentDbName);
        }
    }

    /**
     * 是否存在该超级表？
     *
     * @param strTableName 表名
     * @return true存在
     */
    public boolean IsExistsSTable(String strTableName) throws SQLException, FileNotFoundException {
        String strSQL = "";

        String strFindTableName = "";

        // 查询所有表
        strSQL = "show sec.stables;";

        TS_HikariOpt opt = new TS_HikariOpt();
        ResultSet resultSet = TS_HikariOpt.Query(strSQL);
        if (resultSet == null) {
            System.out.println("执行语句错误：" + strSQL);
            return false;
        }

        List<Map> list = TS_HikariOpt.convertList(resultSet);
        if (list.isEmpty()) {
            System.out.println("执行语句 " + strSQL + " 结果为空");
        }

        //判断表 strTalbeName 是否存在
        for (Map data : list) {
            Iterator<Map.Entry> entries = data.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (entry.getKey().equals("name") && ((String) entry.getValue()).equals(strTableName)) {
                    strFindTableName = (String) entry.getValue();
                    break;
                }
            }
        }

        //不存在表
        if (strFindTableName == "") {
            return false;
        }

        return true;
    }

    /**
     * 是否存在该子表？
     *
     * @param strTableName 表名
     * @return true存在
     */
    public boolean IsExistsTable(String strTableName) throws SQLException, FileNotFoundException {
        String strSQL = "";

        String strFindTableName = "";

        // 查询所有表
        strSQL = "show sec.tables;";

        TS_HikariOpt opt = new TS_HikariOpt();
        ResultSet resultSet = TS_HikariOpt.Query(strSQL);
        if (resultSet == null) {
            System.out.println("执行语句错误：" + strSQL);
            return false;
        }

        List<Map> list = TS_HikariOpt.convertList(resultSet);
        if (list.isEmpty()) {
            System.out.println("执行语句 " + strSQL + " 结果为空");
        }

        //判断表 strTalbeName 是否存在
        for (Map data : list) {
            Iterator<Map.Entry> entries = data.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                if (entry.getKey().equals("table_name") && ((String) entry.getValue()).equals(strTableName)) {
                    strFindTableName = (String) entry.getValue();
                    break;
                }
            }
        }

        //不存在表
        if (strFindTableName == "") {
            return false;
        }

        return true;
    }

    /**
     * 创建超级表
     * 若不存在则创建，否则返回
     *
     * @param strSuperTableName 超级表表名（为t+ip分割组合，比如ip为127.0.0.1则表名为t_127_0_0_1）
     * @return 1成功
     */
    public int CreateSTable(String strSuperTableName) throws SQLException, FileNotFoundException {
        String strSQL = "create table if not exists sec." + strSuperTableName +
                " (ts TIMESTAMP, " +
                " c_IpAddress NCHAR(50), " +
                " c_CPU FLOAT, " +
                " c_Mem FLOAT, " +
                " c_MemSurplus FLOAT, " +
                " c_Disk FLOAT, " +
                " c_DiskSurplus FLOAT, " +
                " c_NetIn FLOAT, " +
                " c_NetOut FLOAT, " +
                " c_NetIsEnable INT " +
                " ) " +
                " tags " +
                " ( " +
                " c_MacAddress NCHAR(50), " +
                " c_MacName NCHAR(50) " +
                " ); ";

        try {
            TS_HikariOpt opt = new TS_HikariOpt();

            int i_resultSet = TS_HikariOpt.Update(strSQL);
            if (i_resultSet == -1) {
                if (TS_HikariOpt.Update(strSQL) == -1) {
                    System.out.println("再次--创建超级表 " + strSuperTableName + " 错误");
                    return -1;
                } else {
                    //System.out.println("再次--创建超级表 " + strSuperTableName + " 成功");
                    return 1;
                }
            }
            //System.out.println("创建超级表 " + strSuperTableName + " 成功");
            return 1;
        } catch (Exception ex) {
            System.out.println("CreateSTable 错误：" + ex.getMessage());
            return -1;
        }
    }

    /**
     * 动态创建超级表
     * 若不存在则创建，否则返回
     *
     * @param strSuperTableName 超级表名
     * @param strPortListSQL    动态端口的Tag SQL语句（比如有2个端口：3389和445，则创建c_3389和c_445列）
     * @return 1成功
     */
    public int CreateTable(String strSuperTableName, String[] strPortListSQL) throws SQLException, FileNotFoundException {
        String strPortListSQLTmp = ",";
        if (strPortListSQL.length > 0) {
            strPortListSQLTmp += String.join(",", strPortListSQL);
        }

        String strSQL = "create table if not exists sec." + strSuperTableName +
                " (ts TIMESTAMP, " +
                " c_CPU FLOAT, " +
                " c_Mem FLOAT, " +
                " c_MemSurplus FLOAT, " +
                " c_Disk FLOAT, " +
                " c_DiskSurplus FLOAT, " +
                " c_NetIn FLOAT, " +
                " c_NetOut FLOAT, " +
                " c_NetIsEnable INT " +
                strPortListSQLTmp +
                " ) " +
                " tags " +
                " ( " +
                " c_IpAddress NCHAR(50), " +
                " c_MacAddress NCHAR(50), " +
                " c_MacName NCHAR(50) " +
                " ); ";
        try {
            TS_HikariOpt opt = new TS_HikariOpt();
            int i_resultSet = TS_HikariOpt.Update(strSQL);
            if (i_resultSet == -1) {
                if (TS_HikariOpt.Update(strSQL) == -1) {
                    System.out.println("再次--动态创建超级表 " + strSuperTableName + " 错误");
                    return -1;
                } else {
                    //System.out.println("再次--动态创建超级表 " + strSuperTableName + " 成功");
                    return 1;
                }
            }
            //System.out.println("动态创建超级表 " + strSuperTableName + " 成功");
            return 1;
        } catch (Exception ex) {
            System.out.println("CreateTable 错误：" + ex.getMessage());
            return -1;
        }
    }

    /**
     * 创建报警监控表
     *
     * @param strTableName 表名
     * @param strField     报警监控字段
     * @param limitValue   阈值范围
     * @return 1成功
     * @throws FileNotFoundException
     */
    public int CreateAlertTable(String strTableName, String strField, String limitValue) throws FileNotFoundException {
        String strSQL = "create table if not exists sec.alert_" + strTableName +
                " as select " + strField + " from sec." + strTableName +
                " interval(1m) sliding(30s) " +
                " where " + strField + ">" + limitValue +
                " ; ";
        try {
            TS_HikariOpt opt = new TS_HikariOpt();
            int i_resultSet = TS_HikariOpt.Update(strSQL);
            if (i_resultSet == -1) {
                System.out.println("创建子表 alert_" + strTableName + " 错误");
                return -1;
            }
            //System.out.println("创建子表 alert_" + strTableName + " 成功");
            return 1;
        } catch (Exception ex) {
            System.out.println("CreateAlertTable 错误：" + ex.getMessage());
            return -1;
        }
    }

    /**
     * 插入原始数据
     *
     * @param strSQL 执行sql语句
     * @return 1成功
     */
    public int AddData(String strSQL) throws FileNotFoundException {
        try {
            TS_HikariOpt opt = new TS_HikariOpt();
            int i_resultSet = TS_HikariOpt.Update(strSQL);
            if (i_resultSet == -1) {
                if (TS_HikariOpt.Update(strSQL) == -1) {
                    System.out.println("再次--插入数据 " + strSQL + " 错误");
                    return -1;
                } else {
                    return 1;
                }
            }
            //System.out.println("插入数据 " + strSQL + " 成功");
            return 1;
        } catch (Exception ex) {
            System.out.println("AddData 错误：" + ex.getMessage());
            return -1;
        }
    }
}
