package net.i2p.i2pcontrol.security;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class AuthToken {
    private static final int VALIDITY_TIME = 1; // Measured in days
    private SecurityManager _secMan;
    private String id;
    private Date expiry;

    public AuthToken(String password){
        _secMan = SecurityManager.getInstance();
        String hash = _secMan.getPasswdHash(password);
        this.id = _secMan.getHash(hash + Calendar.getInstance().getTimeInMillis());
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
        return Calendar.getInstance().getTime().before(expiry);
    }

    public String getExpiryTime(){
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        return sdf.format(expiry);
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
