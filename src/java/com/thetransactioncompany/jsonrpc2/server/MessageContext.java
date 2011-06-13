package com.thetransactioncompany.jsonrpc2.server;


import java.security.Principal;
import java.security.cert.X509Certificate;
import javax.servlet.http.HttpServletRequest;


/**
 * Context information about JSON-RPC 2.0 request and notification messages.
 *
 * <ul>
 *     <li>The client's host name.
 *     <li>The client's IP address.
 *     <li>Whether the request / notification was transmitted securely (e.g. 
 *         via HTTPS).
 *     <li>The client principal (user), if authenticated.
 * </ul>
 *
 * @author <a href="http://dzhuvinov.com">Vladimir Dzhuvinov</a>
 * @version 1.3 (2011-02-27)
 */
public class MessageContext {


	/** 
	 * The client hostname.
	 */
	private String clientHostName = null;

	
	/** 
	 * The client IP address.
	 */
	private String clientInetAddress = null;

	
	/** 
	 * Indicates whether the request was received over HTTPS. 
	 */
	private boolean secure = false;
	
	
	/**
	 * The authenticated client principal.
	 */
	private Principal principal = null;
	
	
	/**
	 * Minimal implementation of the {@link java.security.Principal} 
	 * interface.
	 */
	protected class BasicPrincipal implements Principal {
	
		/**
		 * The principal name.
		 */
		private String name;
	
	
		/**
		 * Creates a new principal.
		 *
		 * @param name The principal name.
		 */
		public BasicPrincipal(final String name) {
		
			this.name = name;
		}
	
	
		/**
		 * Checks for equality.
		 *
		 * @param another The object to compare to.
		 */
		public boolean equals(final Object another) {
		
			if (another instanceof Principal && ((Principal)another).getName().equals(this.getName()))
				return true;
			else
				return false;
		}
		
		
		/**
		 * Returns a hash code for this principal.
		 *
		 * @return The hash code.
		 */
		public int hashCode() {
		
			if (name == null)
				return 0;
			else
				return name.hashCode();
		}
		
		
		/**
		 * Returns the principal name.
		 *
		 * @return The principal name.
		 */
		public String getName() {
			
			return name;
		}
	}
	
	
	/**
	 * Creates a new JSON-RPC 2.0 request / notification context.
	 *
	 * @param clientHostName    The client host name, {@code null} if 
	 *                          unknown.
	 * @param clientInetAddress The client IP address, {@code null} if 
	 *                          unknown.
	 * @param secure            Specifies a request received over HTTPS.
	 * @param principalName     Specifies the authenticated client principle
	 *                          name, {@code null} if none.
	 */
	public MessageContext(final String clientHostName, 
	                      final String clientInetAddress, 
			      final boolean secure, 
			      final String principalName) {
	
		this.clientHostName = clientHostName;
		this.clientInetAddress = clientInetAddress;
		this.secure = secure;
		
		this.principal = new BasicPrincipal(principalName);
	}
	
	
	/**
	 * Creates a new JSON-RPC 2.0 request / notification context. No 
	 * authenticated client principal is specified.
	 *
	 * @param clientHostName    The client host name, {@code null} if 
	 *                          unknown.
	 * @param clientInetAddress The client IP address, {@code null} if 
	 *                          unknown.
	 * @param secure            Specifies a request received over HTTPS.
	 */
	public MessageContext(final String clientHostName, 
	                      final String clientInetAddress, 
			      final boolean secure) {
	
		this(clientHostName, clientInetAddress, secure, null);
	}
	
	
	/**
	 * Creates a new JSON-RPC 2.0 request / notification context. Indicates 
	 * an insecure transport (plain HTTP) and no authenticated client 
	 * principal.
	 *
	 * @param clientHostName    The client host name, {@code null} if 
	 *                          unknown.
	 * @param clientInetAddress The client IP address, {@code null} if 
	 *                          unknown.
	 */
	public MessageContext(final String clientHostName, 
	                      final String clientInetAddress) {
	
		this(clientHostName, clientInetAddress, false, null);
	}
	
	
	/**
	 * Creates a new JSON-RPC 2.0 request / notification context from the
	 * specified HTTP request.
	 *
	 * @param httpRequest The HTTP request.
	 */
	public MessageContext(final HttpServletRequest httpRequest) {
	
		clientInetAddress = httpRequest.getRemoteAddr();
	
		clientHostName = httpRequest.getRemoteHost();
		
		if (clientHostName != null && clientHostName.equals(clientInetAddress))
			clientHostName = null; // not resolved actually
		
		secure = httpRequest.isSecure();

		X509Certificate[] certs = (X509Certificate[])httpRequest.getAttribute("javax.servlet.request.X509Certificate");

		String principalName = null;

		if (certs != null)
			principalName = certs[0].getSubjectDN().getName();
		
		principal = new BasicPrincipal(principalName);
	}
	
	
	/**
	 * Gets the host name of the client that sent the request / 
	 * notification.
	 *
	 * @return The client host name, {@code null} if unknown.
	 */
	public String getClientHostName() {
	
		return clientHostName;
	}
	
	
	/**
	 * Gets the IP address of the client that sent the request /
	 * notification.
	 *
	 * @return The client IP address, {@code null} if unknown.
	 */
	public String getClientInetAddress() {
		
		return clientInetAddress;
	}
	 

	/**
	 * Indicates whether the request / notification was received over a 
	 * secure HTTPS connection.
	 *
	 * @return {@code true} If the request was received over HTTPS, 
	 *         {@code false} if it was received over plain HTTP.
	 */
	public boolean isSecure() {
	
		return secure;
	}
	
	
	/**
	 * Returns the authenticated client principal, {@code null} if none.
	 *
	 * @return The client principal, {@code null} if none.
	 */
	public Principal getPrincipal() {
	
		return principal;
	}
}
