package net.i2p.i2pcontrol;
/*
 *  Copyright 2011 hottuna (dev@robertfoss.se)
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

import net.i2p.I2PAppContext;
import net.i2p.crypto.HMAC256Generator;
import net.i2p.crypto.SHA256Generator;
import net.i2p.util.Log;

/**
 * Manage the password storing for I2PControl.
 */
public class SecurityManager {
	private final int HASH_ITERATIONS = 1000;
	private static final String STORE_FILE = "security.store";
	private static Log _log;
	
	static {
		_log = I2PAppContext.getGlobalContext().logManager().getLog(SecurityManager.class);
		loadStore();
	}
	
	/**
	 * Load password store from default file.
	 */
	private static void loadStore(){
		File store = new File(STORE_FILE);
		if (!store.exists() && !store.canRead())
			_log.debug("Security store, " + STORE_FILE + " doesn't exist or can't be read. Removing password.");
	}
	
	/**
	 * Save security store
	 */
	private void saveStore(){
		
	}
	
	/**
	 * Verifies password against what is stored
	 */
	public boolean verifyPassword(String pwd){
		return false;
	}
	
	/**
	 * Overwrite old password with input
	 */
	public boolean overwritePassword(String pwd){
		return false;
	}
	
	/**
	 * Hash input HASH_ITERATIONS times
	 * @return input hashed HASH_ITERATIONS times
	 */
	private String hashPassword(String pwd){
		SHA256Generator hashGen = new SHA256Generator(I2PAppContext.getGlobalContext());
		byte[] bytes = pwd.getBytes();
		for (int i = 0; i < 1000; i++){
			bytes = hashGen.calculateHash(bytes).toByteArray();
		}
		return new String(bytes);
	}
	


}
