package dao;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import entity.UserInfo;
import util.Base64Utils;
import util.DBUtil;
import util.RSAUtils;
import entity.CryptoRecord;
import entity.Record;
import java.util.Random;

import javax.crypto.Cipher;

public class UserDAO {
	
	public final static int TYPE_CONN_FAILED = -1;
	public final static int TYPE_LOGIN_FAILED = 0;
    public final static int TYPE_TR_SUCCESS = 1;
    public final static int TYPE_AMOUNT_FAILED = 0;
    public final static int TYPE_ID_FAILED = -2;
    public final static int TYPE_SELF_FAILED = -3;
    public final static int TYPE_BALANCE_FAILED = -4;
    public final static int TYPE_PK_FAILED = -5;
    public final static int MAX_DECRYPT_BLOCK = 64;
    
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
	
//	public int merchant_logincheck(Connection conn, String username,String password) {
//		if (conn == null) return TYPE_CONN_FAILED;
//        CallableStatement cs = null;
//        try {
//            cs =conn.prepareCall("{call merchant_login_check(?,?,?)}"); // 调用login_check(in username, in pwd, out account_id)
//            cs.setString(1,username);
//            cs.setString(2,password);
//            cs.registerOutParameter(3, Types.INTEGER);
//            cs.execute();
////            Log.d("LoginActivity","id: " + cs.getInt(3));
//            if (cs.getInt(3) == 0) return TYPE_LOGIN_FAILED;
//            else {
////            	userinfo.setId(cs.getInt(3));
//            	return cs.getInt(3);
//            }
//        } catch (Exception e){
//            e.printStackTrace();
//        }if (cs != null) {
//            try {
//                cs.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//        return TYPE_CONN_FAILED;
//	}
	
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
	
