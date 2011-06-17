package net.i2p.i2pcontrol;
/*
 *  Copyright 2010 hottuna (dev@robertfoss.se)
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

import java.lang.reflect.*;

/**
 * There can be only one - ie. even if the class is loaded in several different classloaders,
 * there will be only one instance of the object.
 */
public class I2PControlManager{

	private static StringBuilder _history;
	public static I2PControlManager instance = null;
 
    public synchronized static I2PControlManager getInstance() {
        if (instance == null) {
                instance = new I2PControlManager();
        }
        return instance;
    }
    
    private I2PControlManager() {
		_history = new StringBuilder();
    }


	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#prependHistory(java.lang.String)
	 */
	public void prependHistory(String str){
		_history.insert(0,str + "<br>\n");
	}
	
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#appendHistory(java.lang.String)
	 */
	public void appendHistory(String str){
		_history.append("<br>\n" + str);
	}
	
	
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#getHistory()
	 */
	public String getHistory(){
		
		return _history.toString();
	}
}
