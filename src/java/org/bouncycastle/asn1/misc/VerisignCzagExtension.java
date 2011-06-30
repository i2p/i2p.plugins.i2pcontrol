package org.bouncycastle.asn1.misc;

import org.bouncycastle.asn1.*;

public class VerisignCzagExtension
    extends DERIA5String
{
    public VerisignCzagExtension(
        DERIA5String str)
    {
        super(str.getString());
    }

    @Override
	public String toString()
    {
        return "VerisignCzagExtension: " + this.getString();
    }
}
