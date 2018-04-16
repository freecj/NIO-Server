/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 3 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import G8R.serialization.CookieList;
import G8R.serialization.G8RMessage;
import G8R.serialization.G8RRequest;
import G8R.serialization.G8RResponse;
import G8R.serialization.MessageInput;
import G8R.serialization.MessageOutput;
import G8R.serialization.ValidationException;

/**
 * Poll command abstact class. have the read, write, generateMsg shared function
 */
public abstract class PollState {
	// server do the action in the context
	protected Context context;
	protected G8RRequest g8rRequest;
	protected G8RResponse g8rResponse;
	protected MessageOutput socketOut = null;
	protected MessageInput socketIn = null;
	protected Socket clntSock;
	protected Logger logger;

	protected static String strNamePoll = "Poll";
	protected static String functionNameForName = "NameStep";
	protected static String functionNameForNull = "NULL";
	protected static String functionNameForFood = "FoodStep";
	protected static String statusOk = "OK";
	protected static String statusError = "ERROR";
	protected static String strFirstName = "FName";
	protected static String strSecondName = "LName";
	protected static String repeatStr = "Repeat";

	private final String TIMELIMIT = "20000"; // Default limit (ms)
	private final String TIMELIMITPROP = "Timelimit"; // Property
	private int timeLimit;

	protected static String functionNameForSendGuess = "SendGuess";
	protected static String strNameGuess = "Guess";
	protected ByteBuffer writeBuf;
	protected ByteBuffer readBuf;
	protected static final String ENC = "ASCII";
	protected static String BufferDelimiter = "\r\n\r\n";
	protected int BUFSIZE = 1;
	protected AsynchronousSocketChannel clntChan;

	/**
	 * set the new context withe the new state to the new context.
	 * 
	 * @param _context
	 *            which is modified since the state change.
	 */
	public void setContext(Context _context) {
		this.context = _context;
	}

	/**
	 * Constructor which get the clientSocket and logger, create the MessageOutput
	 * and MessageInput for decoding and encoding.
	 * 
	 * @param clntChan
	 * 
	 * @param clientSocket
	 *            client socket
	 * @param logger
	 */
	public PollState(AsynchronousSocketChannel clntChan, Logger logger) {
		// this.clntSock = clientSocket;
		this.logger = logger;
		this.clntChan = clntChan;
		readBuf = ByteBuffer.allocateDirect(BUFSIZE);

		try {

			/*
			 * socketOut = new MessageOutput(clntSock.getOutputStream()); socketIn = new
			 * MessageInput(clntSock.getInputStream());
			 */
			// Get the time limit from the System properties or take the default
			timeLimit = Integer.parseInt(System.getProperty(TIMELIMITPROP, TIMELIMIT));

		} catch (NullPointerException e) {
			// socket close
			close();

		}

	}

	/**
	 * decode message from the client, assign it for g8rRequest
	 * 
	 * @throws NullPointerException
	 * @return true if the type of message is G8RRequest. otherwise false.
	 */
	public boolean read(String receivedStr) {

		G8RMessage temp;
		try {
			// clntSock.setSoTimeout(timeLimit);
			socketIn = new MessageInput(new ByteArrayInputStream(receivedStr.getBytes(ENC)));
			temp = G8RMessage.decode(socketIn);

			if (temp instanceof G8RRequest) {
				// the type of message from the socket is G8RRequest
				// System.out.println("request");
				g8rRequest = (G8RRequest) temp;
				System.out.println(g8rRequest);
				return true;
			} else {
				// otherwise throw exception
				throw new ValidationException("Message is other", "Not Request Msg");
			}
		} catch (ValidationException e1) {
			CookieList beforeCookie = new CookieList();
			System.out.println(e1.getToken());
			try {
				// if there is ValidationException, server need send NULL comand to end the
				// connection.
				g8rResponse = new G8RResponse(statusError, functionNameForNull, "Bad version: " + e1.getToken(),
						beforeCookie);
				context.setEndFlag();
				handleWrite(clntChan, context);
				// writerMsg();
				// close();
				return false;
			} catch (ValidationException e) {
				close();
				return false;
			}

		} catch (SocketTimeoutException e) {
			close();
			System.err.println("G8RSever read timeout. " + e.getMessage());
			return false;
		} catch (IOException e1) {
			close();
			System.err.println("G8RSever read IOException. " + e1.getMessage());
			return false;
		} catch (Exception e) {
			close();
			System.err.println("G8RSever read Exception. " + e.getMessage());
			return false;
		}
	}

	/**
	 * close socket of client
	 */
	public void close() {
		context.setEndFlag();
		try {
			logTerminateMsg();

			if (clntChan != null && clntChan.isOpen()) {
				clntChan.shutdownInput();
				clntChan.shutdownOutput();
				clntChan.close();

			}

		} catch (IOException e) {
			logger.log(Level.WARNING, "Close Failed", e);
			System.err.println("server AIO channle closed failed:");
		}
	}

