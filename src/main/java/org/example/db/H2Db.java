package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class H2Db {
    private static final String USER = "bing";
    private static final String PASSWORD = "wallpaper";
    private static final String DRIVER_CLASS = "org.h2.Driver";
    private static final String JDBC_URL = "jdbc:h2:./db/images;AUTO_SERVER=TRUE";

    private static Connection conn;


    static {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            System.err.println("h2数据库驱动类找不到！");
        }
    }

    public static Connection getConnection() {
        if (conn != null) {
            return conn;
        }
        try {
            conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("获取h2数据库连接失败！");
        }
        return conn;
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
