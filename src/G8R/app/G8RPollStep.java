/************************************************
*
* Author: <Jian Cao>
* Assignment: <Programe 6 >
* Class: <CSI 4321>
*
************************************************/
package G8R.app;

import java.net.Socket;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import G8R.serialization.CookieList;
import G8R.serialization.G8RResponse;
import G8R.serialization.ValidationException;
import N4M.app.N4MServer;

/**
 * Send Poll or Guess command Response message
 */
public class G8RPollStep extends PollState {
	private N4MServer n4mServer;
	private static final int SHORTMASK = 0xffff;
	/**
	 * @param clntSock
	 * @param logger
	 * @param n4mServer 
	 */
	public G8RPollStep(Logger logger, N4MServer n4mServer) {
		super(logger);
		this.n4mServer = n4mServer;
	}
	/**
	 * set map infor
	 * @param name
	 */
	public void setMapInfo(String name) {
		if (n4mServer.mapName.containsKey(name)) {
			if (n4mServer.mapName.get(name).get() < SHORTMASK) {
				n4mServer.mapName.get(name).getAndIncrement();
			}
		} else {
			n4mServer.mapName.put(name,new AtomicInteger(1));
			
		}
		n4mServer.mapTime.put(name,new Date());
	}
	@Override
	public void generateMsg() {
		// get the cookielist from the request message
		CookieList beforeCookie = g8rRequest.getCookieList();

		try {

			if (strNamePoll.equals(g8rRequest.getFunction())) {
				setMapInfo(strNamePoll);
				// Poll command fits
				if (beforeCookie.findName(strFirstName) && beforeCookie.findName(strSecondName)) {
					// have firstname and last name in request cookielist, go to the foodstep
					String msString = beforeCookie.getValue(strFirstName) + "'s Food mood>";
					g8rResponse = new G8RResponse(statusOk, functionNameForFood, msString, beforeCookie);

					context.setState(new G8RFoodStep( logger));
				} else {
					// does not have the name cookies, then go to the namestep
					g8rResponse = new G8RResponse(statusOk, functionNameForName, "Name (First Last)>", beforeCookie);
					context.setState(new G8RNameStep( logger));

				}
				writerMsg();
			} else if (strNameGuess.equals(g8rRequest.getFunction())) {
				setMapInfo(strNameGuess);
				// Guess command fits
				beforeCookie.add("Num", String.valueOf(new Random().nextInt(10)));
				if (!beforeCookie.findName("Score")) {
					// if cookielist does not have Score, then set it as 0
					beforeCookie.add("Score", "0");
				}

				g8rResponse = new G8RResponse(statusOk, functionNameForSendGuess, "Guess (0-9)?", beforeCookie);
				context.setState(new G8RSendGuess( logger));
				writerMsg();
			} else {
				// command function is wrong
				generateErrorMsg("Unexpected function");
			}

		} catch (ValidationException e) {
			close();
		} catch (Exception e) {
			close();
		}
	}
}
