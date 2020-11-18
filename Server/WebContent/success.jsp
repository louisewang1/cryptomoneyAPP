<%@ page import="java.sql.*" language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>My Account</title>
</head>
<body style="text-align:center">
<jsp:useBean id="db" class="Bean.DBBean" scope="page"/>
<h1>Login Success</h1>
<%
int id = Integer.parseInt(session.getAttribute("Id").toString());
String paramID = session.getAttribute("Username").toString();

out.println("Username: "+paramID+"<br/>");

String sql="select * from accountinfodb where username="+"'"+paramID+"'";//定义一个查询语句
ResultSet rs=db.executeQuery(sql);//执行查询语句
if(rs.next())
{
	Object bal = rs.getObject("balance");
	double balance = (double)bal;
	out.println("Balance: " + balance + "<br/>");
	out.println("Email: "+rs.getObject("email").toString()+"<br/>");
	out.println("Cellphone: "+rs.getObject("cellphone").toString()+"<br/>");
	int acc_id = rs.getInt("account_id");
	out.println(acc_id+"<br/>");
	session.setAttribute("Id",acc_id);  

}



%>
<p/>

<input type="button" value="Generate QR code" onClick="location.href='QRcodeInput.jsp'">
<input type="button" value="Transfer Money" onClick="location.href='transaction.jsp'">

</body>
</html>