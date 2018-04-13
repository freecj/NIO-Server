/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 0 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization;

import java.io.EOFException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * List of cookies (name/value pairs)
 * @version 1.0
 */
public class CookieList {
	/**
	 * use treemap to store the name/value pair 
	 */
	private TreeMap<String, String> cookieMap;
	private final String cookieDelimter = "\r\n";
	private final String cookieToken = "=";

	/**
	 * check whether the name value is valid or not.
	 * @param temp string to be check
	 * @return if string are alphanumeric, return true. Otherwise false.
	 */
	private boolean isValidCookie(String temp) {
		String regex = "^[A-Za-z0-9]+$";// use regex to test whether is alphanumeric or not
		return temp.matches(regex);
	}

	/**
	 * check whether the format of name=value is valid or not.
	 * @param temp string to be check
	 * @return if format is right, return true. Otherwise false.
	 */
	private boolean isValidCookieDecode(String temp) {
		String regex = "^([A-Za-z0-9]+=[A-Za-z0-9]+)$";

		return temp.matches(regex);
	}

	/**
	 * Creates a new, empty cookie list
	 */
	public CookieList() {
		cookieMap = new TreeMap<String, String>();
	}

	/**
	 * Creates a new, cloned cookie list
	 * @param cookieList list of cookies to clone
	 * @exception java.lang.NullPointerException if cookies is null
	 */
	public CookieList(CookieList cookieList) {
		if (cookieList == null) {
			/* CookieList is null */
			throw new NullPointerException("cookieList is null");
		}
		cookieMap = new TreeMap<String, String>(cookieList.cookieMap);
	}

	/**
	 * Creates a new CookieList by deserializing from the given input according to the specified serialization.
	 * @param in input stream from which to deserialize the name/value list
	 * @exception ValidationException if validation problem such as illegal name and/or value, etc.
	 * @exception java.io.IOException if I/O problem (EOFException for EoS)
	 * @exception java.lang.NullPointerException if input stream is null
	 */
	public CookieList(MessageInput in) throws ValidationException, IOException {
		if (in == null) {
			/* MessageInput is null */
			throw new NullPointerException("MessageInput is null");
		}
		try {
			String token = "";
			String delimiter = "\r\n";
			cookieMap = new TreeMap<String, String>();

			boolean formatWrong = false;
			do {
				token = in.getNextEntry(cookieDelimter);
				if (token.isEmpty()) {
					if (formatWrong) {
						/* format is wrongl */
						throw new ValidationException("cookie's value or name is not alphanumeric.", "Cookie Value not a proper token (alphanumeric)" );
					} else {
						throw new EOFException("EOF");
					}

				} else if (token.indexOf(delimiter) == -1) {
					/* cannot find the delimiter */
					throw new ValidationException("cookie's value or name is not alphanumeric.", "Cookie Value not a proper token (alphanumeric)");

				} else {
					token = token.substring(0, token.length() - 2);
				}
				if (!token.isEmpty()) {

					if (!isValidCookieDecode(token)) {
						/* format is wrong */
						formatWrong = true;
					} else {
						if (!formatWrong) {
							/* format is right */
							String[] mapCookies = token.split(cookieToken);
							cookieMap.put(mapCookies[0], mapCookies[1]);
						}

					}
				}
			} while (!token.isEmpty());
			if (formatWrong) {
				throw new ValidationException("cookie's value or name is not alphanumeric.", "Cookie Value not a proper token (alphanumeric)");
			}
		} catch (EOFException e) {
			/* getNextEntry() has EOFException */
			throw e;
		} catch (IOException e) {
			/* getNextEntry() has IOException */
			throw e;
		}
	}

	/**
	 * Creates a new CookieList by decoding from the console (user) Do not implement this yet
	 * @param in console input source
	 * @param out prompt output sink
	 * @exception java.lang.NullPointerException if in or out is null
	 */
	// public CookieList(java.util.Scanner in, java.io.PrintStream out) {
	// // Do not implement this yet
	// }

