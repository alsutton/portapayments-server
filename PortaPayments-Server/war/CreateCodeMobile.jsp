<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>PortaPayments - Making Payments Portable</title>
<link href="http://static.portapayments.com/css/mobile.css" rel="stylesheet" type="text/css">
</head>
<body>
<div class="title">PortaPayments</div>
<div class="mobilelink"><a href="http://portapayments.mobi/index.html">&lt;- Back</a></div>
<center>
<img src="CreateCode?a0=<%=request.getParameter("a0")%>&c=<%=request.getParameter("c")%>&r0=<%=request.getParameter("r0")%>" width="192" height="192">
</center>
<div class="footer">PortaPayments is owned and operated by <a href="http://funkyandroid.com">Funky Android Ltd.</a><br/>
This website and its' contents are (c)Copyright 2010 Funky Android Ltd., All Rights Reserved.</div>
</body>
</html>