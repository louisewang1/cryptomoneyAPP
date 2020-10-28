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
<input type="text" name="input">
<p/>
<input type="button" value="Submit" onclick="sub()">
</form>

<script>
function sub(){
document.thisform.submit();
}
</script>

<jsp:useBean id="crypto" class="Bean.Crypto" scope="page"/>

<%@ page import="java.lang.Object" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%

int id = Integer.parseInt(session.getAttribute("Id").toString());
//int id = Integer.parseInt(request.getParameter("Id"));
System.out.println("current id is "+id);
String input = null;
input=request.getParameter("input");

System.out.println(input);

boolean f = true; //check if input is valid

if (input == "" || input == null){
	input = null;
	f = false;
}
else {
	try {
    	Double.parseDouble( input );
	}
	catch( Exception e ) {
    	f = false;
    	out.print("<script>alert('Invalid Input') </script>");
	}
}
//TODO: check validity of input string

if (input != null && f == true){
	System.out.println("get input");
	double value = Double.parseDouble(input);
	//CryptoBean crypto = new CryptoBean();
	String addr = crypto.generateQRcontent(value,id);
	if (addr == "not enough balance"){
		out.print("<script>alert('Not Enough Balance') </script>");
	}
	else {
		String private_key = crypto.getKey();
		String modulus = crypto.getMod();
		String ciphertext = "N="+modulus+"&d="+private_key+"&addr="+addr;
		String new_addr = "QRcode.jsp";
		session.setAttribute("Input",ciphertext);  
		response.sendRedirect(new_addr);
	}
}

%>

</body>
</html>