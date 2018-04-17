/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 5 >
* Class: <CSI 4321>
*
************************************************/
package N4M.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.Random;

import N4M.serialization.N4MException;
import N4M.serialization.N4MMessage;
import N4M.serialization.N4MQuery;
import N4M.serialization.N4MResponse;

/**
 *
 */
public class N4MClient {

//	private DatagramSocket socket;
	private String buisnessName;
	private static final int READMAX = 65600; // Maximum size of datagram
	private static final int TIMEOUT = 3000; // Resend timeout (milliseconds)
	private int msgId;
	private static final int MAXTRIES = 5; // Maximum retransmissions
	private AsynchronousSocketChannel clntChan;
	private ByteBuffer writeBuf;
	private ByteBuffer readBuf;
	/**
	 * client init function
	 * 
	 * @param destAddr
	 *            ip addr of the server
	 * @param destPort
	 *            prot number of the server
	 * @param buisnessName
	 *            buisnessName of application
	 * @throws IOException 
	 */
	public N4MClient(InetAddress destAddr, int destPort, String buisnessName) throws IOException {
		//socket = new DatagramSocket(); // UDP socket for sending

		//socket.connect(destAddr, destPort);
		
		// Create channel and set to nonblocking
		clntChan = AsynchronousSocketChannel.open();

	
		// try to connect to the server side
		clntChan.connect(new InetSocketAddress(destAddr, destPort), clntChan,
				new CompletionHandler<Void, AsynchronousSocketChannel>() {
					@Override
					public void completed(Void result, AsynchronousSocketChannel channel) {
						System.out.print("Function>");

						startWrite(clntChan, 0);

					}

					@Override
					public void failed(Throwable exc, AsynchronousSocketChannel channel) {
						try {
							System.out.println("fail to connect to server");
							clntChan.close();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							System.out.println("fail to connect to server");
						}

					}

				});
		this.buisnessName = buisnessName;
	}

	/**
	 * send message to the server
	 * 
	 * @throws IOException
	 * 
	 */
	public void sendMsg() throws IOException {
		int max = 255;
		int min = 0;
		Random random = new Random();

		int randomNum = random.nextInt(max) % (max - min + 1) + min;
		msgId = randomNum;
		try {
			N4MQuery sMsg = new N4MQuery(randomNum, buisnessName);
			// Send request
			byte[] encodedVote = sMsg.encode();
			writeBuf = ByteBuffer.wrap(encodedVote);
			/*DatagramPacket message = new DatagramPacket(encodedVote, encodedVote.length);
			socket.setSoTimeout(TIMEOUT); // Maximum receive blocking time (milliseconds)
			socket.send(message);*/
		} catch (NullPointerException e) {
			System.err.println("send message from server has NullPointerException." + e.getMessage());
		} catch (N4MException e) {
			System.err.println("send message from server has N4MException:" + e.getErrorCodeType().toString());
		}
	}

	/**
	 * read message from the server
	 * 
	 * @throws IOException
	 * 
	 */
	public void readMsg() throws IOException {
		/*DatagramPacket packet = new DatagramPacket(new byte[READMAX], READMAX);
		socket.receive(packet); // Receive packet from server*/
		byte[] in = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
		try {
			N4MMessage receiveMsg = N4MMessage.decode(in);
			if (receiveMsg instanceof N4MResponse) {
				if (msgId != receiveMsg.getMsgId()) {
					System.err.println("msgId does not equal.");
				}
				System.out.println(("Received msg :" + (N4MResponse) receiveMsg));

			} else {
				System.err.println("The packet is not repsonse message.");
			}

		} catch (NullPointerException e) {
			System.err.println("read message from server has NullPointerException." + e.getMessage());
		} catch (N4MException e) {
			e.printStackTrace();
			System.err.println("read message from server has N4MException:" + e.getErrorCodeType().toString());
		}
	}
	private void startRead(final AsynchronousSocketChannel sockChannel, int bytesRead,  final int tryNum) {
		// client read response message

		// index.incrementAndGet();
		// Start with buffer in unknown state
		readBuf.clear(); // Prepare buffer for input, ignoring existing state

		sockChannel.read(readBuf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
			// read();
			@Override
			public void completed(Integer result, AsynchronousSocketChannel channel) {
				// message is read from server
				String now = ret + (char) readBuf.get(0);

				if (now.length() >= BufferDelimiter.length()) {
					/* delete the delimiter */
					if (isValidDlimiter(now.substring(now.length() - BufferDelimiter.length()), BufferDelimiter)) {
						read(now);
						int newIndex = index + 1;
						startWrite(sockChannel, newIndex);
					} else {
						startRead(sockChannel, index, now);
					}
				} else {
					startRead(sockChannel, index, now);
				}
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel channel) {
				close();
				System.out.println("fail to read message from server");
			}

		});

	}

	private void startWrite(final AsynchronousSocketChannel sockChannel, final int tryNum) {
		String userInput = "";
		String foreStr = " ";
		try {
				sockChannel.write(writeBuf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
					@Override
					public void completed(Integer result, AsynchronousSocketChannel channel) {
						
						startRead(sockChannel, 7, MAXTRIES);
					}

					@Override
					public void failed(Throwable exc, AsynchronousSocketChannel channel) {
						System.out.println("Fail to write the message to server");
					}
				});
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	/**
	 * close socket
	 */
	public void close()  {
		if (clntChan != null && clntChan.isOpen()) {
			try {
				clntChan.shutdownInput();
				clntChan.shutdownOutput();
				clntChan.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.err.println("client channel cannot close rightly.");
			}
			
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) { // Test for correct # of args
			throw new IllegalArgumentException("Parameter(s): <IP/name>" + " <Port> <business name>");
		}
		try {
			InetAddress destAddr = InetAddress.getByName(args[0]);// Destination addr
			int destPort = Integer.parseInt(args[1]); // Destination port
			String buisnessName = args[2];
			N4MClient client = new N4MClient(destAddr, destPort, buisnessName);
			int tries = 0; // Packets may be lost, so we have to keep trying
			boolean receivedResponse = false;
			do {
				client.sendMsg();
				try {
					client.readMsg();
					receivedResponse = true;
				} catch (InterruptedIOException e) { // We did not get anything
					tries += 1;
					System.out.println("Timed out, " + (MAXTRIES - tries) + " more tries...");
				}

			} while ((!receivedResponse) && (tries < MAXTRIES));
			if (!receivedResponse) {

				System.out.println("No response -- giving up.");
			}
			client.close();
		} catch (SocketException e) {
			e.printStackTrace();
			System.err.println("Therr is a SocketException in the main function.");
		} catch (IOException e) {
			System.err.println("Therr is a IOException in the main function or client initial function cannot work.");
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Therr is a Exception in the main function.");
		}

	}

}
