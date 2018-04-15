/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 6 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
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
	private static final int BUFSIZE = 0;
	private ExecutorService ThreadPool;
	private static Logger logger = Logger.getLogger(G8RServer.class.getName());
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
		AsynchronousChannelGroup group = null;
		// ChannelGroup用来管理共享资源
		try {
		 group = AsynchronousChannelGroup.withFixedThreadPool(threadNum,
				Executors.defaultThreadFactory());
		final AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open(group);

		listener.bind(new InetSocketAddress(port));

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
			listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(AsynchronousSocketChannel clntChan, Void attachment) {
					listener.accept(null, this);
					try {
						handleAccept(clntChan);
					} catch (IOException e) {
						failed(e, null);
					}
				}

				@Override
				public void failed(Throwable e, Void attachment) {
					logger.log(Level.WARNING, "Close Failed", e);
				}
			});
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			try {
				group.shutdownNow();
			} catch (IOException e1) {
				System.out.println("Terminating the group...");
				e1.printStackTrace();
			}
			
			logger.log(Level.WARNING, "Server Interrupted", e);
		} catch (IOException e1) {
			try {
				group.shutdownNow();
			} catch (IOException e) {
				System.out.println("Terminating the group...");
				e1.printStackTrace();
			}
		}

	}
	 /**
     * Called after each accept completion
     * 
     * @param clntChan channel of new client
     * @throws IOException if I/O problem
     */
    public static void handleAccept(final AsynchronousSocketChannel clntChan) throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(BUFSIZE);
        clntChan.read(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            public void completed(Integer bytesRead, ByteBuffer buf) {
                /*try {
                    handleRead(clntChan, buf, bytesRead);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Handle Read Failed", e);
                }*/
            }

            public void failed(Throwable ex, ByteBuffer v) {
                try {
                    clntChan.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Close Failed", e);
                }
            }
        });
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
			// Block until current thread dies

		} else {
			// args is wrong
			System.err.println("Echo server <Port> or <thread number> is not numeric.");
		}

	}

}
