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

import java.io.UnsupportedEncodingException;

import net.i2p.data.ByteArray;

/**
 *  A 20-byte SHA1 info hash
 */
public class InfoHash extends ByteArray {

    public InfoHash(String data) throws UnsupportedEncodingException {
        this(data.getBytes("ISO-8859-1"));
    }

    public InfoHash(byte[] data) {
        super(data);
        if (data.length != 20)
            throw new IllegalArgumentException("Bad infohash length: " + data.length);
    }
}
