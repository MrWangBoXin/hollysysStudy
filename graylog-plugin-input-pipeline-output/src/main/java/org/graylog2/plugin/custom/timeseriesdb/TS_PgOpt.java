package org.graylog2.plugin.custom.timeseriesdb;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.graylog2.plugin.custom.output.RelayOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * 基于 PstgreSQL 的操作类
 */
public class TS_PgOpt {
    private static final Logger LOG = LoggerFactory.getLogger(TS_PgOpt.class);
    private static String Driver = "";
    private static String url = "";
    private static String user = "";
    private static String password = "";

    /**
     * 构造函数
     */
    public TS_PgOpt() throws FileNotFoundException {
        try {
//            Properties prop = new Properties();
//            Class clazz = TS_HikariOpt.class;
//
//            InputStream in = new FileInputStream("./src/config.properties");
//
//            prop.load(in);
//
//            Driver = prop.getProperty("pg_driver");
//            url = prop.getProperty("pg_url");
//            user = prop.getProperty("pg_username");
//            password = prop.getProperty("pg_password");

//            Driver = "org.postgresql.Driver";//System.getenv("pg_driver");
//            url = "jdbc:postgresql://172.21.33.20:5432/hollysys?currentSchema=prod_test";//System.getenv("pg_url");
//            user = "hollysys";//System.getenv("pg_username");
//            password = "hollysys";//System.getenv("pg_password");

            Driver = System.getenv("pg_driver");
            url = System.getenv("pg_url");
            user = System.getenv("pg_username");
            password = System.getenv("pg_password");

            LOG.info("初始化 PostgreSql 操作类配置信息：{},{},{},{}" , Driver , url , user , password);
        } catch (Exception e) {
            LOG.info("初始化 PostgreSql 连操作类未找到配置文件！");
        }
    }

    /**
     * 获取数据源链接
     *
     * @return
     * @throws SQLException
     */
    public static Connection getDataSource() throws SQLException {
        if (Driver == "" || url == "" || user == "" || password == "") {
            System.out.println("getDataSource 时 url、user、password可能为空！");
            return null;
        }
        Connection connection = null;
        try {
            Class.forName(Driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            System.out.println("连接PostgreSQL数据库错误：" + e.getMessage());
            return null;
        }
        System.out.println("conn pgSQL is ok");
        return connection;
    }

    /**
     * 查询
     *
     * @param sql       sql语句
     * @return rs ResultSet对象
     */
    public static ResultSet Query(String sql) {
        try {
            Connection connection = getDataSource();

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            if (connection != null && !connection.isClosed())
                connection.close();

            return resultSet;

        } catch (Exception e) {
            System.out.println("PG Query 错误语句【" + sql + "】 错误信息【" + e.getMessage() + "】");
            return null;
        }
    }

    /**
     * 插入
     *
     * @param sql       sql语句
     * @return -1不成功    否则成功行数
     */
    public static int Insert(String sql) {
        try {
            Connection connection = getDataSource();

            Statement statement = connection.createStatement();
            int resultSet = statement.executeUpdate(sql);

            if (connection != null && !connection.isClosed())
                connection.close();

            return resultSet;

        } catch (Exception e) {
            System.out.println("PG Insert 错误语句【" + sql + "】 错误信息【" + e.getMessage() + "】");
            return -1;
        }
    }

    public static int Delete(String sql) {
        try {
            Connection connection = getDataSource();
            Statement statement = connection.createStatement();
            int resultSet = statement.executeUpdate(sql);
            if (connection != null && !connection.isClosed())
                connection.close();
            return resultSet;
        } catch (Exception e) {
            System.out.println("PG Delete 错误语句【" + sql + "】 错误信息【" + e.getMessage() + "】");
            return -1;
        }
    }
    /**
     * 输入ResultSet转换为List
     *
     * @return List对象
     * @Resultset rs ResultSet对象
     */
    public static List<Map> convertList(ResultSet rs) throws SQLException {
        List<Map> list = new ArrayList();
        ResultSetMetaData md = rs.getMetaData();//获取键名
        int columnCount = md.getColumnCount();//获取行的数量
        while (rs.next()) {
            Map rowData = new HashMap();//声明Map
            for (int i = 1; i <= columnCount; i++) {
                rowData.put(md.getColumnName(i), rs.getString(i));//获取键名及值
            }
            list.add(rowData);
        }
        return list;
    }
}
