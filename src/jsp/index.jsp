<%@page import="net.i2p.i2pcontrol.ZzzOTController" %>
<html>
<head>
<title>I2PControl - Remote Control Server</title>
</head><body style="background-color: #000; color: #c30; font-size: 400%;">
<p>
I2PControl
<p>
<table cellspacing="8">
<tr><td>Torrents:<td align="right"><%=ZzzOTController.getTorrents().size()%>
<tr><td>Peers:<td align="right"><%=ZzzOTController.getTorrents().countPeers()%>
</table>
</body>
</html>
