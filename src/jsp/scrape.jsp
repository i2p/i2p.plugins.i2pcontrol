<%@page import="java.util.ArrayList" %><%@page import="java.util.List" %><%@page import="java.util.Map" %><%@page import="java.util.HashMap" %><%@page import="net.i2p.zzzot.*" %><%@page import="org.klomp.snark.bencode.BEncoder" %><%

/*
 *  Above one-liner is so there is no whitespace -> IllegalStateException
 *
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

/*
 * USE CAUTION WHEN EDITING
 * Trailing whitespace OR NEWLINE on the last line will cause
 * IllegalStateExceptions !!!
 *
 */
	// so the chars will turn into bytes correctly
	request.setCharacterEncoding("ISO-8859-1");
	java.io.OutputStream cout = response.getOutputStream();
	response.setCharacterEncoding("ISO-8859-1");
	response.setContentType("text/plain");
        response.setHeader("Pragma", "no-cache");
	String info_hash = request.getParameter("info_hash");
        String xff = request.getHeader("X-Forwarded-For");

	boolean fail = false;
	String msg = "bad";

	if (xff != null) {
		fail = true;
		msg = "Non-I2P access denied";
	        response.setStatus(403, msg);
	}

	boolean all = info_hash == null;

	InfoHash ih = null;
	if ((!all) && !fail) {
		try {
			ih = new InfoHash(info_hash);
		} catch (Exception e) {
			fail = true;
			msg = "bad infohash " + e;
		}
	}

        Torrents torrents = ZzzOTController.getTorrents();

	// build 3-level dictionary
	Map<String, Object> m = new HashMap();
	if (fail) {
		m.put("failure reason", msg);		
	} else {
		List<InfoHash> ihList = new ArrayList();
		if (all)
			ihList.addAll(torrents.keySet());
		else
			ihList.add(ih);
		Map<String, Map> files = new HashMap();
		for (InfoHash ihash : ihList) {
			Peers peers = torrents.get(ihash);
			if (peers == null)
				continue;
			Map<String, Object> dict = new HashMap();
			int size = peers.size();
			int seeds = peers.countSeeds();
			dict.put("complete", Integer.valueOf(seeds));
			dict.put("incomplete", Integer.valueOf(size - seeds));
			dict.put("downloaded", Integer.valueOf(0));
			files.put(new String(ihash.getData(), "ISO-8859-1"), dict);
		}
		m.put("files", files);
	}
	BEncoder.bencode(m, cout);

/*
 *  Remove the newline on the last line or
 *  it will generate an IllegalStateException
 *
 */
%>