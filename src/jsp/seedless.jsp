<%@page import="net.i2p.crypto.SHA256Generator" %><%@page import="net.i2p.data.Base32" %><%@page import="net.i2p.data.Base64" %><%@page import="net.i2p.data.DataHelper" %><%@page import="net.i2p.i2pcontrol.*" %><%

/*
 *  Copyright 2010 zzz (zzz@mail.i2p)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

	String req = request.getHeader("X-Seedless");
	// extension for ease of eepget and browser
	if (req == null)
		req = request.getParameter("X-Seedless");
	// we should really put in our own b32
	String me = request.getHeader("Host");
	if (me == null)
		me = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.b32.i2p";
	// unused, we don't accept announces
	String him = request.getHeader("X-I2P-DestB32");
        String xff = request.getHeader("X-Forwarded-For");
        String xfs = request.getHeader("X-Forwarded-Server");

	response.setContentType("text/plain");
	response.setHeader("X-Seedless", me);

	final int US_MINUTES = 360;
	final int PEER_MINUTES = 60;

	if (xff != null || xfs != null) {
		String msg = "Non-I2P access denied";
	        response.setStatus(403, msg);
		out.println(msg);
	} else if (req == null) {
		// probe
		out.println("tracker " + US_MINUTES);
		out.println("eepsite " + US_MINUTES);
		out.println("seedless " + US_MINUTES);
	} else if (req.startsWith("announce")) {
		out.println("thanks");
	} else if (req.startsWith("locate")) {
		// ignore the search string, if any, in the request
		// us
		out.println(Base64.encode(me + ' ' + US_MINUTES + " tracker"));
		out.println(Base64.encode(me + ' ' + US_MINUTES + " seedless"));
		out.println(Base64.encode(me + ' ' + US_MINUTES + " eepsite"));
		// all the peers
		Torrents torrents = I2PControlController.getTorrents();
		for (InfoHash ihash : torrents.keySet()) {
			Peers peers = torrents.get(ihash);
			if (peers == null)
				continue;
			for (Peer p : peers.values()) {
				// dest to b32
				String ip = (String) p.get("ip");
				if (ip.endsWith(".i2p"))
					ip = ip.substring(0, ip.length() - 4);
				String b32 = Base32.encode(SHA256Generator.getInstance().calculateHash(Base64.decode(ip)).getData()) + ".b32.i2p ";
				// service type
				String role;
				if (p.isSeed())
					role = " bt-seed";
				else
					role = " bt-leech";
				// spg wants UTF-8 but all we have is binary data, so hex it
				String ihs = DataHelper.toHexString(ihash.getData());
				String ids = DataHelper.toHexString((byte[])p.get("peer id"));
				out.println(Base64.encode(b32 + PEER_MINUTES + role +
				                          " info_hash=" + ihs +
				                          ";peer_id=" + ids));
			}
		}
	} else {
		// error code
		out.println("SC_NOT_ACCEPTABLE");
	}

%>