	public int accountregister(Connection conn,String username, String pwd, String email,String cellphone) {
		if (conn == null) return TYPE_CONN_FAILED;
		CallableStatement cs = null;
        try {
//        	Connection conn = DBUtil.getConn();
            cs =conn.prepareCall("{call account_register(?,?,?,?,?,?,?,?)}");
            cs.setString(1, username);
            cs.setString(2, pwd);
            cs.setString(3, "CUSTOMER");
            cs.setString(4, "");
            cs.setString(5, "");
            cs.setString(6, email);
            cs.setString(7,cellphone);
//            cs.setNString(5, pk);
//            cs.setNString(6, modulus);
            cs.registerOutParameter(8,Types.INTEGER);
            cs.execute();
            if (cs.getInt(8) == 1) return 1;  // success
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
	
	 public int merchantregister(Connection conn,String username, String pwd, String email,String cellphone, String sk_exp,String modulus) {
		 if (conn == null) return TYPE_CONN_FAILED;
		 CallableStatement cs = null;
		 try {
			 cs =conn.prepareCall("{call account_register(?,?,?,?,?,?,?,?)}");
	         cs.setString(1, username);
	         cs.setString(2, pwd);
	         cs.setString(3, "MERCHANT");
	         cs.setString(4, sk_exp);
	         cs.setString(5, modulus);
	         cs.setString(6,email);
	         cs.setString(7,cellphone);
	         cs.registerOutParameter(8,Types.INTEGER);
	         cs.execute();
	         if (cs.getInt(8) == 1) return 1;  // success
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
	 
	 public String cryptotransfer(Connection conn, Integer account_id, Double value,String modulus,String pk_exp) {
		 if (conn == null) return null;
		 CallableStatement cs = null;
		 String result = null;
		 try {
//			 cs = conn.prepareCall("{call check_modulus(?,?,?)}");
//			 cs.setInt(1, account_id);
//			 System.out.println("modulus= "+modulus);
//			 cs.setString(2, modulus);
//			 cs.registerOutParameter(3,Types.INTEGER);
//			 cs.execute();
//			 if (cs.getInt(3) == 1) {  // modulus matches
			 cs = conn.prepareCall("{call check_balance(?,?,?)}");
			 cs.setInt(1, account_id);
			 cs.setDouble(2, value);
			 cs.registerOutParameter(3, Types.INTEGER);
			 cs.execute();
			 if (cs.getInt(3) == 1) {  // balance enough
				 String addr;
				 do {
					 addr = getRandomString(20);  
					 System.out.println("token address= " + addr);
					 cs = conn.prepareCall("{call crypto_transfer(?,?,?,?,?,?)}");
					 cs.setInt(1, account_id);
					 cs.setDouble(2, value);
					 cs.setString(3, modulus);
					 cs.setString(4, pk_exp);
					 cs.setString(5, addr);
					 cs.registerOutParameter(6, Types.INTEGER);
					 cs.execute();
					 }while (cs.getInt(6) != 1);
				 return result = addr;
			 }
			 else {
				 result = "not enough balance";
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
	 
	 public List<CryptoRecord> cryptotrandetail(Connection conn, Integer account_id) {
	        if (conn == null) return null;
	        List<CryptoRecord> recordList = new ArrayList<>();
	        SimpleDateFormat sdf = new SimpleDateFormat(timePattern);
	        CallableStatement cs = null;
	        ResultSet rs;
	        try {
	            cs =conn.prepareCall("{call cryptotran_detail(?)}"); // 调用display_info(in account_id,out username, out balance, out email, out cellphone)
	            cs.setInt(1,account_id);
	            cs.execute();
	            rs = cs.getResultSet();
	            int index = 1;
	            while (rs.next()) {
	            	CryptoRecord record = new CryptoRecord();
	                record.setIndex(index);
	                record.setAddr(rs.getString("address"));
	                record.setTime(sdf.format(rs.getTimestamp("crypto_time")));
	                record.setValue(rs.getDouble("amount"));
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
	 
	 public List<String> merchantlist(Connection conn) {
		 if (conn == null) return null;
		 List<String> merchantList = new ArrayList<>();
		 CallableStatement cs = null;
	     ResultSet rs;
	     try {
	    	 cs =conn.prepareCall("{call merchant_list()}"); 
	         cs.execute();
	         rs = cs.getResultSet();
	         int index = 1;
			 while (rs.next()) {
				 merchantList.add(rs.getString("username"));
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
        return merchantList;
	 }
	 
	 public String getcryptomoney(Connection conn,String id_enc, String addr) {
		 if (conn == null) return null;
		 CallableStatement cs = null;
		 String result = null;
		 try {
			 cs = conn.prepareCall("{call addr_to_id(?,?)}");
			 cs.setString(1, addr);
			 cs.registerOutParameter(2,Types.INTEGER);
			 cs.execute();
			 Integer from_id = cs.getInt(2);
			 if (from_id > 0) {  // from_id exists
				 System.out.println("from_id= "+from_id);
				 cs = conn.prepareCall("{call get_pk_N(?,?,?)}");
				 cs.setString(1, addr);
				 cs.registerOutParameter(2,Types.VARCHAR);
				 cs.registerOutParameter(3,Types.VARCHAR);
				 cs.execute();
				 BigInteger pk_exp = new BigInteger(Base64Utils.decode(cs.getString(2)));
				 BigInteger modulus = new BigInteger(Base64Utils.decode(cs.getString(3)));
				 System.out.println("token_pk_exp= "+pk_exp);
				 System.out.println("token_modulus= "+modulus);
				 System.out.println("id_enc= "+id_enc);
				 
				 Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                 RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,pk_exp);
                 PublicKey pk = keyFactory.generatePublic(rsaPublicKeySpec);
                 
				 cipher.init(Cipher.DECRYPT_MODE,pk);
				 String id_dec = new String(cipher.doFinal(Base64Utils.decode(id_enc)));
				 System.out.println("id_dec= "+id_dec);
				 
				 if (id_dec.startsWith("ACCOUNT=")) {
//					 result = "successfully decrypted";
					 Integer to_id = Integer.parseInt(id_dec.split("=")[1]);
					 System.out.println("to_id= "+to_id);
					 cs = conn.prepareCall("{call exe_crypto(?,?,?)}");
					 cs.setInt(1, to_id);
					 cs.setString(2,addr);
					 cs.registerOutParameter(3, Types.DOUBLE);
					 cs.execute();
					 Double amount = cs.getDouble(3);
					 result = amount.toString() + "received";
				 }
				 else {
					 result = "decryption failed";
				 }
			 }
			 else {
				 result = "expired transaction";
			 }
		 } catch (Exception e){
	            e.printStackTrace();
	            result = "decryption failed";
	        }if (cs != null) {
	            try {
	                cs.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return result;
	 }
	 
	 public String addnewcontract(Connection conn, int account_id, double value, String modulus, String pk_exp) {
		 if (conn == null) return null;
		 CallableStatement cs = null;
		 String result = null;
		 String contract_addr;
		 try {
			 do {
				 contract_addr = getRandomString(20); 
				 cs =conn.prepareCall("{call create_contract(?,?,?,?,?,?)}"); 
		    	 cs.setString(1,contract_addr);
		    	 cs.setString(2, modulus);
		    	 cs.setString(3, pk_exp);
		    	 cs.setInt(4, account_id);
		    	 cs.setDouble(5, value);
				 cs.registerOutParameter(6,Types.INTEGER);
		         cs.execute();
			 }while(cs.getInt(6) != 1);
			 System.out.println("contract_addr= "+contract_addr);
			 result = contract_addr;      
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
	 
//	 request=recordtoken&contract_addr=...&enc=<>
	 public String recordnewtoken(Connection conn, String contract_addr, String enc) throws SQLException {
		 String result = "failed";
		 if (conn == null) {
			result = "connection error";
			return result;
		 }
		 CallableStatement cs = null;
		 String token_addr;
		 // get contract details from contract_addr
		 try {
			 cs = conn.prepareCall("{call get_contract_detail(?,?,?,?,?,?)}");
			 cs.setString(1,contract_addr);
			 cs.registerOutParameter(2,Types.VARCHAR);  //pk_exp
			 cs.registerOutParameter(3,Types.VARCHAR);  // N
			 cs.registerOutParameter(4,Types.INTEGER);  // rcver_id
			 cs.registerOutParameter(5,Types.DOUBLE);  // request_amount
			 cs.registerOutParameter(6,Types.DOUBLE);  // current_amount
			 cs.execute();
			 
			 int rcver_id = cs.getInt(4);
			 double request_amount = cs.getDouble(5);
			 double current_amount = cs.getDouble(6);
			 
			 // restore contract_pk
			 
			 BigInteger pk_exp = new BigInteger(Base64Utils.decode(cs.getString(2)));
			 BigInteger modulus = new BigInteger(Base64Utils.decode(cs.getString(3)));
			 System.out.println("contract_pk_exp= "+pk_exp);
			 System.out.println("contract_modulus= "+modulus);
			 
			 Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	         RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,pk_exp);
	         PublicKey pk = keyFactory.generatePublic(rsaPublicKeySpec);
	         
	         // get token_addr with contract_pk
			 cipher.init(Cipher.DECRYPT_MODE,pk);
			 // block decrypt
			 int inputLen = Base64Utils.decode(enc).length;
		     int offSet = 0;
		     byte[] resultBytes = {};
             byte[] cache = {};
		        while (inputLen - offSet > 0) {
		            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
		                cache = cipher.doFinal(Base64Utils.decode(enc), offSet, MAX_DECRYPT_BLOCK);
		                offSet += MAX_DECRYPT_BLOCK;
		            } else {
		                cache = cipher.doFinal(Base64Utils.decode(enc), offSet, inputLen - offSet);   
		                offSet = inputLen;
		            }
		            resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                    System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
		        }
		        
//			 String dec = new String(cipher.doFinal(Base64Utils.decode(enc)));
		     String dec = new String(resultBytes);
			 System.out.println("dec= "+dec);
			 
			 // dec =  token_addr + "&ENC_ID=" + enc_id;
			 token_addr = dec.split("&ENC_ID=")[0];
			 System.out.println("token_addr= "+token_addr);
			 String enc_id = dec.split("&ENC_ID=")[1];
			 
			 // check if token addr is valid
			 cs = conn.prepareCall("{call addr_to_id(?,?)}");
			 cs.setString(1, token_addr);
			 cs.registerOutParameter(2,Types.INTEGER);
			 cs.execute();
			 int from_id = cs.getInt(2);
			 if (from_id > 0) {  // from_id exists
				 // get token_pk from token_addr and delete this token
				 cs = conn.prepareCall("{call get_token_detail(?,?,?,?,?)}");
				 cs.setString(1, token_addr);
				 cs.registerOutParameter(2,Types.VARCHAR); // pk_exp
				 cs.registerOutParameter(3,Types.VARCHAR); // N
				 cs.registerOutParameter(4,Types.INTEGER); // sender_id
				 cs.registerOutParameter(5,Types.DOUBLE); // send_amount
				 
				 cs.execute();
				 
				 pk_exp = new BigInteger(Base64Utils.decode(cs.getString(2)));
				 modulus = new BigInteger(Base64Utils.decode(cs.getString(3)));
				 System.out.println("token_pk_exp= "+pk_exp);
				 System.out.println("token_modulus= "+modulus);
				 
				 int sender_id = cs.getInt(4);
				 double send_amount = cs.getDouble(5);	
				 
				 if (from_id != sender_id) {
					 cs = conn.prepareCall("{call return_money(?,?)}");
					 cs.setString(1, contract_addr);
					 cs.registerOutParameter(2,Types.INTEGER);  //result
					 cs.execute();
					 if (cs.getInt(2) == 1) {
						 result = "sender error, contract cancelled";
					 }
					 return result;
				 }
				 
				 // restore token_sk
				 rsaPublicKeySpec = new RSAPublicKeySpec(modulus,pk_exp);
	             pk = keyFactory.generatePublic(rsaPublicKeySpec);
	             
				 cipher.init(Cipher.DECRYPT_MODE,pk);
				 String dec_id = new String(cipher.doFinal(Base64Utils.decode(enc_id)));
				 System.out.println("id_dec= "+dec_id);
				 
				 // dec_id = ACCOUNT= + rcver_id;
				 if (dec_id.startsWith("ACCOUNT=")) {
					 int rcver_id2 = Integer.parseInt(dec_id.split("=")[1]);
					 System.out.println("rcver_id2= "+rcver_id2);
					 if (rcver_id2 != rcver_id) {
						 cs = conn.prepareCall("{call return_money(?,?)}");
						 cs.setString(1, contract_addr);
						 cs.registerOutParameter(2,Types.INTEGER);  //result
						 cs.execute();
						 if (cs.getInt(2) == 1) {
							 result = "invalid receiver, contract cancelled";
						 }
					 }
					 
					 else {
						 // compare request_amount & current_amount + send_amount
						 // if not enough
						 if (current_amount + send_amount < request_amount) {
							 
							 // add new record in table contractrecord 
							 cs = conn.prepareCall("{call add_new_token(?,?,?,?)}");
							 cs.setString(1, contract_addr);
							 cs.setInt(2, sender_id);
							 cs.setDouble(3, send_amount);
							 cs.registerOutParameter(4,Types.INTEGER); // result
							 cs.execute();
							 
							 if (cs.getInt(4) == 1) {
								 // update current_amount in table contract
								 cs = conn.prepareCall("{call update_current_amount(?,?,?)}");
								 cs.setString(1,contract_addr);
//								 System.out.println("new_amount= "+new_amount);
								 cs.setDouble(2,current_amount+send_amount);
								 cs.registerOutParameter(3,Types.INTEGER); // result
								 cs.execute();
								 if (cs.getInt(3) == 1) {
									 result = "not enough";
								 }
							 }
						 }
						 // if enough or need return change
						 else {
							 double change = current_amount + send_amount - request_amount;
							 // add money to rcver and return change to sender
							 cs = conn.prepareCall("{call finish_contract(?,?,?,?,?,?)}");
							 cs.setString(1, contract_addr);
							 cs.setDouble(2,request_amount);
							 cs.setInt(3,sender_id);
							 cs.setInt(4, rcver_id);
							 cs.setDouble(5,change);
							 cs.registerOutParameter(6,Types.INTEGER);
							 cs.execute();
							 if (cs.getInt(6) == 1) {
								 result = "enough";
							 }
						 }
					 }
				 }
			 }
			 else {
				 result = "expired token";
			 }
		 } catch (Exception e){
	            e.printStackTrace();
	            result = "decryption error";
	            cs = conn.prepareCall("{call return_money(?,?)}");
				 cs.setString(1, contract_addr);
				 cs.registerOutParameter(2,Types.INTEGER);  //result
				 cs.execute();
				 if (cs.getInt(2) == 1) {
					 result = "invalid token addres, contract cancelled";
				 }
	        }if (cs != null) {
	            try {
	                cs.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return result;
	 }
	 
	 public String cancelcontract(Connection conn, String contract_addr, String enc) throws SQLException {
		 String result = "failed";
		 if (conn == null) {
			result = "connection error";
			return result;
		 }
		 CallableStatement cs = null;
		 try {
			 // get contract_pk from contract_addr
			 cs = conn.prepareCall("{call get_contract_detail(?,?,?,?,?,?)}");
			 cs.setString(1,contract_addr);
			 cs.registerOutParameter(2,Types.VARCHAR);  //pk_exp
			 cs.registerOutParameter(3,Types.VARCHAR);  // N
			 cs.registerOutParameter(4,Types.INTEGER);  // rcver_id
			 cs.registerOutParameter(5,Types.DOUBLE);  // request_amount
			 cs.registerOutParameter(6,Types.DOUBLE);  // current_amount
			 cs.execute();
			 
			 // restore contract_pk
			 if (cs.getString(2) == null) {
				 result = "contract address not found";
				 return result;
			 }
			 
			 BigInteger pk_exp = new BigInteger(Base64Utils.decode(cs.getString(2)));
			 BigInteger modulus = new BigInteger(Base64Utils.decode(cs.getString(3)));
			 System.out.println("contract_pk_exp= "+pk_exp);
			 System.out.println("contract_modulus= "+modulus);
			 
			 Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	         RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,pk_exp);
	         PublicKey pk = keyFactory.generatePublic(rsaPublicKeySpec);
			// decrypty enc -> dec = CANCEL
	         cipher.init(Cipher.DECRYPT_MODE,pk);
			 String cancel_dec = new String(cipher.doFinal(Base64Utils.decode(enc)));
			 System.out.println("cancel_dec= "+cancel_dec);
			 
			 if(cancel_dec.equals("CANCEL")) {
				 cs = conn.prepareCall("{call return_money(?,?)}");
				 cs.setString(1, contract_addr);
				 cs.registerOutParameter(2,Types.INTEGER);  //result
				 cs.execute();
				 if (cs.getInt(2) == 1) {
					 result = "cancelled";
				 }
				 
			 }
			 
		 } catch (Exception e){
	            e.printStackTrace();
	            cs = conn.prepareCall("{call return_money(?,?)}");
				 cs.setString(1, contract_addr);
				 cs.registerOutParameter(2,Types.INTEGER);  //result
				 cs.execute();
				 if (cs.getInt(2) == 1) {
					 result = "decryption error, contract cancelled";
				 }
	        }if (cs != null) {
	            try {
	                cs.close();
	            } catch (SQLException e) {
	                e.printStackTrace();
	            }
	        }
	        return result;
	 }
	 
	 public String changeamount(Connection conn, String contract_addr, String enc) throws SQLException {
		 String result = "failed";
		 if (conn == null) {
			result = "connection error";
			return result;
		 }
		 CallableStatement cs = null;
		 try {
			 // get contract_pk from contract_addr
			 cs = conn.prepareCall("{call get_contract_detail(?,?,?,?,?,?)}");
			 cs.setString(1,contract_addr);
			 cs.registerOutParameter(2,Types.VARCHAR);  //pk_exp
			 cs.registerOutParameter(3,Types.VARCHAR);  // N
			 cs.registerOutParameter(4,Types.INTEGER);  // rcver_id
			 cs.registerOutParameter(5,Types.DOUBLE);  // request_amount
			 cs.registerOutParameter(6,Types.DOUBLE);  // current_amount
			 cs.execute();
			 
			 double current_amount = cs.getDouble(6);
			 int rcver_id = cs.getInt(4);
			 
			 // restore contract_pk
			 if (cs.getString(2) == null) {
				 result = "contract address not found";
				 return result;
			 }
			 
			 BigInteger pk_exp = new BigInteger(Base64Utils.decode(cs.getString(2)));
			 BigInteger modulus = new BigInteger(Base64Utils.decode(cs.getString(3)));
			 System.out.println("contract_pk_exp= "+pk_exp);
			 System.out.println("contract_modulus= "+modulus);
			 
			 Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		     KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	         RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus,pk_exp);
	         PublicKey pk = keyFactory.generatePublic(rsaPublicKeySpec);
			// decrypty enc -> dec = new_amount
	         cipher.init(Cipher.DECRYPT_MODE,pk);
			 String new_amount = new String(cipher.doFinal(Base64Utils.decode(enc)));
			 double new_request_amount = Double.parseDouble(new_amount);
			 System.out.println("changeamount_dec= "+new_request_amount);
			 
			 // update new request amount
			 cs = conn.prepareCall("{call update_request_amount(?,?,?)}");
			 cs.setString(1,contract_addr);
			 cs.setDouble(2, new_request_amount);
			 cs.registerOutParameter(3,Types.INTEGER); //result
			 cs.execute();
			 
			 if (cs.getInt(3) == 1) {
				 // check if current_money is enough
				 if (current_amount >= new_request_amount) {
					 // find last sender
					 cs = conn.prepareCall("{call find_last_sender(?,?)}");
					 cs.setString(1, contract_addr);
					 cs.registerOutParameter(2,Types.INTEGER);
					 cs.execute();
					 int sender_id = cs.getInt(2);
					 if (sender_id > 0) {
						 double change = current_amount - new_request_amount;
						 // add money to rcver and return change to sender
						 cs = conn.prepareCall("{call finish_contract(?,?,?,?,?,?)}");
						 cs.setString(1, contract_addr);
						 cs.setDouble(2,new_request_amount);
						 cs.setInt(3,sender_id);
						 cs.setInt(4, rcver_id);
						 cs.setDouble(5,change);
						 cs.registerOutParameter(6,Types.INTEGER);
						 cs.execute();
						 if (cs.getInt(6) == 1) {
							 result = "enough for new amount";
						 }
					 } 
				 }
				 else {
					 result = "amount changed";
				 }
				 
			 }
			 
		 } catch (Exception e){
	            e.printStackTrace();
	            cs = conn.prepareCall("{call return_money(?,?)}");
				 cs.setString(1, contract_addr);
				 cs.registerOutParameter(2,Types.INTEGER);  //result
				 cs.execute();
				 if (cs.getInt(2) == 1) {
					 result = "decryption error, contract cancelled";
				 }
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
	 

	 
//	public int addmoney(Connection conn, Integer account_id, Double value) {
//	        if (conn == null) return TYPE_CONN_FAILED;
//	        CallableStatement cs = null;
//	        try {
//	            cs =conn.prepareCall("{call add_balance(?,?,?)}");
//	            cs.setInt(1, account_id);
//	            cs.setDouble(2, value);
//	            cs.registerOutParameter(3, Types.INTEGER);
//	            cs.execute();
////	            Log.d("MainActivity","transaction result: " + cs.getInt(4));
//	            return 1;
//	        } catch (Exception e){
//	            e.printStackTrace();
//	        }if (cs != null) {
//	            try {
//	                cs.close();
//	            } catch (SQLException e) {
//	                e.printStackTrace();
//	            }
//	        }
//	        return TYPE_CONN_FAILED;
//	 }
	
}

