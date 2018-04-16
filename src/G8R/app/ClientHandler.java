/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 3 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.net.Socket;
import java.util.logging.Logger;

import N4M.app.N4MServer;

/**
 * which is a thread that service each clients
 */
public class ClientHandler implements Runnable {
	private Socket clntSock;
	private Logger logger;
	private N4MServer n4mServer;

	/**
	 * @param clntSock
	 * @param logger
	 * @param n4mServer 
	 */
	public ClientHandler(Socket clntSock, Logger logger, N4MServer n4mServer) {
		this.clntSock = clntSock;
		this.logger = logger;
		this.n4mServer = n4mServer;
	}

	/**
	 * Creates a new ClientHandler thread for the socket provided.
	 */
	public void handleEchoClient() {
		Context context = new Context();
	//	context.setState(new G8RPollStep( logger, n4mServer));
		
		while (true) {
			if (context.isEndFlag()) {
				// if the context is in the final state, break.
				break;
			}
			// send message and turn to the right state
			//context.pull();
		}

	}

	/**
	 * The run method is invoked by the ExecutorService (thread pool).
	 */
	@Override
	public void run() {
		handleEchoClient();
	}

}