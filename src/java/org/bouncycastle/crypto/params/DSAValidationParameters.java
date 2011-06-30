package org.bouncycastle.crypto.params;

import org.bouncycastle.util.Arrays;

public class DSAValidationParameters
{
    private byte[]  seed;
    private int     counter;

    public DSAValidationParameters(
        byte[]  seed,
        int     counter)
    {
        this.seed = seed;
        this.counter = counter;
    }

    public int getCounter()
    {
        return counter;
    }

    public byte[] getSeed()
    {
        return seed;
    }

    @Override
	public int hashCode()
    {
        return counter ^ Arrays.hashCode(seed);
    }
    
    @Override
	public boolean equals(
        Object o)
    {
        if (!(o instanceof DSAValidationParameters))
        {
            return false;
        }

        DSAValidationParameters  other = (DSAValidationParameters)o;

        if (other.counter != this.counter)
        {
            return false;
        }

        return Arrays.areEqual(this.seed, other.seed);
    }
}
