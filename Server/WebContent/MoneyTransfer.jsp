<%@ page import="java.sql.*" language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>
<jsp:useBean id="db" class="Bean.DBBean" scope="page"/>
<jsp:useBean id="userv" class="service.UserService" scope="page"/>
<%	
    request.setCharacterEncoding("UTF-8");
    String amount=(String)request.getParameter("amount");//获取transfer页面输入的用户名和金额
    String receiver=(String)request.getParameter("receiver");
    String paramID = request.getParameter("paramID");//paramID从check.jsp中获取
    out.println("Username: "+paramID+"<br/>");
	
    String sql_receiver="select * from logindb where username="+"'"+receiver+"'";//定义一个查询语句
    ResultSet rs1=db.executeQuery(sql_receiver);//执行查询语句
    
    String sql_sender="select * from logindb where username="+"'"+paramID+"'";//定义一个查询语句
    ResultSet rs2=db.executeQuery(sql_sender);//执行查询语句
    
    Integer to_account = (Integer) rs1.getObject("id");
    Integer from_account = (Integer) rs2.getObject("id");
    double value = Double.valueOf(amount);
    
    //此处需要写一个conn传入transfer函数中
            
    userv.transfer(conn, from_account, to_account, value);
%>
</body>
</html>