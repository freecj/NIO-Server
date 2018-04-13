package G8R.serialization;

/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 1 >
* Class: <CSI 4321>
*
************************************************/
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents generic portion of a G8R message and provides
 * serialization/deserialization. This ONLY specifies an API (i.e., minimum
 * required class/method names and parameter types). You may add any classes you
 * wish. You may make any class/method abstract or concrete. You may add any
 * methods (public, protected, or private) to these classes. You may override
 * any methods in the subclasses.
 * 
 * @version 1.0
 */
public class G8RMessage {
	/* request message */
	private static int typeRequest = 0;
	/* response message */
	private static int typeResponse = 1;
	/* wrong type of message */
	private static int other = -1;
	/* MessageDelimiter for getting from next entry */
	private static String MessageDelimiter = "\r\n";
	/* CookieList */
	protected CookieList g8rCookieList;
	/* store function */
	protected String g8rFunction;
	/* use space as delimiter */
	private static String headerSpaceDelimiter = " ";

	/**
	 * Creates a new G8R message by deserializing from the given input according to
	 * the specified serialization.
	 * 
	 * @param in
	 *            user input source
	 * @return new G8R message
	 * @throws ValidationException
	 * @exception java.lang.NullPointerException
	 *                if in is null
	 * @exception java.io.IOException
	 *                if I/O problem
	 */
	public static G8RMessage decode(MessageInput in) throws ValidationException, IOException {
		Objects.requireNonNull(in, "MessageInput is null");

		/* String token for getting from next entry */
		String token = "";
		
		token = in.getNextEntry(headerSpaceDelimiter);
		
		if (token.isEmpty()) {
			throw new EOFException("EOF");
		}
		/* delete space delimiter */
		token = token.substring(0, token.length() - headerSpaceDelimiter.length());

		if (!isValidHeader(token)) {
			/* header GOR\1.0 is wrong */
			throw new ValidationException("wrong header", "wrong header");
		}

		token = in.getNextEntry(headerSpaceDelimiter);
		if (token.isEmpty()) {
			throw new ValidationException("wrong type", "wrong type");
		}
		token = token.substring(0, token.length() - headerSpaceDelimiter.length());

		int type = isValidMessageType(token);
		if (type == other) {
			/* not request or response */
			throw new ValidationException("wrong type", "wrong type");
		}
		if (type == typeRequest) {
			return generateRequestClass(in);

		} else {
			return generateResponseClass(in);
		}

	}

	/**
	 * @param in
	 * @return G8RRequest
	 * @throws ValidationException
	 * @throws IOException
	 */
	public static G8RRequest generateRequestClass(MessageInput in) throws ValidationException, IOException {
		String function = "";
		String token = "";
		token = in.getNextEntry(headerSpaceDelimiter);
		if (token.isEmpty()) {
			throw new ValidationException("wrong Command", "wrong Command");
		}

		token = token.substring(0, token.length() - headerSpaceDelimiter.length());
		if (!isValidRequestCommand(token)) {
			/* command is wrong */
			throw new ValidationException("wrong Command", "wrong Command");
		}

		token = in.getNextEntry(headerSpaceDelimiter, MessageDelimiter);

		if (token.indexOf(headerSpaceDelimiter) != -1) {
			/* delimiter is space */
			token = token.substring(0, token.length() - headerSpaceDelimiter.length());
			if (!isValidFunction(token)) {
				/* function format is wrong */
				throw new ValidationException("wrong function", "Unexpected message");
			}
			function = token;
			List<String> strList = new ArrayList<String>();
			do {

				token = in.getNextEntry(headerSpaceDelimiter, MessageDelimiter);
				if (token.indexOf(headerSpaceDelimiter) != -1) {
					/* delimiter is space */
					token = token.substring(0, token.length() - headerSpaceDelimiter.length());
					if (!isValidFunction(token)) {
						/* param[] format is wrong */
						throw new ValidationException("wrong param", "Param not a proper token (alphanumeric)");
					}
					strList.add(token);
				} else if (token.indexOf(MessageDelimiter) != -1) {
					/* delimiter is \r\n */
					token = token.substring(0, token.length() - MessageDelimiter.length());
					if (!isValidFunction(token)) {
						/* param[] format is wrong */
						throw new ValidationException("wrong param", "Param not a proper token (alphanumeric)");
					}
					strList.add(token);
					break;
				}

			} while (true);

			String[] param = (String[]) strList.toArray(new String[0]);
			CookieList newCookieL = new CookieList(in);
			return new G8RRequest(function, param, newCookieL);

		} else if (token.indexOf(MessageDelimiter) != -1) {
			token = token.substring(0, token.length() - MessageDelimiter.length());
			if (!isValidFunction(token)) {
				/* function format is wrong */
				throw new ValidationException("wrong function", "Unexpected message");
			}
			function = token;
			String[] param = new String[] {};
			CookieList newCookieL = new CookieList(in);
			return new G8RRequest(function, param, newCookieL);
		}
		return null;

	}

