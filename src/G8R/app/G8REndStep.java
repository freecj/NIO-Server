package G8R.app;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.logging.Logger;

import G8R.serialization.CookieList;
import G8R.serialization.G8RResponse;
import G8R.serialization.ValidationException;

/**
 * @author CAOJ
 *
 */
public class G8REndStep extends PollState {
	private String msg;
	/**
	 * @param clntChan
	 * @param logger
	 * @param msg 
	 */
	public G8REndStep(AsynchronousSocketChannel clntChan, Logger logger, String msg) {
		super(clntChan, logger);
		this.msg = msg;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generateMsg() {
		CookieList beforeCookie = g8rRequest.getCookieList();
		try {
			g8rResponse = new G8RResponse(statusError, functionNameForNull, msg, beforeCookie);
		} catch (ValidationException e) {
			close();
			e.printStackTrace();
		}
	}

}
