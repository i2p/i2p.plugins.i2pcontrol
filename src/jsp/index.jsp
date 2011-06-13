<%@page import="net.i2p.i2pcontrol.I2PControlController" %>
<html>
<head>
<title>I2PControl</title>
</head><body style="background-color: #000; color: #c30; font-size: 400%;">
<h1>
I2PControl
</h1>
<table cellspacing="8">
<tr><td>Torrents:<td align="right"><%=I2PControlController.getTorrents().size()%>
<tr><td>Peers:<td align="right"><%=I2PControlController.getTorrents().countPeers()%>
</table>
</body>
</html>
