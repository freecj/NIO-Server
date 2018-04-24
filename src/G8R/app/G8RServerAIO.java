/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 7 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

import N4M.app.N4MServer;

/**
 * Server that send and get G8RMessage
 */
public class G8RServerAIO {
	private static Logger logger = Logger.getLogger(G8RServerAIO.class.getName());
	private FileHandler fileTxt;
	private SimpleFormatter formatterTxt;
	private N4MServer n4mServer;
	private Thread n4m;

	/**
	 * constructor for server, use Executors newFixedThreadPool as thread pool
	 * 
	 * @param port
	 */
	public G8RServerAIO(int port) {

		try (AsynchronousServerSocketChannel listenChannel = AsynchronousServerSocketChannel.open()) {
			// Bind local port
			listenChannel.bind(new InetSocketAddress(port));

			logger.setLevel(Level.INFO);
			fileTxt = new FileHandler("connections.log");
			logger.addHandler(fileTxt);

			// create a TXT formatter
			formatterTxt = new SimpleFormatter();
			fileTxt.setFormatter(formatterTxt);

			n4mServer = new N4MServer(port);
			n4m = new Thread(n4mServer);
			n4m.start();

			// Create accept handler
			listenChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
					listenChannel.accept(null, this);
					Context context = new Context();
					context.setState(new G8RPollStep(clntChan, logger, n4mServer));
					try {
						context.getState().handleRead(clntChan, context, "");
					} catch (IOException e) {
						logger.log(Level.WARNING, "Handle Read Failed", e);
					}
				}

				@Override
				public void failed(Throwable e, Void attachment) {
					logger.log(Level.WARNING, "Close Failed", e);
				}
			});
			// Block until current thread dies
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			System.err.println("Server Interrupted...");
			logger.log(Level.WARNING, "Server Interrupted", e);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Server has IOException,Terminated", e);
			System.err.println("Server has IOException,Terminated");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Server has Exception,Terminated", e);
			System.err.println("Server has Exception,Terminated");
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
		if ((args.length != 1)) {
			// Test for correct # of args
			System.err.println("Echo server requires 2 argument: <Port> ");
			throw new IllegalArgumentException("Parameter(s): <Port> ");
		}
		if (isNumeric(args[0])) {
			// args is numeric
			int servPort = Integer.parseInt(args[0]);// Server port

			new G8RServerAIO(servPort);// initial the server
			// Block until current thread dies

		} else {
			// args is wrong
			System.err.println("Echo server <Port> or <thread number> is not numeric.");
		}

	}

}
