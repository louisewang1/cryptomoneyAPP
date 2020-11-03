<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>Insert title here</title>
</head>
<body>
    <center>
        <h1 style="color:blue">Transaction</h1>
        	<form name="thisform"method="post">
                <table border="5px" width="500px" align="center" height="300px">
                    <tr>
                        <td align="right">Amount</td>
                        <td align="center"><input type="text" 
                        	name="amount" style="height: 30px; width: 250px;" /></td>
                    </tr>
                    <tr>
                        <td align="right">Receiver</td>
                        <td align="center"><input type="text" 
                        	name="receiver" style="height: 30px; width: 250px;" /></td>
                    </tr>
                </table>
            <br>
                <input type="submit" value="Transfer" onclick="sub()">
            </form>
    </center>
    
<script>
function sub(){
document.thisform.submit();
}
</script>

<%@ page import="service.UserService" %>
<%@ page import="util.DBUtil" %>
<%@ page import="java.sql.*" %>

<%
int cur_id = Integer.parseInt(session.getAttribute("Id").toString());

String receiver = request.getParameter("receiver");
String amount = request.getParameter("amount");

System.out.println("receiver " + receiver + "|||||amount " + amount);
boolean f = true;

if (receiver != null || amount != null){
try {
	Double.parseDouble( amount );
}
catch( Exception e ) {
	f = false;
	out.print("<script>alert('Invalid Amount') </script>");
}

try {
	Integer.parseInt( receiver );
}
catch( Exception e ) {
	f = false;
	out.print("<script>alert('Invalid Receiver') </script>");
}

}

if (receiver != null && amount != null && f){



int to_account = Integer.parseInt(receiver);
double value = Double.parseDouble(amount);

UserService userService = new UserService();
Connection conn = DBUtil.getConn();
int res = userService.transfer(conn,cur_id, to_account,value);

final int TYPE_TR_SUCCESS = 1;
final int TYPE_AMOUNT_FAILED = 0;
final int TYPE_ID_FAILED = -2;
final int TYPE_SELF_FAILED = -3;

if (res == TYPE_TR_SUCCESS)
	out.print("<script>alert('Transaction Success!') </script>");
else if (res == TYPE_AMOUNT_FAILED)
	out.print("<script>alert('Not Enough Balance!') </script>");
else if (res == TYPE_ID_FAILED)
	out.print("<script>alert('Invalid ID!') </script>");
else if (res == TYPE_SELF_FAILED)
	out.print("<script>alert('Cannot Transfer to Yourself!') </script>");
else 
	out.print("<script>alert('Unknown Error') </script>");

}


%>
<input type="button" value="Back" onClick="location.href='success.jsp'">
</body>
</html>