	/**
	 * Encode the name-value list. The name-value pair serialization must be in sort order (alphabetically by name in increasing order). For example, a=1 b=2 ...
	 * @param out serialization output sink
	 * @exception java.io.IOException if I/O problem
	 * @exception java.lang.NullPointerException if out is null
	 */
	public void encode(MessageOutput out) throws IOException {
		if (out == null) {
			/* MessageOutput is null */
			throw new NullPointerException("MessageOutput is null");
		}
		String result = "";
		String nameCookie = null;
		String valueCookie = null;
		Iterator<String> iterCookie = cookieMap.keySet().iterator();
		while (iterCookie.hasNext()) {
			nameCookie = (String) iterCookie.next(); // get name
			valueCookie = (String) cookieMap.get(nameCookie); // get value
			result = (nameCookie + "=" + valueCookie + "\r\n"); // one cookie

			out.write(result);
		}
		result = "\r\n"; // cookielist ending

		out.write(result);

		// outputstream flush
		out.flush();
	}

	/**
	 * Adds the name/value pair. If the name already exists, the new value replaces the old value
	 * @param name name to be added
	 * @param value value to be associated with the name
	 * @exception ValidationException if validation failure for name or value
	 * @exception java.lang.NullPointerException if name or value is null
	 */
	public void add(String name, String value) throws ValidationException {
		if (name == null) {
			/* name is null, throw exception */
			throw new NullPointerException("name is null");
		}
		if (value == null) {
			/* value is null, throw exception */
			throw new NullPointerException("value is null");
		}
		if (!isValidCookie(name)) {
			/* name content is invalid, throw ValidationException */
			throw new ValidationException("name is not alphanumeric", name);
		}
		if (!isValidCookie(value)) {
			/* value content is invalid, throw ValidationException */
			throw new ValidationException("value is not alphanumeric", value);
		}

		cookieMap.put(name, value); // insert name/value
	}

	/**
	 * Returns string representation of cookie list. The name/value pair serialization must be in sort order (alphabetically by name in increasing order). For example, Cookies=[a=1,b=2] You must follow the spacing, etc. precisely. If there are no cookies, encode as Cookies=[] Note: Space (or lack thereof) is important
	 * @return string representation of cookie list
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder("");
		String nameCookie = null; // name
		String valueCookie = null; // value
		Iterator<String> iterCookie = cookieMap.keySet().iterator();// cookieMap's Iterator for loop
		while (iterCookie.hasNext()) {
			nameCookie = (String) iterCookie.next(); // get name
			valueCookie = (String) cookieMap.get(nameCookie); // get value
			// one cookie
			result.append(nameCookie);
			result.append("=");
			result.append(valueCookie);
			result.append(",");
		}

		String ret = result.toString();
		if (ret.length() == 0) {
			return "Cookies=[]";
		}
		ret = "Cookies=[" + ret.substring(0, ret.length() - 1) + "]";// delete the last ","
		return ret;
	}

	/**
	 * Gets the set of names
	 * @return Set (potentially empty) of names (strings) for this list
	 */
	public Set<String> getNames() {
		Set<String> keys = new TreeSet<>(cookieMap.keySet()); // get name set
		return keys;
	}
	/**
	 * find
	 * @param str 
	 * @return true if find the str in the nameset.
	 */
	public boolean findName(String str) {
		Iterator<String> iterCookie = cookieMap.keySet().iterator();// cookieMap's Iterator for loop
		String nameCookie = ""; // name
		while (iterCookie.hasNext()) {
			nameCookie = (String) iterCookie.next(); // get name
			if (nameCookie.equals(str)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the value associated with the given name
	 * @param name cookie name
	 * @return Value associated with the given name or null if no such name
	 */
	public String getValue(String name) {
		return cookieMap.get(name);
	}

	/**
	 * return the hashcode of the object
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		return Objects.hash(cookieMap);
	}

	/**
	 * test whether two CookieList equal or not
	 * @param obj one object to be tested
	 * @return true means same, otherwise different
	 */
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			result = false;
		} else {
			CookieList testedCookList = (CookieList) obj;
			if (this.cookieMap.equals(testedCookList.cookieMap)) {
				/* two object's cookieMap equal */
				result = true;
			}
		}
		return result;
	}
}
