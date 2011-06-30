package org.bouncycastle.asn1;

import java.io.IOException;

/**
 * A NULL object.
 */
public abstract class ASN1Null
    extends ASN1Object
{
    public ASN1Null()
    {
    }

    @Override
	public int hashCode()
    {
        return -1;
    }

    @Override
	boolean asn1Equals(
        DERObject o)
    {
        if (!(o instanceof ASN1Null))
        {
            return false;
        }
        
        return true;
    }

    @Override
	abstract void encode(DEROutputStream out)
        throws IOException;

    @Override
	public String toString()
    {
         return "NULL";
    }
}
