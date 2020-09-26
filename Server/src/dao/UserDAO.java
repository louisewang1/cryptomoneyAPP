package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import entity.UserInfo;
import util.DBUtil;
import entity.Record;
import java.util.Random;

public class UserDAO {
	
	public final static int TYPE_CONN_FAILED = -1;
	public final static int TYPE_LOGIN_FAILED = 0;
    public final static int TYPE_TR_SUCCESS = 1;
    public final static int TYPE_AMOUNT_FAILED = 0;
    public final static int TYPE_ID_FAILED = -2;
    public final static int TYPE_SELF_FAILED = -3;
    public final static int TYPE_BALANCE_FAILED = -4;
    public final static int TYPE_PK_FAILED = -5;
    
    private static String timePattern = "yyyy-MM-dd HH:mm:ss";
	
//	public boolean queryUser(UserInfo userinfo){
//		Connection conn = DBUtil.getConn();
//		String sql="select * from logindb where username=? and pwd=?";
//		try {
//			PreparedStatement ps = conn.prepareStatement(sql);
//			ps.setString(1, userinfo.getUsername());
//			ps.setString(2, userinfo.getPassword());
//			ResultSet rs = ps.executeQuery();
//			if(rs.next()){
//				return true;
//			}else{
//				return false;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//			return false;
//		}finally{
//			DBUtil.closeConn();
//		}
//		
//	}
	
