<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>PortaPayments - Making Payments Portable</title>
	<link href="http://static.portapayments.com/css/desktop.css" rel="stylesheet" type="text/css">
</head>
<body>
<table width="100%" cellpadding="0" cellspacing="0">
	<tr>
		<td rowspan="2" width="79"><img src="http://static.portapayments.com/images/pp_qrcode.png" width="77" height="77"></td>
		<td bgcolor="003366" class="title"><a href="http://www.portapayments.com/" style="text-decoration: none; color: white">PortaPayments</a></td>
	</tr>
	<tr>
		<td bgcolor="336699" class="strapline">Making Payments Portable</td>
	</tr>
</table>
<div style="padding: 50px">
<div class="roundedcornr_box_456447">
   <div class="roundedcornr_top_456447"><div></div></div>
      <div class="roundedcornr_content_456447" align="center">
      	<% if(request.getAttribute("ErrorMessage") != null) {%>
      		<div style="background: #400; color:#FFF; width:100%; text-align: center"><p><b><%=request.getAttribute("ErrorMessage") %></b></p></div>
      	<% } %>
      	<p>Please enter the amount you wish to pay;</p>
      	<form action="PayUniversalCode" method="GET" style="display:inline">
      		<input type="hidden" name="k" value="<%=request.getParameter("k")%>"/>
      		<input type="hidden" name="i" value="<%=request.getParameter("i")%>"/>
      		<% if(request.getAttribute("Currency") != null) { %>
      			<%=request.getAttribute("Currency")%>
      		<% } %>
      		<input type="text" name="amount" style="text-align:right"/>
      		<input type="submit" value="Continue.."/>
      	</form>&nbsp;<br/>&nbsp;
      </div>
   <div class="roundedcornr_bottom_456447"><div></div></div>
</div>
</div>
<div class="footer">PortaPayments is owned and operated by <a href="http://funkyandroid.com">Funky Android Ltd.</a><br/>
This website and its' contents are (c)Copyright 2010 Funky Android Ltd., All Rights Reserved.</div>
</body>
</html>