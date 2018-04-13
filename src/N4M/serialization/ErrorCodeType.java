package N4M.serialization;
/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 4 >
* Class: <CSI 4321>
*
************************************************/

/**
 * Error Code enumerated type
 *
 */
public enum ErrorCodeType {

	/**
	 * No error(client error code is always 0)
	 */
	NOERROR(0),
	/**
	 * Incorrect version
	 */
	INCORRECTHEADER(1),
	/**
	 * Message too short/long
	 */
	BADMSGSIZE(2),
	/**
	 * Bad message
	 */
	BADMSG(3),
	/**
	 * System error
	 */
	SYSTEMERROR(4);

	int errorCodeNum;

	ErrorCodeType(int errorCodeNum) {
		this.errorCodeNum = errorCodeNum;
	}

	/**
	 * Return error code number corresponding to the error code
	 * 
	 * @return error code number
	 */
	public int getErrorCodeNum() {
		return errorCodeNum;
	}

	/**
	 * Return error code corresponding to the error code number
	 * 
	 * @param errorCodeNum
	 *            error code number to find
	 * @return corresponding error code
	 * @throws N4MException
	 *             if invalid error code number
	 */
	public static ErrorCodeType valueOf(int errorCodeNum) throws N4MException {
		if (errorCodeNum >= 0 && errorCodeNum <= 4) {
			// errorCodeNum is in the range[0,4]
			for (ErrorCodeType type : ErrorCodeType.values()) {
				if (type.getErrorCodeNum() == errorCodeNum) {
					return type;
				}
			}
		} else {
			throw new N4MException("invalid error code number", INCORRECTHEADER);
		}
		return null;
	}

}
