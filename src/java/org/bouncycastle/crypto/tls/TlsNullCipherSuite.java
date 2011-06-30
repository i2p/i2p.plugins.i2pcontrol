package org.bouncycastle.crypto.tls;

/**
 * A NULL CipherSuite in java, this should only be used during handshake.
 */
public class TlsNullCipherSuite extends TlsCipherSuite
{

    @Override
	protected void init(byte[] ms, byte[] cr, byte[] sr)
    {
        throw new TlsRuntimeException("Sorry, init of TLS_NULL_WITH_NULL_NULL is forbidden");
    }

    @Override
	protected byte[] encodePlaintext(short type, byte[] plaintext, int offset, int len)
    {
        byte[] result = new byte[len];
        System.arraycopy(plaintext, offset, result, 0, len);
        return result;
    }

    @Override
	protected byte[] decodeCiphertext(short type, byte[] plaintext, int offset, int len, TlsProtocolHandler handler)
    {
        byte[] result = new byte[len];
        System.arraycopy(plaintext, offset, result, 0, len);
        return result;
    }

    @Override
	protected short getKeyExchangeAlgorithm()
    {
        return 0;
    }

}
