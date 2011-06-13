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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Date;

import net.i2p.I2PAppContext;
import net.i2p.data.Base32;
import net.i2p.data.DataHelper;
import net.i2p.data.Destination;
import net.i2p.data.PrivateKeyFile;
import net.i2p.util.FileUtil;
import net.i2p.util.I2PAppThread;
import net.i2p.util.Log;


/**
 * This handles the starting and stopping of an eepsite tunnel and jetty
 * from a single static class so it can be called via clients.config.
 *
 * This makes installation of a new eepsite a turnkey operation -
 * the user is not required to configure a new tunnel in i2ptunnel manually.
 *
 * Usage: ZzzOTController -d $PLUGIN [start|stop]
 *
 * @author zzz
 */
public class I2PControlController {
    private static final Log _log = I2PAppContext.getGlobalContext().logManager().getLog(I2PControlController.class);
    private static Object _lock = new Object();

    public static void main(String args[]) {
        if (args.length != 3 || (!"-d".equals(args[0])))
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
        if ("start".equals(args[2]))
            start(args);
        else if ("stop".equals(args[2]))
            stop();
        else
            throw new IllegalArgumentException("Usage: PluginController -d $PLUGIN [start|stop]");
    }
    
    public static String getTestString(){
    	Calendar cal = Calendar.getInstance();
    	int hour = cal.get(Calendar.HOUR_OF_DAY);
    	int minute = cal.get(Calendar.MINUTE);
    	int second = cal.get(Calendar.SECOND);
    	int ms = cal.get(Calendar.MILLISECOND);
    	return hour+":"+minute+":"+second+":"+ms;
    }


    private static void start(String args[]) {
        File pluginDir = new File(args[1]);
        if (!pluginDir.exists())
            throw new IllegalArgumentException("Plugin directory " + pluginDir.getAbsolutePath() + " does not exist");

    }

    private static void stop() {
        // Maybe do things. Perhaps.
    }
}
