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
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an N4M response and provides serialization/deserialization
 */
public class N4MResponse extends N4MMessage {
	private long timestamp;
	private List<ApplicationEntry> applications = new ArrayList<>();

	/**
	 * Constructor for the decoding
	 * 
	 * @param input
	 * @param errorCodeNum
	 * @param msgId
	 * @throws IOException
	 * @throws N4MException
	 *             if validation fails (e.g., bad version, incorrect packet size,
	 *             etc.)
	 */
	protected N4MResponse(DataInputStream input, ErrorCodeType errorCodeNum, int msgId)
			throws IOException, N4MException {
		long newDate = 0;
		for (int i = 0; i < 4; ++i) {
			int shift = (3 - i) << 3;
			newDate |= ((long) 0xff << shift) & ((long) input.readUnsignedByte() << shift);
		}

		int applicationCount = input.readUnsignedByte();

		List<ApplicationEntry> applicationsReceived = new ArrayList<ApplicationEntry>();
		for (int i = 0; i < applicationCount; ++i) {
			// initialize the application entry
			int applicationUseCount = input.readUnsignedShort();
			int applicationNameLength = input.readUnsignedByte();
			String applicationName = "";
			for (int j = 0; j < applicationNameLength; ++j) {
				applicationName += (char) input.readByte();

			}
			ApplicationEntry newEntry = new ApplicationEntry(applicationName, applicationUseCount);
			applicationsReceived.add(newEntry);
		}

		setMsgId(msgId);
		setErrorCode(errorCodeNum);
		setTimestamp(newDate);
		setApplications(applicationsReceived);

	}

	/**
	 * Creates a new N4M request using given values
	 * 
	 * @param errorCode
	 * @param msgId
	 *            message ID
	 * @param timestamp
	 *            timestamp
	 * @param applications
	 *            list of applications
	 * @throws N4MException
	 *             if validation fails (BADMSG)
	 * @throws NullPointerException
	 *             if timestamp or applications is null
	 */
	public N4MResponse(ErrorCodeType errorCode, int msgId, long timestamp, List<ApplicationEntry> applications)
			throws N4MException, NullPointerException {
		setMsgId(msgId);
		setErrorCode(errorCode);
		setTimestamp(timestamp);
		setApplications(applications);

	}

	/**
	 * Get list of applications
	 * 
	 * @return list of applications
	 */
	public List<ApplicationEntry> getApplications() {
		List<ApplicationEntry> list2 = applications.stream().map(t -> {
			try {
				return new ApplicationEntry(t.getApplicationName(), t.getAccessCount());
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (N4MException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return t;

		}).collect(Collectors.toList());

		return list2;
	}

	/**
	 * Set the list of applications
	 * 
	 * @param applications
	 *            list of applications
	 * @throws NullPointerException
	 *             if applications is null
	 * @throws N4MException
	 *             if too many applications (BADMSG)
	 */
	public void setApplications(List<ApplicationEntry> applications) throws NullPointerException, N4MException {
		Objects.requireNonNull(applications, "applications is null");
		for (ApplicationEntry entry : applications) {
			// test every ApplicationEntry String is not null
			Objects.requireNonNull(entry, "ApplicationEntry is null");
			Objects.requireNonNull(entry.getApplicationName(), "ApplicationEntry is null");
		}
		if (applications.size() > BYTEMASK) {
			// test whether applicationName is in the right range
			throw new N4MException("applications size is not in the unsigned byte range", ErrorCodeType.BADMSG);
		}

		// create the same size ArrayList

		this.applications = applications.stream().map(t -> {
			try {
				return new ApplicationEntry(t.getApplicationName(), t.getAccessCount());
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (N4MException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return t;

		}).collect(Collectors.toList());

	}

	/**
	 * Return timestamp
	 * 
	 * @return timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Set timestamp
	 * 
	 * @param timestamp
	 *            response timestamp
	 * @throws N4MException
	 *             if validation fails
	 * @throws NullPointerException
	 *             if timestamp is null
	 */
	public void setTimestamp(long timestamp) throws N4MException, NullPointerException {
		Objects.requireNonNull(timestamp, "timestamp is null");

		if (timestamp < 0L || timestamp > INTMASK) {
			// test the time is valid or not
			throw new N4MException("timestamp is not in the range", ErrorCodeType.BADMSG);
		}
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		String str = "";

		for (ApplicationEntry e : applications) {
			str += e + ",";
		}

		long time = timestamp * 1000L;
		
		Date strTime = new Date(time);
		System.out.println(str.length());
		str += " Data=" + strTime;
		System.out.println(str.length());
		return super.toString() + str;
	}

	/**
	 * return the hashcode of the object
	 * 
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		int hashCode = 1;
		for (ApplicationEntry e : applications) {
			hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
		}
		hash += timestamp + hashCode;
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
			// test msgid and errorcodeNum
			return false;
		}
		if (obj == null || obj.getClass() != getClass()) {
			/* obj is null or class type is different */
			return false;
		} else {
			N4MResponse test = (N4MResponse) obj;
			return this.applications.equals(test.applications) && this.timestamp == (test.timestamp);
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

		header[0] |= RESPONSE_FLAG;

		byte[] msg = null;
		try {
			// write header
			out.writeByte(header[0]);
			out.writeByte(header[1]);
			// write Date
			out.writeInt((int) (timestamp));
			// write applications
			out.writeByte(applications.size());
			for (ApplicationEntry entry : applications) {
				out.writeShort(entry.getAccessCount());
				out.writeByte(entry.getApplicationName().length());
				out.writeBytes(entry.getApplicationName());
			}
			out.flush();
			msg = buf.toByteArray();
		} catch (IOException e) {

		}
		return msg;
	}
}
