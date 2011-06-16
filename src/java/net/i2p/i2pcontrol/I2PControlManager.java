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
public class I2PControlManager implements ManagerInterface{

	private static StringBuilder _history;
	
    /**
     *  This is effectively an instance of this class (although actually it may be instead a
     *  java.lang.reflect.Proxy wrapping an instance from the original classloader).
     */
    public static ManagerInterface instance = null;
    /**
     * Retrieve an instance of AbsoluteSingleton from the original classloader. This is a true
     * Singleton, in that there will only be one instance of this object in the virtual machine,
     * even though there may be several copies of its class file loaded in different classloaders.
     */
    public synchronized static ManagerInterface getInstance() {
        ClassLoader myClassLoader = I2PControlManager.class.getClassLoader();
        if (instance==null) {
            // The root classloader is sun.misc.Launcher package. If we are not in a sun package,
			// we need to get hold of the instance of ourself from the class in the root classloader.
            if (! myClassLoader.toString().startsWith("sun.")) {
                try {
                    // So we find our parent classloader
                    ClassLoader parentClassLoader = I2PControlManager.class.getClassLoader().getParent();
                    // And get the other version of our current class
                    Class otherClassInstance = parentClassLoader.loadClass(I2PControlManager.class.getName());
                    // And call its getInstance method - this gives the correct instance of ourself
                    Method getInstanceMethod = otherClassInstance.getDeclaredMethod("getInstance", new Class[] { });
                    Object otherAbsoluteSingleton = getInstanceMethod.invoke(null, new Object[] { } );
                    // But, we can't cast it to our own interface directly because classes loaded from
			// different classloaders implement different versions of an interface.
			// So instead, we use java.lang.reflect.Proxy to wrap it in an object that *does*
			// support our interface, and the proxy will use reflection to pass through all calls
			// to the object.
                    instance = (ManagerInterface) Proxy.newProxyInstance(myClassLoader,
                                                         new Class[] { ManagerInterface.class },
                                                         new PassThroughProxyHandler(otherAbsoluteSingleton));
		// And catch the usual tedious set of reflection exceptions
			// We're cheating here and just catching everything - don't do this in real code
                } catch (Exception e) {
                    e.printStackTrace();
                }
            // We're in the root classloader, so the instance we have here is the correct one
            } else {
                instance = new I2PControlManager();
            }
        }
        return instance;
    }
    
    private I2PControlManager() {
		_history = new StringBuilder();

    }

    private String value = "";
    /* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#getValue()
	 */
    /* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#getValue()
	 */
    public String getValue() { return value; }
    /* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#setValue(java.lang.String)
	 */
    /* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#setValue(java.lang.String)
	 */
    public void setValue(String value) {
        this.value = value;
    }

	
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#prependHistory(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#prependHistory(java.lang.String)
	 */
	public void prependHistory(String str){
		_history.insert(0,str + "<br>\n");
	}
	
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#appendHistory(java.lang.String)
	 */
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#appendHistory(java.lang.String)
	 */
	public void appendHistory(String str){
		_history.append("<br>\n" + str);
	}
	
	
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#getHistory()
	 */
	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingletonInterface#getHistory()
	 */
	public String getHistory(){
		
		return _history.toString();
	}
}