	public int logincheck(Connection conn, UserInfo userinfo){  // 若登录成功返回用户id（>0）
//		Connection conn = DBUtil.getConn();
		if (conn == null) return TYPE_CONN_FAILED;
        CallableStatement cs = null;
        try {
            cs =conn.prepareCall("{call login_check(?,?,?)}"); // 调用login_check(in username, in pwd, out account_id)
            cs.setString(1,userinfo.getUsername());
            cs.setString(2,userinfo.getPassword());
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();
//            Log.d("LoginActivity","id: " + cs.getInt(3));
            if (cs.getInt(3) == 0) return TYPE_LOGIN_FAILED;
            else {
//            	userinfo.setId(cs.getInt(3));
            	return cs.getInt(3);
            }
        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return TYPE_CONN_FAILED;
	}
	
	public Object[] displayinfo(Connection conn,int account_id) {
//		Connection conn1 = DBUtil.getConn();
//      Log.d("LoginActivity","conn: " + conn);
      if (conn == null) return null;
      Object[] account_info = new Object[5];
      account_info[0] = account_id;
      CallableStatement cs = null;
      try {
          cs =conn.prepareCall("{call display_info(?,?,?,?,?)}"); // 调用display_info(in account_id,out username, out balance, out email, out cellphone)
          cs.setInt(1,account_id);
          cs.registerOutParameter(2, Types.VARCHAR);
          cs.registerOutParameter(3, Types.DOUBLE);
          cs.registerOutParameter(4, Types.VARCHAR);
          cs.registerOutParameter(5, Types.VARCHAR);
          cs.execute();
          if (cs.getString(2) == null) return null;  // 无匹配条目
          account_info[1] = cs.getString(2);
          account_info[2] = cs.getDouble(3);
          account_info[3] = cs.getString(4);
          account_info[4] = cs.getString(5);
      } catch (Exception e){
          e.printStackTrace();
      }if (cs != null) {
          try {
              cs.close();
          } catch (SQLException e) {
              e.printStackTrace();
          }
      }
      return account_info;
  }
	
	public int accountregister(Connection conn,String username, String pwd, String email,String cellphone, String pk) {
        CallableStatement cs = null;
        try {
//        	Connection conn = DBUtil.getConn();
            cs =conn.prepareCall("{call account_register(?,?,?,?,?,?)}");
            cs.setString(1, username);
            cs.setString(2, pwd);
            cs.setString(3, email);
            cs.setString(4,cellphone);
            cs.setNString(5, pk);
            cs.registerOutParameter(6,Types.INTEGER);
            cs.execute();
            if (cs.getInt(6) == 1) return 1;  // success
        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return 0;  //error
    }
	
	public int exetransaction(Connection conn, Integer from_account_id, Integer to_account_id, Double value) {
        if (conn == null) return TYPE_CONN_FAILED;
        CallableStatement cs = null;
        try {
            cs =conn.prepareCall("{call exe_transaction(?,?,?,?)}");
            cs.setInt(1, from_account_id);
            cs.setInt(2, to_account_id);
            cs.setDouble(3, value);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
//            Log.d("MainActivity","transaction result: " + cs.getInt(4));
            if (cs.getInt(4) == 1) return TYPE_TR_SUCCESS;
            else if (cs.getInt(4) == 0) return TYPE_AMOUNT_FAILED;
            else if (cs.getInt(4) == -1 ) return TYPE_ID_FAILED;
            else if (cs.getInt(4) == -2) return TYPE_SELF_FAILED;
        } catch (Exception e){
            e.printStackTrace();
        }if (cs != null) {
            try {
                cs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return TYPE_CONN_FAILED;
    }
	
	 public List<Record> trandetail(Connection conn, Integer account_id) {
	        if (conn == null) return null;
	        List<Record> recordList = new ArrayList<>();
	        SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
	        CallableStatement cs = null;
	        ResultSet rs;
	        try {
	            cs =conn.prepareCall("{call tran_detail(?)}"); // 调用display_info(in account_id,out username, out balance, out email, out cellphone)
	            cs.setInt(1,account_id);
	            cs.execute();
	            rs = cs.getResultSet();
	            int index = 1;
	            while (rs.next()) {
	                Record record = new Record();
	                record.setIndex(index);
	                record.setFrom(rs.getInt("tr_from_account"));
	                record.setTo(rs.getInt("tr_to_account"));
	                record.setTime(sdf.format(rs.getTimestamp("tr_time")));
	                record.setValue(rs.getDouble("tr_value"));
	                recordList.add(record);
	                index ++;
	            }

	        } catch (Exception e){
	            e.printStackTrace();
	        }if (cs != null) {
	            try {
	                cs.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return recordList;
	    }
	 
	 public String cryptotransfer(Connection conn, Integer account_id, Double value,String pk) {
		 if (conn == null) return null;
		 CallableStatement cs = null;
		 String result = null;
		 try {
			 cs = conn.prepareCall("{call check_pk(?,?,?)}");
			 cs.setInt(1, account_id);
			 cs.setString(2, pk);
			 cs.registerOutParameter(3,Types.INTEGER);
			 cs.execute();
			 if (cs.getInt(3) == 1) {  // pk matches
				 cs = conn.prepareCall("{call check_balance(?,?,?)}");
				 cs.setInt(1, account_id);
				 cs.setDouble(2, value);
				 cs.registerOutParameter(3, Types.INTEGER);
				 cs.execute();
				 if (cs.getInt(3) == 1) {  // balance enough
					 String addr;
					 do {
//						 System.out.println("add a record");
						 addr = getRandomString(100);  
						 System.out.println("token address= " + addr);
						 cs = conn.prepareCall("{call crypto_transfer(?,?,?,?)}");
						 cs.setInt(1, account_id);
						 cs.setDouble(2, value);
						 cs.setString(3, addr);
						 cs.registerOutParameter(4, Types.INTEGER);
						 cs.execute();
					 }while (cs.getInt(4) != 1);
					 return result = addr;
				 }
				 else {
					 result = "not enough balance";
				 }
			 } else {
				 result = "pk doesn't match";
			 } 
		 } catch (Exception e){
	            e.printStackTrace();
	     }if (cs != null) {
	    	 try {
	    		 cs.close();
	    	 } catch (SQLException e) {
	                e.printStackTrace();
	         }
	     }
	        return result;
	 
	 }
	 
	//generate random address
	 public static String getRandomString(int length){
	     String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	     Random random=new Random();
	     StringBuffer sb=new StringBuffer();
	     for(int i=0;i<length;i++){
	       int number=random.nextInt(62);
	       sb.append(str.charAt(number));
	     }
	     return sb.toString();
	 }
	
}