	/**
	 * @param in
	 * @return G8RResponse
	 * @throws ValidationException
	 * @throws IOException
	 */
	public static G8RResponse generateResponseClass(MessageInput in) throws ValidationException, IOException {
		String token = "";
		String status = "";
		String function = "";
		String message = "";
		token = in.getNextEntry(headerSpaceDelimiter);
		if (token.isEmpty()) {
			throw new ValidationException("wrong status", "wrong status");
		}
		token = token.substring(0, token.length() - headerSpaceDelimiter.length());

		if (!isValidResponseStatus(token)) {
			/* status format is wrong */
			throw new ValidationException("wrong status", "wrong status");
		}
		status = token;

		token = in.getNextEntry(headerSpaceDelimiter);
		if (token.isEmpty()) {
			/* function format is wrong */
			throw new ValidationException("wrong function", "Unexpected message");
		}

		token = token.substring(0, token.length() - headerSpaceDelimiter.length());
		if (!isValidFunction(token)) {
			/* function format is wrong */
			throw new ValidationException("wrong function", "Unexpected message");
		}
		function = token;

		token = in.getNextEntry(MessageDelimiter);
		if (token.isEmpty()) {
			throw new ValidationException("wrong message", "wrong message");
		}

		token = token.substring(0, token.length() - MessageDelimiter.length());
		if (!isValidMessage(token)) {
			/* message format is wrong */
			throw new ValidationException("wrong message", "wrong message");
		}

		message = token;

		CookieList newCookieL = new CookieList(in);
		return new G8RResponse(status, function, message, newCookieL);

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
	public void encode(MessageOutput out) throws IOException {
	}

	/**
	 * check whether the string valid or not.
	 * 
	 * @param temp
	 *            string to be check
	 * @return if string are alphanumeric, return true. Otherwise false.
	 */
	public boolean isValidString(String temp) {
		String regex = "^[A-Za-z0-9]+$";// use regex to test whether is alphanumeric or not
		return temp.matches(regex);
	}
	
	/**
	 * @param msg
	 *            string to be tested
	 * @return if header is right return yes, otherwise false
	 */
	public static boolean isValidHeader(String msg) {
		String header = "G8R/1.0";
		return header.equals(msg);
	}

	/**
	 * @param msg
	 *            string to be tested
	 * @return if type is right (request or response)return yes, otherwise false
	 */
	public static int isValidMessageType(String msg) {
		String requestStr = "Q";
		String responseStr = "R";
		if (requestStr.equals(msg)) {
			return typeRequest;
		} else if (responseStr.equals(msg)) {
			return typeResponse;
		} else {
			return other;
		}
	}

	/**
	 * @param msg
	 *            string to be tested
	 * @return if command is right return yes, otherwise false
	 */
	public static boolean isValidRequestCommand(String msg) {
		String requestStr = "RUN";
		return requestStr.equals(msg);
	}

	/**
	 * @param msg
	 *            string to be tested
	 * @return if status is right return yes, otherwise false
	 */
	public static boolean isValidResponseStatus(String msg) {
		String statusStrOk = "OK";
		String statusStrError = "ERROR";
		return (statusStrOk.equals(msg) || statusStrError.equals(msg));
	}

	/**
	 * @param msg
	 *            string to be tested
	 * @return if function is right return yes, otherwise false
	 */
	public static boolean isValidFunction(String msg) {
		String regex = "^([A-Za-z0-9]+)$";// use regex to test whether is
		return msg.matches(regex);
	}

	/**
	 * @param msg
	 *            string to be tested
	 * @return if message is printable character return yes, otherwise false
	 */
	public static boolean isValidMessage(String msg) {
		String regex = "^[\\x20-\\x7e]*?$";// use regex to test whether is
		return msg.matches(regex);
	}

	/**
	 * Return function
	 * 
	 * @return function
	 */
	public String getFunction() {
		String result = g8rFunction;
		return result;
	}

	/**
	 * Set function
	 * 
	 * @param function
	 *            new function
	 * @exception java.lang.NullPointerException
	 *                if null command
	 * @exception G8R.serialization.ValidationException
	 *                if invalid command
	 */
	public void setFunction(String function) throws ValidationException {
		Objects.requireNonNull(function, "function is null");

		if (!isValidString(function)) {
			/* function format is wrong */
			throw new ValidationException("function format is wrong", "Unexpected message" );
		}
		g8rFunction = function;
	}

	/**
	 * Return message cookie list
	 * 
	 * @return cookie list
	 */
	public CookieList getCookieList() {
		CookieList ret = new CookieList(g8rCookieList);
		return ret;
	}

	/**
	 * Set cookie list
	 * 
	 * @param cookieList
	 *            cookie list
	 * @exception java.lang.NullPointerException
	 *                if null cookie list
	 */
	public void setCookieList(CookieList cookieList) {
		Objects.requireNonNull(cookieList, "cookieList is null");
		g8rCookieList = new CookieList(cookieList);
	}

}
