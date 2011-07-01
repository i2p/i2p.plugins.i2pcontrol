package net.i2p.i2pcontrol.security;

public class ExpiredAuthTokenException extends Exception{

	private static final long serialVersionUID = 2279019346592900289L;

	public ExpiredAuthTokenException(String str){
		super(str);
	}
}
