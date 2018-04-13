/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 2 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.net.Socket;
import java.util.Set;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import G8R.serialization.*;

/**
 * Client that send and get G8RMessage
 *
 */
public class G8RClient {
	private Socket socket;
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
			// Create socket that is connected to server on specified port
			socket = new Socket(ip, port);
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
			socketOut = new MessageOutput(socket.getOutputStream());
			socketIn = new MessageInput(socket.getInputStream());
			String[] param = new String[0];
			String function = "inital";
			// new a g8r request for initalization
			g8rRequest = new G8RRequest(function, param, cookieClient);

		} catch (IOException e) {
			System.err.println("socket init failed:");
			close();
			System.exit(1);
		} catch (ValidationException e) {
			System.err.println("cookieClient init failed:");
			close();
			System.exit(1);
		} catch (Exception e) {
			System.err.println("other exception:");
			close();
			System.exit(1);
		}
	}

	/**
	 * read response message from server
	 */
	public void read() {
		try {
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
			if (socket != null && !socket.isClosed())
				socket.close();
		} catch (IOException e) {
			System.err.println("socket closed failed:");
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
			g8rRequest.encode(socketOut);
		} catch (ValidationException e) {

			System.err.println("socket send Request failed: ValidationException");

		} catch (IOException e) {
			close();
			System.err.println("socket send Request failed: IOException");
			System.exit(1);
		} catch (Exception e) {
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
			g8rRequest.encode(socketOut);
		} catch (ValidationException e) {
			System.err.println("socket send Request failed: ValidationException");
		} catch (IOException e) {
			close();
			System.err.println("socket send Request failed: IOException");
			System.exit(1);
		} catch (Exception e) {
			System.err.println("other exception:");
			close();
			System.exit(1);
		}
	}

	@Override
	public int hashCode() {
		return socket.hashCode() + cookieFileName.hashCode();
	}

	/**
	 * test whether two client equal or not
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
			G8RClient test = (G8RClient) obj;
			return socket.equals(test.socket) && cookieFileName.equals(test.cookieFileName);
		}
		return result;
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

			System.out.print("Function>");

			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			int index = 0;
			while (true) {
				String userInput = "";
				String foreStr = " ";
				if ((userInput = stdIn.readLine()) != null) {
					String test = foreStr + userInput;

					if (index == firstTime) {
						// input function
						if (!client.isValidParam(test)) {
							// input error
							System.err.println("Bad user input: Function not a proper token (alphanumeric)");
							System.err.flush();
							System.out.print("Function>");
							System.out.flush();
							// input again
							continue;
						}
						// client send new function
						client.sendRequest(userInput);
					} else {
						// input params
						if (!client.isValidParam(test)) {
							System.err.println("Bad user input: Params not a proper token (alphanumeric)");
							System.err.flush();
							System.out.print(client.g8rResponse.getMessage());
							System.out.flush();
							// input again
							continue;
						}

						String[] param = userInput.split(" ");
						// client send request with new param
						client.sendRequest(param);
					}
					// client read response message
					client.read();
					index++;
				}
			}
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
