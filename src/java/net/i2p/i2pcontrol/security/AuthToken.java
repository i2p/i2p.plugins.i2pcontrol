package net.i2p.i2pcontrol.security;

import java.util.Calendar;
import java.util.Date;

public class AuthToken {
	private static final int VALIDITY_TIME = 1; // Measured in days
	private String id;
	private Date expiry;
	
	public AuthToken(String password){
		String hash = SecurityManager.getPasswdHash(password);
		this.id = SecurityManager.getHash(hash+ Calendar.getInstance().getTime());
		Calendar expiry = Calendar.getInstance();
		expiry.add(Calendar.DAY_OF_YEAR, VALIDITY_TIME);
		this.expiry = expiry.getTime();
	}
	
	public String getId(){
		return id;
	}
	
	/**
	 * Checks whether the AuthToken has expired.
	 * @return True if AuthToken hasn't expired. False in any other case.
	 */
	public boolean isValid(){
		return Calendar.getInstance().before(expiry);
	}
	
	@Override
	public String toString(){
		return id;
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}

}
