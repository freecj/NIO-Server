package G8R.serialization;
/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 1 >
* Class: <CSI 4321>
*
************************************************/

import java.io.IOException;
import java.util.Objects;

/**
 * Represents a G8R response and provides serialization/deserialization
 * 
 * @version 1.0
 */
public class G8RResponse extends G8RMessage {

	/* status string */
	private String g8rStatus;
	/* Message string */
	private String g8rMsg;
	/* CRLF delimiter */
	private final String crlfDelimiter = "\r\n";

	/**
	 * check whether the msg is valid or not.
	 * 
	 * @param temp
	 *            string to be check
	 * @return if string are printable, return true. Otherwise false.
	 */
	private boolean isValidMsg(String temp) {
		String regex = "^[\\x20-\\x7e]*$";// use regex to test whether is printable character or not
		return temp.matches(regex);
	}

	/**
	 * Constructs G8R response using given values
	 * 
	 * @param status
	 *            response status
	 * @param function
	 *            request function
	 * @param message
	 *            response message
	 * @param cookies
	 *            response cookie list
	 * @exception java.lang.NullPointerException
	 *                if null parameter
	 * @exception G8R.serialization.ValidationException
	 *                if error with given values
	 */
	public G8RResponse(String status, String function, String message, CookieList cookies) throws ValidationException {

		setStatus(status);
		setFunction(function);
		setMessage(message);
		setCookieList(cookies);
	}

	/**
	 * Returns human-readable, complete (i.e., all attribute values), string
	 * representation of G8R response message
	 * 
	 * @return string representation
	 */
	@Override
	public String toString() {
		String result = "response: " + g8rStatus + " " + g8rFunction + " " + g8rMsg + " " + g8rCookieList.toString();
		return result;
	}

	/**
	 * Return message
	 * 
	 * @return message
	 */
	public String getMessage() {
		String ret = g8rMsg;
		return ret;
	}

	/**
	 * Set message
	 * 
	 * @param message
	 *            new message
	 * @exception java.lang.NullPointerException
	 *                if null message
	 * @exception G8R.serialization.ValidationException
	 *                if invalid message
	 */
	public void setMessage(String message) throws ValidationException {
		Objects.requireNonNull(message, "message is null");

		if (!isValidMsg(message)) {
			throw new ValidationException("message format is wrong", "");
		}
		g8rMsg = message;
	}

	/**
	 * Return status
	 * 
	 * @return status
	 */
	public String getStatus() {
		String ret = g8rStatus;
		return ret;
	}

	/**
	 * Set status
	 * 
	 * @param status
	 * @exception java.lang.NullPointerException
	 *                if null status
	 * @exception G8R.serialization.ValidationException
	 *                if invalid status
	 */
	public void setStatus(String status) throws ValidationException {
		Objects.requireNonNull(status, "status is null");

		if ("OK".equals(status) || "ERROR".equals(status)) {
			g8rStatus = status;
		} else {
			throw new ValidationException("status content is wrong", "");
		}

	}

	/**
	 * Encode the entire G8R message
	 * 
	 * @param out
	 *            serialization output sink
	 * @exception NullPointerException
	 *                if out is null
	 * @exception IOException
	 *                if I/O problem
	 */
	@Override
	public void encode(MessageOutput out) throws IOException {
		Objects.requireNonNull(out, "out is null");
		String header = "G8R/1.0 R " + g8rStatus;
		out.write(header);
		out.write(" ");
		out.write(g8rFunction);
		out.write(" ");
		out.write(g8rMsg);

		out.write(crlfDelimiter);
		out.flush();
		g8rCookieList.encode(out);

	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return g8rStatus.hashCode() + g8rMsg.hashCode() + g8rFunction.hashCode() + g8rCookieList.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			result = false;
		} else {

			return this.g8rMsg.equals(((G8RResponse) obj).g8rMsg) && this.g8rStatus == ((G8RResponse) obj).g8rStatus
					&& this.g8rFunction.equals(((G8RResponse) obj).g8rFunction)
					&& this.g8rCookieList.equals(((G8RResponse) obj).g8rCookieList);
		}
		return result;
		// TODO Auto-generated method stub

	}

}
