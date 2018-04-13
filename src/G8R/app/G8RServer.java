/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 6 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import N4M.app.N4MServer;

/**
 * Server that send and get G8RMessage
 */
public class G8RServer {
	private ExecutorService ThreadPool;
	private Logger logger = Logger.getLogger(G8RServer.class.getName());
	private ServerSocket serverSocket;
	private FileHandler fileTxt;
	private SimpleFormatter formatterTxt;
	private N4MServer n4mServer;
	private Thread n4m;

	/**
	 * constructor for server, use Executors newFixedThreadPool as thread pool
	 * 
	 * @param port
	 * @param threadNum
	 *            thread pool number
	 */
	public G8RServer(int port, int threadNum) {
		try {

			ThreadPool = Executors.newFixedThreadPool(threadNum);
			// Create a server socket to accept client connection requests
			serverSocket = new ServerSocket();
			// port reuse
			serverSocket.setReuseAddress(true);
			serverSocket.bind(new InetSocketAddress(port));

			logger.setLevel(Level.INFO);
			fileTxt = new FileHandler("connections.log");
			logger.addHandler(fileTxt);

			// create a TXT formatter
			formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);

			n4mServer = new N4MServer(port);
			n4m = new Thread(n4mServer);
			n4m.start();
			// Run forever, accepting and spawning a thread for each connection
			while (true) {
				ThreadPool.execute(new ClientHandler(serverSocket.accept(), logger, n4mServer));
			}
			
		} catch (Exception e) {
			ThreadPool.shutdown();
			System.err.println("G8RSever has exception. " + e.getMessage());
		}
	}

	/**
	 * test the string is numeric
	 * 
	 * @param str
	 * @return true if is numeric, otherwise false
	 */
	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	/**
	 * main function of the server
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if ((args.length != 2)) {
			// Test for correct # of args
			System.err.println("Echo server requires 2 argument: <Port> <thread number>");
			throw new IllegalArgumentException("Parameter(s): <Port> <thread number>");
		}
		if (isNumeric(args[0]) && isNumeric(args[1])) {
			// args is numeric
			int servPort = Integer.parseInt(args[0]);// Server port
			int threadNum = Integer.parseInt(args[1]);// the number of thread in the thread pool
			new G8RServer(servPort, threadNum);// initial the server
		} else {
			// args is wrong
			System.err.println("Echo server <Port> or <thread number> is not numeric.");
		}

	}

}
