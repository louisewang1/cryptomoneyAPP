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

<jsp:useBean id="crypto" class="Bean.Crypto" scope="page"/>

<% 
int id = Integer.parseInt(request.getParameter("Id"));
System.out.println(id);
String input = null;
input=request.getParameter("Input");

System.out.println(input);
if (input == "") input = null;

//TODO: check validity of input string

if (input != null){
	System.out.println("get input");
	double value = Double.parseDouble(input);
	//CryptoBean crypto = new CryptoBean();
	String addr = crypto.generateQRcontent(value,id);
	String private_key = crypto.getKey();
	String modulus = crypto.getMod();
	String ciphertext = "N="+modulus+"&d="+private_key+"&addr="+addr;
	String new_addr = "QRcode.jsp?Input=" + ciphertext;
	response.sendRedirect(new_addr);
}
%>

</body>
</html>