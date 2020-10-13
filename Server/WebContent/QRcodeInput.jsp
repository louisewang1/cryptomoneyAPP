<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>QRcodeGenerator</title>
</head>
<body style="text-align:center">
<h1>
Type your input in the box
</h1>
<form name="thisform"method="post">
<input type="text" name="Input">
<p/>
<input type="button" value="Submit" onclick="sub()">
</form>

<script>
function sub(){
document.thisform.submit();
}
</script>

<% 
String input = null;
input=request.getParameter( "Input");
System.out.println(input);
if (input == "") input = null;

//TODO: check validity of input string

if (input != null){
	System.out.println("get input");
	String temp = "QRcode.jsp?Input=" + input;
	response.sendRedirect(temp);
}
%>

</body>
</html>