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

import java.util.concurrent.ConcurrentHashMap;

/**
 *  All the torrents
 */
public class Torrents extends ConcurrentHashMap<InfoHash, Peers> {

    public Torrents() {
        super();
    }

    public int countPeers() {
        int rv = 0;
        for (Peers p : values()) {
             rv += p.size();
        }
        return rv;
    }
}
