package com.example.cryptomoney;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBOpenHelper {
    private static String diver = "com.mysql.jdbc.Driver";
    //加入utf-8是为了后面往表中输入中文，表中不会出现乱码的情况
    private static String url = "jdbc:mysql://192.168.1.5:3306/test";
    private static String user = "user";//用户名
    private static String password = "user";//密码
    /*
     * 连接数据库
     * */
    public static Connection getConn(){
        Connection conn = null;
        try {
            Class.forName(diver);
            conn = (Connection) DriverManager.getConnection(url,user,password);//获取连接
            Log.d("DBOpenHelper", "连接成功");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            Log.d("DBOpenHelper", "连接失败：" + e.getLocalizedMessage());
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        Log.d("DBOpenHelper", "连接关闭");
    }
}