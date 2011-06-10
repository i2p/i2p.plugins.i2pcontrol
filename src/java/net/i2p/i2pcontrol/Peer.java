package net.i2p.i2pcontrol;
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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import net.i2p.crypto.SHA256Generator;
import net.i2p.data.Base64;
import net.i2p.data.Destination;
import net.i2p.data.Hash;
import net.i2p.util.SimpleScheduler;
import net.i2p.util.SimpleTimer;

/*
 *  A single peer for a single torrent.
 *  Save a couple stats, and implements
 *  a Map so we can BEncode it
 *  So it's like PeerID but in reverse - we make a Map from the
 *  data. PeerID makes the data from a Map.
 */
public class Peer extends HashMap<String, Object> {

    private long lastSeen;
    private long bytesLeft;
    private static final ConcurrentHashMap<String, String> destCache = new ConcurrentHashMap();
    private static final Integer PORT = Integer.valueOf(6881);
    private static final long CLEAN_TIME = 3*60*60*1000;

    static {
        SimpleScheduler.getInstance().addPeriodicEvent(new Cleaner(), CLEAN_TIME);
    }

    public Peer(byte[] id, Destination address) {
        super(3);
        if (id.length != 20)
            throw new IllegalArgumentException("Bad peer ID length: " + id.length);
        put("peer id", id);
        put("port", PORT);
        // cache the 520-byte address strings
        String dest = address.toBase64() + ".i2p";
	String oldDest = destCache.putIfAbsent(dest, dest);
        if (oldDest != null)
            dest = oldDest;
        put("ip", dest);
    }

    public void setLeft(long l) {
        bytesLeft = l;
        lastSeen = System.currentTimeMillis();
    }

    public boolean isSeed() {
        return bytesLeft <= 0;
    }

    public long lastSeen() {
        return lastSeen;
    }

    /** convert b64.i2p to a Hash, then to a binary string */
    /* or should we just store it in the constructor? cache it? */
    public String getHash() {
        String ip = (String) get("ip");
        byte[] b = Base64.decode(ip.substring(0, ip.length() - 4));
        Hash h = SHA256Generator.getInstance().calculateHash(b);
        try {
            return new String(h.getData(), "ISO-8859-1");
        } catch (UnsupportedEncodingException uee) { return null; }
    }

    private static class Cleaner implements SimpleTimer.TimedEvent {
        public void timeReached() {
            destCache.clear();
        }
    }
}
