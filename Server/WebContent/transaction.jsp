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
            <form action="MoneyTransfer.jsp" method="post">
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
                    <tr>
                        <td align="right">Sender</td>
                        <td align="center"><input type="text" 
                        	name="paramID" style="height: 30px; width: 250px;" /></td>
                    </tr>
                </table>
            <br>
                <input type="submit" value="Transfer">
            </form>
    </center>

</body>
</html>