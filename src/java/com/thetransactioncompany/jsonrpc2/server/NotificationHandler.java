package com.thetransactioncompany.jsonrpc2.server;


import com.thetransactioncompany.jsonrpc2.*;


/**
 * Interface for handling JSON-RPC 2.0 notifications.
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.3 (2011-03-05)
 */
public interface NotificationHandler {

	
	/**
	 * Gets the JSON-RPC 2.0 notification method names that this handler 
	 * processes.
	 *
	 * @return The method names of the served JSON-RPC 2.0 notifications.
	 */
	public String[] handledNotifications();
	
	
	/**
	 * Processes a JSON-RPC 2.0 notification.
	 *
	 * @param notification    A valid JSON-RPC 2.0 notification instance.
	 * @param notificationCtx Context information about the notification,
	 *                        may be {@code null} if undefined.
	 */
	public void process(final JSONRPC2Notification notification, final MessageContext notificationCtx);

}
