/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 2 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Set;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import G8R.serialization.*;

/**
 * Client that send and get G8RMessage
 *
 */
public class G8RClient {

	private G8RRequest g8rRequest;
	private G8RResponse g8rResponse;

	private CookieList cookieClient = null;
	private MessageOutput socketOut = null;
	private MessageInput socketIn = null;
	private String endFlag = "NULL";
	private static int firstTime = 0;
	private String cookieFileName;
	private String MessageDelimiter = "\r\n";
	private String okStatsus = "OK";
	private AsynchronousSocketChannel clntChan;
	ByteBuffer writeBuf;
	ByteBuffer readBuf;
	private static final String ENC = "ASCII";
	/* BufferDelimiter for getting from next entry */
	private static String BufferDelimiter = "\r\n\r\n";
	private BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	private int BUFSIZE = 1;

	/**
	 * Constructor of client
	 * 
	 * @param ip
	 *            name or ip addr
	 * @param port
	 *            int number
	 * @param FileName
	 *            String
	 */
	public G8RClient(String ip, int port, String FileName) {
		try {

			cookieFileName = FileName;
			// pass the filename or directory name to File object
			File file = new File(cookieFileName);
			if (!file.exists()) {
				// file is not existed, then create an empty cookielist file.
				file.createNewFile();
				OutputStream cookieFile = new FileOutputStream(file.getAbsoluteFile());
				cookieFile.write(MessageDelimiter.getBytes());
				cookieFile.close();
				cookieClient = new CookieList();
			} else {
				// open the file and read the cookielist.
				InputStream inCookieFile = new FileInputStream(file.getAbsoluteFile());
				MessageInput inMsg = new MessageInput(inCookieFile);
				cookieClient = new CookieList(inMsg);

			}
			readBuf = ByteBuffer.allocateDirect(BUFSIZE);

			String[] param = new String[0];
			String function = "inital";
			// new a g8r request for initalization
			g8rRequest = new G8RRequest(function, param, cookieClient);

			// Create channel and set to nonblocking
			clntChan = AsynchronousSocketChannel.open();

			// Initiate connection to server and repeatedly poll until complete
			clntChan.connect(new InetSocketAddress(ip, port));
			// try to connect to the server side
			clntChan.connect(new InetSocketAddress(ip, port), clntChan,
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

							} finally {
								System.out.println("fail to connect to server");
							}

						}

					});

		} catch (IOException e) {
			System.err.println("socket init failed:");
			close();
			System.exit(1);
		} catch (ValidationException e) {
			System.err.println("cookieClient init failed:");
			close();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("other exception:");
			close();
			System.exit(1);
		}
	}

	private void startRead(final AsynchronousSocketChannel sockChannel, final int index, final String ret) {
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
					}
				} else {
					startRead(sockChannel, index, ret);
				}
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel channel) {
				System.out.println("fail to read message from server");
			}

		});

	}

	private void startWrite(final AsynchronousSocketChannel sockChannel, final int index) {
		String userInput = "";
		String foreStr = " ";
		try {
			if ((userInput = stdIn.readLine()) != null) {
				String test = foreStr + userInput;

				if (index == firstTime) {
					// input function
					if (!isValidParam(test)) {
						// input error
						System.err.println("Bad user input: Function not a proper token (alphanumeric)");
						System.err.flush();
						System.out.print("Function>");
						System.out.flush();
						// input again
						startWrite(sockChannel, index);
						return;
					}
					// client send new function
					sendRequest(userInput);
				} else {
					// input params
					if (!isValidParam(test)) {
						System.err.println("Bad user input: Params not a proper token (alphanumeric)");
						System.err.flush();
						System.out.print(g8rResponse.getMessage());
						System.out.flush();
						// input again
						startWrite(sockChannel, index);
						return;
					}

					String[] param = userInput.split(" ");
					// client send request with new param
					sendRequest(param);
				}

				sockChannel.write(writeBuf, sockChannel, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
					@Override
					public void completed(Integer result, AsynchronousSocketChannel channel) {
						startRead(sockChannel, index, "");
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
	 * Gets a new next CookieList by deserializing from the given input according to the specified serialization.
	 * @param delimiter 
	 * @return String token
	 * @exception ValidationException if validation problem such as illegal name and/or value, etc.
	 * @exception java.io.IOException if I/O problem (EOFException for EoS)
	 * @exception java.lang.NullPointerException if input stream is null
	 */
	/*
	 * public String getNextEntry(String delimiter) throws IOException,
	 * NullPointerException, ValidationException { int i = 0; // the read length of
	 * every time String ret = ""; int index = 0; // represent the number of the
	 * string ret while ((i = clntChan.read(readBuf)) != -1) { ret += (char) i;
	 * index++; if (index >= delimiter.length()) { delete the delimiter if
	 * (isValidDlimiter(ret.substring(ret.length() - delimiter.length()),
	 * delimiter)) { return ret; } } } return ret; }
	 */

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

	/**
	 * read response message from server
	 * @param receivedStr 
	 */
	public void read(String receivedStr) {
		try {

			socketIn = new MessageInput(new ByteArrayInputStream(receivedStr.getBytes(ENC)));
			G8RMessage temp = G8RMessage.decode(socketIn);
			if (temp instanceof G8RResponse) {
				// messsage is response
				g8rResponse = (G8RResponse) temp;

				if (okStatsus.equals(g8rResponse.getStatus())) {
					// response message status is ok
					System.out.print(g8rResponse.getMessage());
				} else {
					// response message status is error
					System.err.print(g8rResponse.getMessage());
				}

				// update cookielist in request message
				CookieList responseCookieList = g8rResponse.getCookieList();
				CookieList reqeustCookieList = g8rRequest.getCookieList();
				Set<String> keys = responseCookieList.getNames();
				for (String name : keys) {
					String value = responseCookieList.getValue(name);
					reqeustCookieList.add(name, value);
				}
				g8rRequest.setCookieList(reqeustCookieList);
				// write to file
				writeCookieToFile();
				if (endFlag.equals(g8rResponse.getFunction())) {
					close();
					System.exit(0);
				} else {
					g8rRequest.setFunction(g8rResponse.getFunction());
				}

			} else {
				// otherwise wrong answer
				throw new ValidationException("Message is other", "");
			}
		} catch (ValidationException e) {
			System.err.println("G8RMessage decode failed: ValidationException");
			close();
			System.exit(1);
		} catch (IOException e) {
			System.err.println("G8RMessage decode failed: IOException");
			close();
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("other exception:");
			close();
			System.exit(1);
		}

	}

	/**
	 * write cookielist to file
	 * 
	 * @throws IOException
	 */
	public void writeCookieToFile() throws IOException {

		File file = new File(cookieFileName);
		MessageOutput cookieFile = new MessageOutput(new FileOutputStream(file.getAbsoluteFile()));
		g8rRequest.getCookieList().encode(cookieFile);
		cookieFile.close();
	}

	/**
	 * close socket of client
	 */
	public void close() {
		try {
			writeCookieToFile();
			if (clntChan != null)
				clntChan.close();
		} catch (IOException e) {
			System.err.println("clntChan closed failed:");
			System.exit(1);
		}
	}

	/**
	 * check whether the user input is valid or not.
	 * 
	 * @param temp
	 *            string to be check
	 * @return if string are alphanumeric, return true. Otherwise false.
	 */
	public boolean isValidParam(String temp) {
		String regex = "^( [A-Za-z0-9]+)+$";// use regex to tesaat whether is alphanumeric or not
		return temp.matches(regex);
	}

	/**
	 * Send first request
	 * 
	 * @param function
	 *            function name
	 */
	public void sendRequest(String function) {
		try {
			g8rRequest.setFunction(function);
			OutputStream out = new ByteArrayOutputStream();
			socketOut = new MessageOutput(out);
			g8rRequest.encode(socketOut);
			writeBuf = ByteBuffer.wrap(socketOut.toString().getBytes(ENC));

		} catch (ValidationException e) {

			System.err.println("socket send Request failed: ValidationException");

		} catch (IOException e) {
			close();
			System.err.println("socket send Request failed: IOException");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("other exception:");
			close();
			System.exit(1);
		}
	}

	/**
	 * send not the first request
	 * 
	 * @param param
	 *            params of request message
	 */
	public void sendRequest(String[] param) {
		try {
			g8rRequest.setFunction(g8rResponse.getFunction());
			g8rRequest.setParams(param);
			OutputStream out = new ByteArrayOutputStream();
			socketOut = new MessageOutput(out);
			g8rRequest.encode(socketOut);
			writeBuf = ByteBuffer.wrap(socketOut.toString().getBytes(ENC));

		} catch (ValidationException e) {
			System.err.println("socket send Request failed: ValidationException");
		} catch (IOException e) {
			close();
			System.err.println("socket send Request failed: IOException");
			System.exit(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("other exception:");
			close();
			System.exit(1);
		}
	}

	/**
	 * client main function
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		G8RClient client = null;
		try {
			if ((args.length <= 2) || (args.length > 3)) {
				// Test for correct # of args
				throw new IllegalArgumentException("Parameter(s): <Server> [<Port>] <Cookiefile>");
			}

			String server = args[0]; // Server name or IP address
			// Convert argument String to bytes using the default character encoding
			int servPort = Integer.parseInt(args[1]);
			// accept file name or directory name through command line args
			String cookieFileName = args[2];

			client = new G8RClient(server, servPort, cookieFileName);
			Thread.currentThread().join();
			/*
			 * System.out.print("Function>");
			 * 
			 * BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			 * int index = 0; while (true) { String userInput = ""; String foreStr = " "; if
			 * ((userInput = stdIn.readLine()) != null) { String test = foreStr + userInput;
			 * 
			 * if (index == firstTime) { // input function if (!client.isValidParam(test)) {
			 * // input error System.err.
			 * println("Bad user input: Function not a proper token (alphanumeric)");
			 * System.err.flush(); System.out.print("Function>"); System.out.flush(); //
			 * input again continue; } // client send new function
			 * client.sendRequest(userInput); } else { // input params if
			 * (!client.isValidParam(test)) {
			 * System.err.println("Bad user input: Params not a proper token (alphanumeric)"
			 * ); System.err.flush(); System.out.print(client.g8rResponse.getMessage());
			 * System.out.flush(); // input again continue; }
			 * 
			 * String[] param = userInput.split(" "); // client send request with new param
			 * client.sendRequest(param); } // client read response message client.read();
			 * index++; } }
			 */
		} catch (Exception e) {
			System.err.println(e.toString() + "main has exception");

		} finally {
			if (client != null) {
				client.close();
			} else {
				System.err.println("client is null");
			}
		}

	}
}
