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
 * N4M validation exception
 *
 */
public class N4MException extends Exception {
	private ErrorCodeType errorType;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs N4M validation exception
	 * 
	 * @param msg
	 *            exception message (cannot be null)
	 * @param errorCodeType
	 *            type of error(cannot be null)
	 * @param cause
	 *            exception cause
	 * @throws java.lang.NullPointerException
	 *             if msg or errorCodeType is null
	 */
	public N4MException(String msg, ErrorCodeType errorCodeType, Throwable cause) {
		super(msg, cause);
		Objects.requireNonNull(errorCodeType, "errorCodeType is null");
		errorType = errorCodeType;
	}

	/**
	 * Constructs N4M validation exception
	 * 
	 * @param msg
	 *            exception message (cannot be null)
	 * @param errorCodeType
	 *            type of error(cannot be null)
	 * @throws java.lang.NullPointerException
	 *             if msg or errorCodeType is null
	 */
	public N4MException(String msg, ErrorCodeType errorCodeType) {
		this(msg, errorCodeType, null);
	}

	/**
	 * Return error code type
	 * 
	 * @return error code type
	 */
	public ErrorCodeType getErrorCodeType() {
		return errorType;
	}

}
