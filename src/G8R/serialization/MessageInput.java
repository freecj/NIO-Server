/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 0 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Deserialization input source for messages
 */
public class MessageInput {
	/**
	 * Constructs a new input source from an InputStream
	
	 */
	private BufferedReader bufferedReader;

	/**
	 * Constructs a new input source from an InputStream
	 * 
	 * @param in byte input source
	 * @exception java.lang.NullPointerException if in is null
	 */
	public MessageInput(InputStream in) throws NullPointerException {
		Objects.requireNonNull(in, "InputStream is null");
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII.name()));
		} catch (UnsupportedEncodingException e) {

		}

	}

	/**
	 * Gets a new next CookieList by deserializing from the given input according to the specified serialization.
	 * @param delimiter 
	 * @return String token
	 * @exception ValidationException if validation problem such as illegal name and/or value, etc.
	 * @exception java.io.IOException if I/O problem (EOFException for EoS)
	 * @exception java.lang.NullPointerException if input stream is null
	 */
	public String getNextEntry(String delimiter) throws IOException, NullPointerException, ValidationException {
		int i = 0; // the read length of every time
		String ret = "";
		int index = 0; // represent the number of the string ret
		while ((i = bufferedReader.read()) != -1) {
			
			ret += (char) i;
			index++;
			if (index >= delimiter.length()) {
				/* delete the delimiter */
				if (isValidDlimiter(ret.substring(ret.length() - delimiter.length()), delimiter)) {
					return ret;
				}
			}
		}
		if (ret.isEmpty()) {
			return ret;
		} else {
			throw new ValidationException("wrong format", ret);
		}
	}

	/**
	 * Gets a new next CookieList by deserializing from the given input according to the specified serialization.
	 * @param delimiter 
	 * @param secondDelimiter 
	 * @return String token
	 * @exception ValidationException if validation problem such as illegal name and/or value, etc.
	 * @exception java.io.IOException if I/O problem (EOFException for EoS)
	 * @exception java.lang.NullPointerException if input stream is null
	 */
	public String getNextEntry(String delimiter, String secondDelimiter)
			throws IOException, NullPointerException, ValidationException {
		int i = 0; // the read length of every time
		String ret = "";
		int index = 0; // represent the number of the string ret
		while ((i = bufferedReader.read()) != -1) {
			ret += (char) i;
			index++;
			if (index >= delimiter.length()) {
				/* delete the delimiter */
				if (isValidDlimiter(ret.substring(ret.length() - delimiter.length()), delimiter)) {
					/* test the delimiter equal */
					return ret;
				}
			}
			if (index >= secondDelimiter.length()) {
				/* delete the delimiter */
				if (isValidDlimiter(ret.substring(ret.length() - secondDelimiter.length()), secondDelimiter)) {
					return ret;
				}
			}
		}
		throw new ValidationException("wrong format", ret);

	}

	/**
	 * check string is the format or not
	 * @param test String to be tested
	 * @param delimiter 
	 * @return true if match, otherwise false.
	 */
	public boolean isValidDlimiter(String test, String delimiter) {
		String regex = delimiter;
		return test.matches(regex);

	}

	

}
