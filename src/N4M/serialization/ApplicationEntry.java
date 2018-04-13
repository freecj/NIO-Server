/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 4 >
* Class: <CSI 4321>
*
************************************************/
package N4M.serialization;

import java.util.Objects;

/**
 * Represents one application and its access count
 */
public class ApplicationEntry {
	// information of the entry
	private String applicationName;
	private int accessCt;
	/**
	 * Unsigned byte maximum
	 */
	public static final int BYTEMASK = 0xff;
	/**
	 * Unsigned short maximum
	 */
	public static final int SHORTMASK = 0xffff;

	/**
	 * Create application entry
	 * 
	 * @param applicationName
	 *            name of application
	 * @param accessCt
	 *            application access count
	 * @throws N4MException
	 *             if validation fails
	 * @throws NullPointerException
	 *             if applicationName is null
	 */
	public ApplicationEntry(String applicationName, int accessCt) throws N4MException, NullPointerException {
		setApplicationName(applicationName);
		setAccessCount(accessCt);
	}

	/**
	 * Return human-readable representation
	 * 
	 * @return human readable string
	 */
	@Override
	public String toString() {
		String str = null;
		str = String.format("applicationName=%s, Use Conut=%d", applicationName, accessCt);
		return str;

	}

	/**
	 * return the hashcode of the object
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		return applicationName.hashCode() + Integer.valueOf(accessCt).hashCode();

	}

	/**
	 * test whether two objects equal or not
	 * 
	 * @param obj
	 *            one object to be tested
	 * @return true means same, otherwise different
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			result = false;
		} else {
			ApplicationEntry test = (ApplicationEntry) obj;
			return this.applicationName.equals(test.applicationName) && this.accessCt == test.accessCt;
		}
		return result;

	}

	/**
	 * Return application access count
	 * 
	 * @return access count
	 */
	public int getAccessCount() {
		return accessCt;
	}

	/**
	 * Set application access count
	 * 
	 * @param accessCount
	 *            access count
	 * @throws N4MException
	 *             if validation fails(BADMSG)
	 */
	public void setAccessCount(int accessCount) throws N4MException {
		if (accessCount < 0 || accessCount > SHORTMASK) {

			throw new N4MException("accessCount is not in the unsigned short range", ErrorCodeType.BADMSG);
		}

		accessCt = accessCount;
	}

	/**
	 * Returns application name
	 * 
	 * @return application name
	 */
	public String getApplicationName() {
		return applicationName;
	}

	/**
	 * Set application name
	 * 
	 * @param applicationName
	 *            application name
	 * @throws N4MException
	 *             if validation fails such as name too long (BADMSG)
	 * @throws NullPointerException
	 *             if applicationName is null
	 */
	public void setApplicationName(String applicationName) throws N4MException, NullPointerException {
		Objects.requireNonNull(applicationName, "applicationName is null");
		if (applicationName.length() > BYTEMASK) {
			// test whether applicationName is in the right range
			throw new N4MException("applicationName length is not in the unsigned byte range", ErrorCodeType.BADMSG);
		}
		if (!isValidName(applicationName)) {
			// test whether applicationName is not ASCII
			throw new N4MException("applicationName is not ASCII", ErrorCodeType.BADMSG);
		}
		this.applicationName = applicationName;
	}

	/**
	 * check whether the msg is ASCII or not.
	 * 
	 * @param temp
	 *            string to be check
	 * @return if string are ASCII, return true. Otherwise false.
	 */
	private boolean isValidName(String temp) {
		String regex = "^[\\x00-\\x7f]*$";// use regex to test whether is printable character or not
		return temp.matches(regex);
	}
}
