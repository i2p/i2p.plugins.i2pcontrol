<%@page 
import="net.i2p.i2pcontrol.*"
%>
<html>
<head>
<title>I2PControl</title>
</head><body style="background-color: #000; color: #c30; font-size: 250%;">
<h2>
I2PControl
</h2>
<table cellspacing="8">
<tr><td><h3>History:</h3><td align="right"><%
ManagerInterface _manager = I2PControlManager.getInstance();
out.print( _manager.getHistory() );
</table>
</body>
</html>
