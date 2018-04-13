/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 4 >
* Class: <CSI 4321>
*
************************************************/
package N4M.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents generic portion of a N4M message and provides
 * serialization/deserialization. This ONLY specifies an API (i.e., minimum
 * required class/method names and parameter types). You may add any classes you
 * wish. You may make any class/method abstract or concrete. You may add any
 * methods (public, protected, or private) to these classes. You may override
 * any methods in the subclasses.
 */
public abstract class N4MMessage {
	// the maximum value of int, byte,
	protected static int BYTEMASK = 0xff;
	protected static long INTMASK = 4294967295L;

	// header information and mask
	protected static int VERSION = 0X20;
	protected static int RESPONSE_FLAG = 0x8;
	protected static int QUERY_FLAG = 0;
	protected static int VERSION_MASK = 0Xf0;
	protected static int ERRCODE_MASK = 0x7;
	protected static int VERSION_SHIFT = 8;

	// the header information
	protected int msgId;
	protected ErrorCodeType errorCode;

	/**
	 * Creates a new N4M message by deserializing from the given byte array
	 * according to the specified serialization.
	 * 
	 * @param in
	 *            buffer of received packet
	 * @return new N4M message
	 * @throws N4MException
	 *             if validation fails such as bad version, incorrect packet size,
	 *             etc. with error code types: header problems (INCORRECTHEADER) too
	 *             many/few bytes (BADMSGSIZE)
	 * @throws NullPointerException
	 *             if in is null
	 */
	public static N4MMessage decode(byte[] in) throws N4MException, NullPointerException {
		Objects.requireNonNull(in, "byte[] is null");
		ByteArrayInputStream bs = new ByteArrayInputStream(in);
		DataInputStream input = new DataInputStream(bs);
		N4MMessage ret = null;
		try {
			int version = input.readUnsignedByte();
			if ((version & VERSION_MASK) != VERSION) {
				// version is not right
				throw new N4MException("incorrect version bad header", ErrorCodeType.INCORRECTHEADER);
			}
			int msgId = input.readUnsignedByte();
			if ((version & RESPONSE_FLAG) == RESPONSE_FLAG) {
				// response
				ret = new N4MResponse(input, ErrorCodeType.valueOf(version & ERRCODE_MASK), msgId);
			} else {
				// query
				if ((version & ERRCODE_MASK) != ErrorCodeType.NOERROR.getErrorCodeNum()) {
					throw new N4MException("bad header", ErrorCodeType.INCORRECTHEADER);
				}
				ret = new N4MQuery(input, msgId);
			}

		} catch (EOFException e) {
			throw new N4MException("msg is too short/too long", ErrorCodeType.BADMSGSIZE);
		} catch (IOException e) {

		}
		try {
			if (input.readByte() != -1) {
				throw new N4MException("msg is too short/too long", ErrorCodeType.BADMSGSIZE);
			}
		} catch (EOFException e) {

		} catch (IOException e) {

		}
		return ret;

	}

	/**
	 * Return encoded N4M head string
	 * 
	 * @return message encoded in byte array
	 */
	public byte[] encode() {

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(buf);
		int header = VERSION;
		header |= errorCode.getErrorCodeNum();
		header <<= VERSION_SHIFT;
		header |= msgId & BYTEMASK;
		byte[] msg = null;
		try {
			out.writeShort(header);
			out.flush();
			msg = buf.toByteArray();
		} catch (IOException e) {

		}
		return msg;
	}

	/**
	 * Set the message ID
	 * 
	 * @param msgId
	 *            new message ID
	 * @throws N4MException
	 *             if validation fails(BADMSG)
	 */
	public void setMsgId(int msgId) throws N4MException {
		if (msgId < 0 || msgId > BYTEMASK) {
			// the msgid is not in the range
			throw new N4MException("msgId is not in the range", ErrorCodeType.BADMSG);
		}
		this.msgId = msgId;
	}

	/**
	 * Return message ID
	 * 
	 * @return message ID
	 */
	public int getMsgId() {
		return msgId;
	}

	/**
	 * Return error code 
	 * 
	 * @return error code 
	 */
	public ErrorCodeType getErrorCode() {
		return errorCode;
	}

	/**
	 * Set error code 
	 * 
	 * @param errorCode
	 *            ErrorCodeType enum
	 * @throws N4MException
	 *             if validation fails (INCORRECTHEADER)
	 * @throws NullPointerException
	 *             if error code is null
	 */
	public void setErrorCode(ErrorCodeType errorCode) throws N4MException,NullPointerException {
		Objects.requireNonNull(errorCode, "errorCodeNum is null");
		int num = errorCode.getErrorCodeNum();
		if (num < ErrorCodeType.NOERROR.getErrorCodeNum() || num > ErrorCodeType.SYSTEMERROR.getErrorCodeNum()) {
			// the errorCodeNum is not in the range
			throw new N4MException("errorCode is not in the range", ErrorCodeType.INCORRECTHEADER);
		}
		this.errorCode = errorCode;
	}

	@Override
	public String toString() {
		String str = null;
		str = String.format("msgId=%d, errorCodeNum=%d, ", msgId, errorCode.getErrorCodeNum());
		return str;
	}

	/**
	 * return the hashcode of the object
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		return Integer.valueOf(msgId).hashCode() + errorCode.hashCode();
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
		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			return false;
		} else {
			N4MMessage test = (N4MMessage) obj;
			return this.errorCode.equals(test.errorCode) && this.msgId == test.msgId;
		}

	}
}
