/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 1 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization;

import java.util.Arrays;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents a G8R request and provides serialization/deserialization
 * 
 * @version 1.0
 */
public class G8RRequest extends G8RMessage {

	/* store param[] */
	private String[] g8rParam;
	/* string delimiter */
	private static String Delimiter = "\r\n";

	/**
	 * Creates a new G8R request using given values
	 * 
	 * @param function
	 *            request function
	 * @param params
	 *            request parameters
	 * @param cookieList
	 *            request cookie list
	 * @exception java.lang.NullPointerException
	 *                if null parameter
	 * @exception G8R.serialization.ValidationException
	 *                if error with given values
	 */
	public G8RRequest(String function, String[] params, CookieList cookieList) throws ValidationException {
		setFunction(function);
		setParams(params);
		setCookieList(cookieList);
	}

	/**
	 * Returns human-readable, complete (i.e., all attribute values), string
	 * representation of G8R request message
	 * 
	 * @return string representation
	 */
	@Override
	public String toString() {
		String result = "g8rrequest: " + g8rFunction;
		StringBuilder ret = new StringBuilder("");
		for (int i = 0; i < g8rParam.length; i++) {
			ret.append(" ");
			ret.append(g8rParam[i]);

		}
		result += " " + ret.toString();
		// result += Delimiter;
		result += g8rCookieList.toString();
		return result;
	}

	/**
	 * Return parameter list
	 * 
	 * @return parameter list
	 */
	public String[] getParams() {
		String[] ret = (String[]) g8rParam.clone();
		return ret;
	}

	/**
	 * Set parameters
	 * 
	 * @param params
	 *            new parameters
	 * @exception java.lang.NullPointerException
	 *                if null array or array element
	 * @exception G8R.serialization.ValidationException
	 *                if invalid params
	 */
	public void setParams(String[] params) throws ValidationException {

		Objects.requireNonNull(params, "params is null");

		for (int i = 0; i < params.length; i++) {
			Objects.requireNonNull(params[i], "params element is null");

			if (!isValidString(params[i])) {
				throw new ValidationException("params format is wrong", "Param not a proper token (alphanumeric)");
			}
		}
		g8rParam = (String[]) params.clone();// deep copy
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
		String header = "G8R/1.0 Q RUN " + g8rFunction;
		out.write(header);

		for (String item : g8rParam) {
			out.write(" ");
			out.write(item);
		}

		out.write(Delimiter);

		g8rCookieList.encode(out);
		// out.flush();
	}

	/**
	 * return the hashcode of the object
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		return Arrays.deepHashCode(g8rParam) + g8rFunction.hashCode() + g8rCookieList.hashCode();
	}

	/**
	 * test whether two CookieList equal or not
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
			G8RRequest test = (G8RRequest) obj;
			if (Arrays.deepEquals(this.g8rParam, test.g8rParam)) {
				return this.g8rFunction.equals(((G8RRequest) obj).g8rFunction)
						&& this.g8rCookieList.equals(((G8RRequest) obj).g8rCookieList);

			}
		}
		return result;
	}

}
