package net.i2p.zzzot;
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

import java.util.Iterator;

import net.i2p.util.SimpleScheduler;
import net.i2p.util.SimpleTimer;

/**
 *  Instantiate this to fire it up
 */
class ZzzOT {

    private Torrents _torrents;
    private static final long CLEAN_TIME = 4*60*1000;
    private static final long EXPIRE_TIME = 60*60*1000;

    ZzzOT() {
        _torrents = new Torrents();
        SimpleScheduler.getInstance().addPeriodicEvent(new Cleaner(), CLEAN_TIME);
    }

    Torrents getTorrents() {
        return _torrents;
    }

    void stop() {
        _torrents.clear();
        // no way to stop the cleaner
    }

    private class Cleaner implements SimpleTimer.TimedEvent {

        public void timeReached() {
            long now = System.currentTimeMillis();
            for (Iterator<Peers> iter = _torrents.values().iterator(); iter.hasNext(); ) {
                Peers p = iter.next();
                int recent = 0;
                for (Iterator<Peer> iterp = p.values().iterator(); iterp.hasNext(); ) {
                     Peer peer = iterp.next();
                     if (peer.lastSeen() < now - EXPIRE_TIME)
                         iterp.remove();
                     else
                         recent++;
                }
                if (recent <= 0)
                    iter.remove();
            }
        }
    }
}
