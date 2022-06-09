package org.graylog2.plugin.custom.timeseriesdb;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 Hikar 连接池的操作类
 */
public class TS_HikariOpt {
    private static final Logger LOG = LoggerFactory.getLogger(TS_HikariOpt.class);

    //-- Hikari Datasource -->
    //driverClassName无需指定，除非系统无法自动识别
    private static String driverClassName = "com.taosdata.jdbc.rs.RestfulDriver";
    //address
    private static String jdbcUrl = "";
    //用户名
    private static String username = "";
    //密码
    private static String password = "";
    //数据库名称
    public static String DBName = "";
    //等待连接池分配连接的最大时长（毫秒），超过这个时长还没可用的连接则发生SQLException， 缺省:30秒，设置连接超时为8小时 -->
    private static int connectionTimeout;
    // 一个连接idle状态的最大时长（毫秒），超时则被释放（retired），缺省:10分钟，设置为60000 -->
    private static int idleTimeout;
    //一个连接的生命时长（毫秒），超时而且没被使用则被释放（retired），缺省:30分钟，设置为60000 -->
    private static int maxLifetime;
    // 连接池中允许的最大连接数。缺省值：10；推荐的公式：((core_count * 2) + effective_spindle_count) -->
    private static int maximumPoolSize;
    static HikariDataSource hikariDataSource = new HikariDataSource();

    static {
        readProperties();
        dataSourceConfig();
    }

    /**
     * 读取数据库配置文件
     *
     * @return
     * @Exception FileNotFoundException, IOException
     */
    private static void readProperties() {
        try {
//            Properties prop = new Properties();
//            Class clazz = TS_HikariOpt.class;
//
//            InputStream in = new FileInputStream("./src/config.properties");
//
//            prop.load(in);
//
//            url = prop.getProperty("ts_url");
//            user = prop.getProperty("ts_username");
//            password = prop.getProperty("ts_password");

//            jdbcUrl = "jdbc:TAOS-RS://127.0.0.1:6041/" + DBName;//System.getenv("ts_url");
//            username = "root";//System.getenv("ts_username");
//            password = "taosdata";//System.getenv("ts_password");

            jdbcUrl = System.getenv("ts_url") + DBName;
            username = System.getenv("ts_username");
            password = System.getenv("ts_password");

            LOG.info("初始化 Hikar 连接池的操作类配置信息：{},{},{}", jdbcUrl, username, password);
            connectionTimeout = Integer.parseInt(String.valueOf(8 * 60 * 60));
            idleTimeout = Integer.parseInt(String.valueOf(60000));
            maxLifetime = Integer.parseInt(String.valueOf(60000));
            maximumPoolSize = Integer.parseInt(String.valueOf(10));

        } catch (Exception e) {
            LOG.info("读取数据库参数出现问题：" + e);
            throw e;
        }
    }

    /**
     * 设置datasource各个属性值
     */
    private static void dataSourceConfig() {
        try {
            hikariDataSource.setDriverClassName(driverClassName);
            hikariDataSource.setJdbcUrl(jdbcUrl);
            hikariDataSource.setUsername(username);
            hikariDataSource.setPassword(password);
            hikariDataSource.setConnectionTimeout(connectionTimeout);
            hikariDataSource.setIdleTimeout(idleTimeout);
            hikariDataSource.setMaximumPoolSize(maxLifetime);
            hikariDataSource.setMaximumPoolSize(maximumPoolSize);
        } catch (Exception e) {
            LOG.info("设置datasource各个属性值异常!" + e);
            throw e;
        }
    }


    /**
     * 取得数据库连接
     *
     */
    public static Connection getConnection() throws Exception {

        Connection connection = null;
        try {
            connection = hikariDataSource.getConnection();
        } catch (Exception e) {
            LOG.info("取得数据库连接时发生异常!" + e);
            throw e;
        }
        return connection;
    }

    /**
     * 释放数据库连接
     *
     * @param connection
     * @throws Exception
     */
    public static void freeConnection(Connection connection) throws Exception {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                LOG.info("释放数据库连接时发生异常!" + e.getMessage());
            }
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

    /**
     * 查询
     *
     * @param sql sql语句
     * @return rs ResultSet对象
     */
    public static ResultSet Query(String sql) throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            return resultSet;

        } catch (Exception e) {
            System.out.println("Query 错误语句【" + sql + "】 错误信息【" + e.getMessage() + "】");
        } finally {
            try {
                if (connection != null && !connection.isClosed())
                    connection.close();
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception ex) {
                System.out.println("Query 回收错误信息【" + ex.getMessage() + "】");
            }
        }

        return null;
    }

    /**
     * 更新
     *
     * @param sql       sql语句
     * @return -1不成功,1成功
     */
    public static int Update(String sql) throws SQLException {
        int result = -1;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            result = statement.executeUpdate(sql);

            return 1;
        } catch (Exception e) {
            System.out.println("Update 错误语句【" + sql + "】错误信息【" + e.getMessage() + "】");
        } finally {
            try {
                if (connection != null && !connection.isClosed())
                    connection.close();
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception ex) {
                System.out.println("Update 回收错误信息【" + ex.getMessage() + "】");
            }
        }

        return result;
    }

    /**
     * 增加
     *
     * @param strDBName 数据库名
     * @param sql       sql语句
     * @return -1不成功,1成功
     */
    public static int Add(String strDBName, String sql) {
        Connection connection = null;
        Statement statement = null;
        int result = -1;
        try {
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet != null) {
                if (resultSet.next()) {
                    result = resultSet.getInt(1);
                }
            }

            return 1;
        } catch (Exception e) {
            System.out.println("Add 错误语句【" + sql + "】错误信息【" + e.getMessage() + "】");
        } finally {
            try {
                if (connection != null && !connection.isClosed())
                    connection.close();
                if (statement != null) {
                    statement.close();
                }
            } catch (Exception ex) {
                System.out.println("Add 回收错误信息【" + ex.getMessage() + "】");
            }
        }
        return result;
    }
}
