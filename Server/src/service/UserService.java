package service;

import java.sql.Connection;
import java.util.List;

import dao.UserDAO;
import entity.UserInfo;
import util.DBUtil;
import entity.CryptoRecord;
import entity.Record;

public class UserService {
	
	UserDAO userDAO = new UserDAO();
	public int login(Connection conn,String username, String password) {
		UserInfo userInfo = new UserInfo();
		//userInfo.setUsername("tom");
		//userInfo.setPassword("1");		
		userInfo.setUsername(username);
		userInfo.setPassword(password);
//		return userDAO.queryUser(userInfo);
		return userDAO.logincheck(conn,userInfo);
		
	}
	
   public Object[] accountinfo(Connection conn,int account_id) {
	   return userDAO.displayinfo(conn,account_id);
   }
   
   public int register(Connection conn,String username,String password, String email, String cellphone, String pk,String modulus) {
	   return userDAO.accountregister(conn,username,password,email,cellphone,pk,modulus);
   }
   
   public int transfer(Connection conn,int from_account, int to_account, double value) {
	   return userDAO.exetransaction(conn,from_account,to_account,value);
   }
   
   public List<Record> transaction(Connection conn,int account_id) {
	   return userDAO.trandetail(conn,account_id);
   }
   
   public String cryptomoney(Connection conn,int account_id,double value,String modulus) {
	   return userDAO.cryptotransfer(conn,account_id,value,modulus);
   }
   
   public List<CryptoRecord> cryptotransaction(Connection conn,int account_id) {
	   return userDAO.cryptotrandetail(conn,account_id);
   }
   
   public String getcryptomoney(Connection conn, String id_enc,String addr) {
	   return userDAO.getcryptomoney(conn,id_enc,addr);
   }
}
