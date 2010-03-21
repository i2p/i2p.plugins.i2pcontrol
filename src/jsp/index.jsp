<%@page import="net.i2p.zzzot.ZzzOTController" %>
<html>
<head>
<title>ZzzOT</title>
</head><body style="background-color: #000; color: #c30; font-size: 400%;">
<p>
zzzot
<p>
<table cellspacing="8">
<tr><td>Torrents:<td align="right"><%=ZzzOTController.getTorrents().size()%>
<tr><td>Peers:<td align="right"><%=ZzzOTController.getTorrents().countPeers()%>
</table>
</body>
</html>
