<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Insert title here</title>
</head>
<body style="text-align:center">

<%@ page import="java.io.FileInputStream" %>
<%@ page import="java.io.ByteArrayOutputStream" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.InputStream" %>
<%@ page import="java.awt.image.BufferedImage" %>
<%@ page import="javax.imageio.ImageIO" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.Hashtable" %>

<%@ page import="com.google.zxing.BarcodeFormat" %>
<%@ page import="com.google.zxing.EncodeHintType" %>
<%@ page import="com.google.zxing.MultiFormatWriter" %>
<%@ page import="com.google.zxing.WriterException" %>
<%@ page import="com.google.zxing.common.BitMatrix" %>
<%
int BLACK = 0xFF000000;  

int WHITE = 0xFFFFFFFF;  

String input=request.getParameter( "Input"); //get text from user

String text = input; 
String exts = ""; 
File f = new File(getServletContext().getRealPath("/")+exts);
		System.out.println(getServletContext().getRealPath("/"));
        int width = 300;  
        int height = 300;  
        //二维码的图片格式  
        String format = "gif";  
        Hashtable hints = new Hashtable();  
        //内容所使用编码  
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");  
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text,BarcodeFormat.QR_CODE, width, height, hints);  
        //生成二维码  
        File outputFile = new File(f+"/core.gif");  

        if(!f.exists()){

f.mkdir();

}

     BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
     for (int x = 0; x < width; x++) {  
       for (int y = 0; y < height; y++) {  
         image.setRGB(x, y, bitMatrix.get(x, y) ? BLACK : WHITE);  
       }  
     }  
     BufferedImage bimage =  image;
     if (!ImageIO.write(image, format, outputFile)) {  
       throw new IOException("Could not write an image of format " + format + " to " + outputFile);  
     } 
     out.println("<div style='text-align:center;'><img src='"+request.getContextPath()+exts+"/core.gif' /><br/>"+text+"<br/>Image Created Success: "+f+"</div>");
%>

<input type="button" value="Generate Again" onClick="window.location.href='QRcodeInput.jsp'">

</body>
</html>