	/**
	 * When the server get the wrong things from the client, it need send the NULL
	 * function to the client to close the connection with the client.
	 * 
	 * @param msg
	 *            wrong information in the g8rResponse.
	 */
	public void generateErrorMsg(String msg) {
		CookieList beforeCookie = g8rRequest.getCookieList();
		try {
			g8rResponse = new G8RResponse(statusError, functionNameForNull, msg, beforeCookie);
			writerMsg();
			close();

		} catch (ValidationException e) {
			close();
		} catch (Exception e) {
			close();
		}
	}

	/**
	 * send response message and set time out
	 */
	public void writerMsg() {
		try {
			// clntSock.setSoTimeout(timeLimit);
			OutputStream out = new ByteArrayOutputStream();
			socketOut = new MessageOutput(out);
			g8rResponse.encode(socketOut);
			writeBuf = ByteBuffer.wrap(((ByteArrayOutputStream) out).toByteArray());
			logMsg();
		} catch (SocketTimeoutException e) {
			close();
			System.err.println("G8RSever write timeout. " + e.getMessage());
		} catch (IOException e) {
			close();
			System.err.println("G8RSever write IOException. " + e.getMessage());
		} catch (Exception e) {
			close();
			System.err.println("G8RSever write Exception. " + e.getMessage());
		}
	}

	/**
	 * log info of message information
	 * 
	 * @throws IOException
	 */
	public void logMsg() throws IOException {
		logger.info("<" + clntChan.getRemoteAddress() + ">:" + "<" + ">-" + "<" + Thread.currentThread().getId()
				+ "> [Received:<" + g8rRequest.toString() + ">|Sent: <" + g8rResponse.toString() + ">]"
				+ System.getProperty("line.separator"));

	}

	/**
	 * log info of client terminated
	 * 
	 * @throws IOException
	 */
	public void logTerminateMsg() throws IOException {
		logger.info("<" + clntChan.getRemoteAddress() + ">:" + "<" + ">-" + "<" + Thread.currentThread().getId()
				+ "> ***client terminated" + System.getProperty("line.separator"));

	}

	/**
	 * test the string is numeric
	 * 
	 * @param str
	 * @return true if is numeric, otherwise false
	 */
	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * state change and send response message
	 */
	public abstract void generateMsg();

	/**
	 * check string is the format or not
	 * 
	 * @param test
	 *            String to be tested
	 * @param delimiter
	 * @return true if match, otherwise false.
	 */
	public boolean isValidDlimiter(String test, String delimiter) {
		String regex = delimiter;
		return test.matches(regex);

	}

	/**
	 * Called after each read completion
	 * 
	 * @param clntChan
	 *            channel of new client
	 * @param context
	 * @param ret
	 * 
	 * @throws IOException
	 *             if I/O problem
	 */
	public void handleRead(final AsynchronousSocketChannel clntChan, final Context context, final String ret) {
		readBuf.clear(); // Prepare buffer for input, ignoring existing state

		clntChan.read(readBuf, clntChan, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
			// read();
			@Override
			public void completed(Integer bytesRead, AsynchronousSocketChannel channel) {
				// message is read from server
				if (bytesRead == -1) {

					close();

				} else if (bytesRead > 0) {
					String now = ret + (char) readBuf.get(0);
					// System.out.println( byteBuffer2String(readBuf, Charset.forName(ENC)));

					if (now.length() >= BufferDelimiter.length()) {
						/* delete the delimiter */

						if (isValidDlimiter(now.substring(now.length() - BufferDelimiter.length()), BufferDelimiter)) {
							read(now);
							generateMsg();
							handleWrite(clntChan, context);
						} else {
							handleRead(clntChan, context, now);
						}
					} else {
						handleRead(clntChan, context, now);
					}
				}
			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel channel) {
				close();
			}

		});

	}

	/**
	 * Called after each write
	 * 
	 * @param clntChan
	 *            channel of new client
	 * @param context
	 * @param ret
	 * @param buf
	 *            byte buffer used in write
	 * @throws IOException
	 *             if I/O problem
	 */
	public void handleWrite(final AsynchronousSocketChannel clntChan, final Context context) {
		writerMsg();
		clntChan.write(writeBuf, clntChan, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
			@Override
			public void completed(Integer result, AsynchronousSocketChannel channel) {
				if (context.isEndFlag()) {
					close();
				} else {
					context.getState().handleRead(clntChan, context, "");
				}

			}

			@Override
			public void failed(Throwable exc, AsynchronousSocketChannel channel) {
				close();
			}
		});

	}

}
