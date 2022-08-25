package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimeZone;

public class H2Db {
    private static final String USER = "bing";
    private static final String PASSWORD = "wallpaper";
    private static final String DRIVER_CLASS = "org.h2.Driver";
    // 嵌入式 只允许有一个客户端连接到H2数据库 服务器模式可以同时多个客户端，但需要单独启动数据库服务
    private static final String JDBC_URL = "jdbc:h2:./db/images;AUTO_SERVER=TRUE;;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=RUNSCRIPT FROM 'db/schema.sql'";

    private static Connection connection;
    private static Statement statement;

    static {
        try {
            // 设置时区 ，两个中的任意一个都可以
            System.setProperty("user.timezone", "UTC+8");
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("h2数据库驱动类找不到！" + e.getMessage());
        }
    }

    public static Connection getConnection() {
        if (connection != null) {
            return connection;
        }
        try {
            connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("获取h2数据库连接失败！" + e.getMessage());
        }
        return connection;
    }

    public static Statement getStatement() {
        if (statement != null) {
            return statement;
        }
        Connection conn = getConnection();
        try {
            statement = conn.createStatement();
        } catch (SQLException e) {
            System.err.println("获取h2数据库Statement对象失败！" + e.getMessage());
        }
        return statement;
    }

    public static boolean delete(String hsh){
        Statement session = getStatement();
        boolean res = false;
        try {
            res = session.execute("delete from wallpaper where hsh = '" + hsh + "'");
        } catch (SQLException e) {
            System.err.println("刪除"+hsh+"数据失败！" + e.getMessage());
        }
        // close(session, connection);
        return res;
    }

    public static boolean addWallpaper(Wallpaper wallpaper) {
        delete(wallpaper.getHsh());
        Statement session = getStatement();
        String sql = "insert into wallpaper(startdate,fullstartdate,enddate,url,urlbase,copyright,copyrightlink,title,quiz,hsh)"
                     + "values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";
        sql = String.format(sql, wallpaper.getStartdate(), wallpaper.getFullstartdate(), wallpaper.getEnddate(),
                wallpaper.getUrl(), wallpaper.getUrlbase(), wallpaper.getCopyright(), wallpaper.getCopyrightlink(),
                wallpaper.getTitle(), wallpaper.getQuiz(), wallpaper.getHsh(), wallpaper.getDesc(), wallpaper.getCreatetime());
        boolean res = false;
        try {
            res = session.execute(sql);
        } catch (SQLException e) {
            System.err.println("新增" + wallpaper.getHsh() + "数据失败！" + e.getMessage());
        }
        // close(session, connection);
        return res;
    }

    //释放资源
    public static void close(Statement stemt, Connection conn) {
        if (stemt != null) {
            try {
                stemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet rs, Statement stemt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (stemt != null) {
            try {
                stemt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
