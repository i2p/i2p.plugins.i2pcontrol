<%@page import="net.i2p.i2pcontrol.I2PControlController" %>
<html>
<head>
<title>I2PControl</title>
</head><body style="background-color: #000; color: #c30; font-size: 400%;">
<h1>
I2PControl
</h1>
<table cellspacing="8">
<tr><td>Status:<td align="right"><%=I2PControlController.getTestString()%>
<tr><td>Status again:<td align="right"><%=I2PControlController.getTestString()%>
</table>
</body>
</html>
