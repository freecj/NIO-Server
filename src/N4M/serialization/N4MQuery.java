/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 4 >
* Class: <CSI 4321>
*
************************************************/
package N4M.serialization;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents an N4M query and provides serialization/deserialization
 *
 */
public class N4MQuery extends N4MMessage {
	private String businessName;

	/**
	 * Constructor for decoding
	 * 
	 * @param input
	 *            buffer of received packet
	 * @param msgId
	 * @throws IOException
	 * @throws N4MException
	 *             if validation fails (e.g., bad version, incorrect packet size,
	 *             etc.)
	 */
	protected N4MQuery(DataInputStream input, int msgId) throws IOException, N4MException {

		int bussinessNameLength = input.readUnsignedByte();
		String businessName = "";
		for (int i = 0; i < bussinessNameLength; ++i) {
			businessName += (char) input.readByte();
		}
		setBusinessName(businessName);
		setMsgId(msgId);
		setErrorCode(ErrorCodeType.NOERROR);

	}

	/**
	 * Creates a new N4M query using given values
	 * 
	 * @param msgId
	 *            message ID
	 * @param businessName
	 *            business name
	 * @throws N4MException
	 *             if validation fails(BADMSG)
	 * @throws NullPointerException
	 */
	public N4MQuery(int msgId, String businessName) throws N4MException, NullPointerException {
		setBusinessName(businessName);
		setMsgId(msgId);
		setErrorCode(ErrorCodeType.NOERROR);
	}

	/**
	 * check whether the msg is valid or not.
	 * 
	 * @param temp
	 *            string to be check
	 * @return if string are printable, return true. Otherwise false.
	 */
	private boolean isValidName(String temp) {
		String regex = "^[\\x00-\\x7f]*$";// use regex to test whether is printable character or not
		return temp.matches(regex);
	}

	/**
	 * Return business name
	 * 
	 * @return business name
	 */
	public String getBusinessName() {
		return businessName;

	}

	@Override
	public String toString() {
		String str = null;
		str = String.format("businessName:%s", businessName);

		return super.toString() + str;
	}

	/**
	 * Set business name
	 * 
	 * @param businessName
	 *            business name
	 * @throws N4MException
	 *             if validation fails (BADMSG)
	 * @throws NullPointerException
	 *             if business name is null
	 */
	public void setBusinessName(String businessName) throws N4MException, NullPointerException {
		Objects.requireNonNull(businessName, "businessName is null");

		if (businessName.length() > BYTEMASK) {

			throw new N4MException("businessName is too long", ErrorCodeType.BADMSG);
		}
		if (!isValidName(businessName)) {

			throw new N4MException("businessName is not ASCII", ErrorCodeType.BADMSG);
		}

		this.businessName = businessName;
	}

	/**
	 * Description copied from class: N4MMessage Set error code
	 * 
	 * @param errorCode
	 *            new error code (INCORRECTHEADER)
	 * @throws N4MException
	 *             if validation fails
	 * @throws NullPointerException
	 *             if error code is null
	 */
	@Override
	public void setErrorCode(ErrorCodeType errorCode) throws N4MException {
		Objects.requireNonNull(errorCode, "errorCodeNum is null");
		int num = errorCode.getErrorCodeNum();
		if (num != ErrorCodeType.NOERROR.getErrorCodeNum()) {
			throw new N4MException("errorCodeNum is not in the range", ErrorCodeType.INCORRECTHEADER);
		}
		this.errorCode = errorCode;
	}

	/**
	 * return the hashcode of the object
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode() + businessName.hashCode();
		return hash;

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
		if (!super.equals(obj)) {
			return false;
		}

		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			return false;
		} else {
			N4MQuery test = (N4MQuery) obj;
			return this.businessName.equals(test.businessName);
		}
	}

	/**
	 * Return encoded N4M message
	 * 
	 * @return message encoded in byte array
	 */
	@Override
	public byte[] encode() {
		byte[] header = super.encode();
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		header[0] |= QUERY_FLAG;
		byte[] msg = null;
		try {
			out.writeByte(header[0]);
			out.writeByte(header[1]);
			out.writeByte(businessName.length());

			out.writeBytes(businessName);
			out.flush();

			msg = buf.toByteArray();
		} catch (IOException e) {

		}
		return msg;
	}
}
