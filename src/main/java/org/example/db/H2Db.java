package org.example.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class H2Db {

    private static final String USER = "bing";
    private static final String PASSWORD = "wallpaper";
    private static final String DRIVER_CLASS = "org.h2.Driver";
    // 嵌入式 只允许有一个客户端连接到H2数据库 服务器模式可以同时多个客户端，但需要单独启动数据库服务
    private static final String JDBC_URL = "jdbc:h2:./db/images;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1;MODE=MySQL;INIT=RUNSCRIPT FROM 'db/schema.sql'";

    private static ThreadLocal<Connection> threadLocal = new ThreadLocal<>();

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Connection conn = threadLocal.get();
        if (conn == null) {
            // 设置时区 ，两个中的任意一个都可以
            System.setProperty("user.timezone", "UTC+8");
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
            Class.forName(DRIVER_CLASS);
            conn = DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
            threadLocal.set(conn);
        }
        return conn;
    }

    public static int addWallpaper2DB(Wallpaper wallpaper) {
        String sql = "merge into wallpaper(startdate,fullstartdate,enddate,url,urlbase,copyright,copyrightlink,title,quiz,hsh)"
                     + "values('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s')";
        sql = String.format(sql, wallpaper.getStartdate(), wallpaper.getFullstartdate(), wallpaper.getEnddate(),
                wallpaper.getUrl(), wallpaper.getUrlbase(), wallpaper.getCopyright(), wallpaper.getCopyrightlink(),
                wallpaper.getTitle(), wallpaper.getQuiz(), wallpaper.getHsh());
        int ret = 0;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            ret = statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(null, statement, connection);
        }
        return ret;
    }

    public static int batchAddWallpaper2DB(List<Wallpaper> wallpapers) {
        String sql = "merge into wallpaper(startdate,fullstartdate,enddate,url,urlbase,copyright,copyrightlink,title,quiz,hsh)"
                     + "values(?,?,?,?,?,?,?,?,?,?)";
        int ret = 0;
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            for (int i = 0, size = wallpapers.size(); i < size; i++) {
                Wallpaper wallpaper = wallpapers.get(i);
                statement.setString(1, wallpaper.getStartdate());
                statement.setString(2, wallpaper.getFullstartdate());
                statement.setString(3, wallpaper.getEnddate());
                statement.setString(4, wallpaper.getUrl());
                statement.setString(5, wallpaper.getUrlbase());
                statement.setString(6, wallpaper.getCopyright());
                statement.setString(7, wallpaper.getCopyrightlink());
                statement.setString(8, wallpaper.getTitle());
                statement.setString(9, wallpaper.getQuiz());
                statement.setString(10, wallpaper.getHsh());
                statement.addBatch();
                if ((i != 0 && i % 30 == 0) || i == size - 1) {
                    int[] arr = statement.executeBatch();
                    ret += arr.length;
                    connection.commit();
                    statement.clearBatch();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(null, statement, connection);
        }
        return ret;
    }

    public static int deleteWallpaper2DB(String date) {
        String sql = "delete from wallpaper where enddate = '" + date + "'";
        int ret = 0;
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            ret = statement.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(null, statement, connection);
        }
        return ret;
    }

    public static List<Wallpaper> getDBWallpapers(String date) {
        String sql = "select * from wallpaper";
        if (date != null && date.trim().length() != 0) {
            sql += " where enddate like '" + date + "%'";
        }
        sql += " order by enddate desc";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Wallpaper> wallpapers = new ArrayList<>();
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Wallpaper wallpaper = getWallpaper(resultSet);
                wallpapers.add(wallpaper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(resultSet, statement, connection);
        }
        return wallpapers;
    }

    public static List<Wallpaper> getDBWallpapers(int days) {
        String sql = " select * from wallpaper w where datediff(dd,enddate,current_date) <= " + (days - 1)
                     + " order by enddate desc";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        List<Wallpaper> wallpapers = new ArrayList<>();
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Wallpaper wallpaper = getWallpaper(resultSet);
                wallpapers.add(wallpaper);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(resultSet, statement, connection);
        }
        return wallpapers;
    }

    public static Wallpaper getDBWallpaper(String date) {
        String sql = "select * from wallpaper where enddate = '" + date + "'";
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        Wallpaper wallpaper = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                wallpaper = getWallpaper(resultSet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(resultSet, statement, connection);
        }
        return wallpaper;
    }

    private static Wallpaper getWallpaper(ResultSet resultSet) throws SQLException {
        Wallpaper wallpaper = new Wallpaper();
        wallpaper.setStartdate(resultSet.getString("startdate"));
        wallpaper.setFullstartdate(resultSet.getString("fullstartdate"));
        wallpaper.setEnddate(resultSet.getString("enddate"));
        wallpaper.setUrl(resultSet.getString("url"));
        wallpaper.setUrlbase(resultSet.getString("urlbase"));
        wallpaper.setCopyright(resultSet.getString("copyright"));
        wallpaper.setCopyrightlink(resultSet.getString("copyrightlink"));
        wallpaper.setTitle(resultSet.getString("title"));
        wallpaper.setQuiz(resultSet.getString("quiz"));
        wallpaper.setHsh(resultSet.getString("hsh"));
        return wallpaper;
    }

    public static void close(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
