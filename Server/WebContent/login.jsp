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
        <h1 style="color:blue">登录</h1>
            <form id="indexform" name="indexForm" action="check.jsp" method="post">
                <table border="0">
                    <tr>
                        <td>账号：</td>
                        <td><input type="text" name="username"></td>
                    </tr>
                    <tr>
                        <td>密码：</td>
                        <td><input type="password" name="password"></td>
                    </tr>
                </table>
            <br>
                <input type="submit" value="登录">
            </form>
    </center>
</body>
</html>