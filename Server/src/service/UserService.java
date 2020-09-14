package service;

import dao.UserDAO;
import entity.UserInfo;

public class UserService {
	
	UserDAO userDAO = new UserDAO();
	public boolean login(String username, String password) {
		UserInfo userInfo = new UserInfo();
		//userInfo.setUsername("tom");
		//userInfo.setPassword("1");		
		userInfo.setUsername(username);
		userInfo.setPassword(password);
		return userDAO.queryUser(userInfo);
	}
	
}
