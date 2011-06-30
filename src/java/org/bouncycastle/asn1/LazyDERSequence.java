package org.bouncycastle.asn1;

import java.io.IOException;
import java.util.Enumeration;

public class LazyDERSequence
    extends DERSequence
{
    private byte[] encoded;
    private boolean parsed = false;
    private int size = -1;

    LazyDERSequence(
        byte[] encoded)
        throws IOException
    {
        this.encoded = encoded;
    }

    private void parse()
    {
        Enumeration en = new LazyDERConstructionEnumeration(encoded);

        while (en.hasMoreElements())
        {
            addObject((DEREncodable)en.nextElement());
        }

        parsed = true;
    }

    @Override
	public DEREncodable getObjectAt(int index)
    {
        if (!parsed)
        {
            parse();
        }

        return super.getObjectAt(index);
    }

    @Override
	public Enumeration getObjects()
    {
        if (parsed)
        {
            return super.getObjects();
        }

        return new LazyDERConstructionEnumeration(encoded);
    }

    @Override
	public int size()
    {
        if (size < 0)
        {
            Enumeration en = new LazyDERConstructionEnumeration(encoded);

            size = 0;
            while (en.hasMoreElements())
            {
                en.nextElement();
                size++;
            }
        }

        return size;
    }
    
    @Override
	void encode(
        DEROutputStream out)
        throws IOException
    {
        out.writeEncoded(SEQUENCE | CONSTRUCTED, encoded);
    }
}
