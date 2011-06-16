package net.i2p.i2pcontrol;

public interface ManagerInterface {

	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#getValue()
	 */
	public abstract String getValue();

	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#setValue(java.lang.String)
	 */
	public abstract void setValue(String value);

	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#prependHistory(java.lang.String)
	 */
	public abstract void prependHistory(String str);

	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#appendHistory(java.lang.String)
	 */
	public abstract void appendHistory(String str);

	/* (non-Javadoc)
	 * @see net.i2p.i2pcontrol.SingleTonInterface#getHistory()
	 */
	public abstract String getHistory();

}