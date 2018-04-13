/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 0 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization;

import java.util.Objects;

/**
 * Validation exception containing the token failing validation
 * @version 1.0
 */
public class ValidationException extends Exception {
	private String tokenValidation;

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs validation exception
	 * @param msg exception message
	 * @param token token causing validation failure
	 * @param cause exception cause
	 * @exception java.lang.NullPointerException throw NullPointerException
	 */
	public ValidationException(String msg, String token, Throwable cause) throws NullPointerException {
		super(msg, cause);
		tokenValidation = Objects.requireNonNull(token, "token is null");

	}

	/**
	 * Constructs validation exception
	 * @param msg exception message
	 * @param token token causing validation failure
	 * @exception java.lang.NullPointerException throw NullPointerException
	 */
	public ValidationException(String msg, String token) throws NullPointerException {
		this(msg, token, null);

	}

	/**
	 * Constructs validation exception
	 * @return string 
	 */
	public String getToken() {
		return tokenValidation;
	}
}
