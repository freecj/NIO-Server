/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 6 >
* Class: <CSI 4321>
*
************************************************/
package N4M.app;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import N4M.serialization.ApplicationEntry;
import N4M.serialization.ErrorCodeType;
import N4M.serialization.N4MException;
import N4M.serialization.N4MMessage;
import N4M.serialization.N4MQuery;
import N4M.serialization.N4MResponse;

/**
 *
 */
public class N4MServer implements Runnable {
	private static final int READMAX = 500; // Maximum size of datagram

	private Logger logger = Logger.getLogger(N4MServer.class.getName());
	private FileHandler fileTxt;
	private SimpleFormatter formatterTxt;
	private N4MQuery query;
	private N4MResponse response;
	private DatagramSocket socketUdp;
	/**
	 * application count map
	 */
	public Map<String, AtomicInteger> mapName;
	/**
	 * timestamp map
	 */
	public Map<String, Date> mapTime;

	/**
	 * @param destPort
	 * @throws IOException
	 * @throws SecurityException
	 * @throws SocketException
	 */
	public N4MServer(int destPort) throws SecurityException, IOException, SocketException {
		socketUdp = new DatagramSocket(destPort); // UDP socket for sending
		logger.setLevel(Level.INFO);
		fileTxt = new FileHandler("n4m.log");
		logger.addHandler(fileTxt);

		// create a TXT formatter
		formatterTxt = new SimpleFormatter();
		fileTxt.setFormatter(formatterTxt);

		mapName = new ConcurrentHashMap<>();
		mapTime = new ConcurrentHashMap<>();
	}

	/**
	 * msg is response and wrong
	 * @throws N4MException
	 */
	public void sendMsgisResponseWrong() throws N4MException {
		response = new N4MResponse(ErrorCodeType.BADMSG, query.getMsgId(), 0, new ArrayList<ApplicationEntry>());
	}

	/**
	 * msg is query and is wrong
	 * @param t errorcode
	 * @throws N4MException
	 */
	public void sendMsgisQueryWrong(ErrorCodeType t) throws N4MException {
		response = new N4MResponse(t, 0, 0, new ArrayList<ApplicationEntry>());
	}

	/**
	 * @throws N4MException
	 */
	public void sendMsgRight() throws N4MException {
		List<ApplicationEntry> applicationsReceived = new ArrayList<ApplicationEntry>();
		AtomicInteger useCount = new AtomicInteger(0);
		long time = 0;

		for (String name : mapName.keySet()) {
			ApplicationEntry newEntry = new ApplicationEntry(name, mapName.get(name).get());
			applicationsReceived.add(newEntry);
		}

		for (Date t : mapTime.values()) {
			time = Math.max(time, t.getTime() / 1000L);
		}
		response = new N4MResponse(ErrorCodeType.NOERROR, query.getMsgId(), time, applicationsReceived);

	}

	/**
	 * close socket
	 */
	public void close() {
		socketUdp.close();
	}

	/**
	 * read message from the client
	 * 
	 * @throws IOException
	 * 
	 */
	public void readMsg() throws IOException {
		DatagramPacket packet = new DatagramPacket(new byte[READMAX], READMAX);

		socketUdp.receive(packet); // Receive packet from client
		byte[] in = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		try {
			N4MMessage receiveMsg = N4MMessage.decode(in);
			if (receiveMsg instanceof N4MQuery) {
				// msg is query and right
				query = (N4MQuery) receiveMsg;
				logMsg(packet);
				sendMsgRight();

			} else {
				// msg is response
				sendMsgisResponseWrong();
			}
			packet.setData(response.encode());
			socketUdp.send(packet);

		} catch (NullPointerException e) {
			System.err.println("read message from client has NullPointerException." + e.getMessage());
		} catch (N4MException e) {
			try {
				// msg is query and has error
				logMsg(packet);
				sendMsgisQueryWrong(e.getErrorCodeType());
				packet.setData(response.encode());
				socketUdp.send(packet);

			} catch (N4MException e1) {
				System.err.println("send message to client has N4MException:" + e.getErrorCodeType());
			}
			System.err.println("read message from client has N4MException:" + e.getErrorCodeType());
		}
	}

	/**
	 * log info of message information
	 * 
	 * @param packet
	 */
	public void logMsg(DatagramPacket packet) {
		logger.info("<" + packet.getAddress() + ">:" + "<" + packet.getPort() + ">-" + "<" + query.getBusinessName()
				+ ">" + System.getProperty("line.separator"));

	}

	@Override
	public void run() {
		while (true) {
			try {
				readMsg();
			} catch (IOException e) {
				System.err.println("read message from client has IOException");
				e.printStackTrace();
				socketUdp.close();
			}
		}

	}

}
