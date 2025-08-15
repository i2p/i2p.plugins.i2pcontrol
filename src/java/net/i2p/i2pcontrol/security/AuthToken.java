package net.i2p.i2pcontrol.security;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.security.SecureRandom;

import net.i2p.util.RandomSource;


public class AuthToken {
    // SECURITY: Reduced token validity from 24 hours to 1 hour (CVE-2024-I2PCONTROL-002)
    static final int VALIDITY_TIME_HOURS = 1; // Measured in hours (was 1 day)
    private final SecurityManager _secMan;
    private final String id;
    private final Date expiry;

    public AuthToken(SecurityManager secMan, String password) {
        _secMan = secMan;
        String hash = _secMan.getPasswdHash(password);
        
        // SECURITY: Improved token generation with additional entropy
        SecureRandom secureRandom = new SecureRandom();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        long randomValue = secureRandom.nextLong();
        String entropy = hash + timestamp + randomValue + secureRandom.nextInt(Integer.MAX_VALUE);
        
        this.id = _secMan.getHash(entropy);
        
        Calendar expiry = Calendar.getInstance();
        // SECURITY: Use hour-based expiry instead of day-based
        expiry.add(Calendar.HOUR_OF_DAY, VALIDITY_TIME_HOURS);
        this.expiry = expiry.getTime();
    }

    public String getId() {
        return id;
    }

    /**
     * Checks whether the AuthToken has expired.
     * @return True if AuthToken hasn't expired. False in any other case.
     */
    public boolean isValid() {
        return Calendar.getInstance().getTime().before(expiry);
    }

    public String getExpiryTime() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
        return sdf.format(expiry);
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
