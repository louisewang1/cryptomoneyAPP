package util;

/**
 * Created by zhangwei on 2018/3/8.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil{
//    private static Connection conn;
	private static Connection conn = null;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/test";
//	private static final String URL = "jdbc:mysql://192.168.1.16:3306/test";
    private static final String USERNAME = "user1";
//	 private static final String USERNAME = "user";
//	 private static final String PASSWORD = "user";
    private static final String PASSWORD = "User1234!";
    static{
        try{
            Class.forName("com.mysql.jdbc.Driver");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Connection getConn(){
        try{
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
//            System.out.println("连接成功");
        }
        catch (Exception e) {
            e.printStackTrace();
//            System.out.println("连接失败");
        }
        return conn;
    }
    public static void closeConn(Connection conn){
        if(conn != null){
            try {
                conn.close();
//                System.out.println("连接关闭");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

}

