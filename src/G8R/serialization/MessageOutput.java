/************************************************
*
* Author: <Jian Cao>
* Assignment: <Prgrame 0 >
* Class: <CSI 4321>
*
************************************************/
package G8R.serialization;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Serialization output source for messages
 */
public class MessageOutput {
	/**
	 * Cookielist use it to output
	 */
	private BufferedWriter outputWriter = null;

	/**
	 * Constructs a new output source from an OutputStream
	 * 
	 * @param out outputstream
	 * @exception java.lang.NullPointerException if out is null
	 */
	public MessageOutput(java.io.OutputStream out) throws NullPointerException {
		if (out == null) {
			/* OutputStream is null */
			throw new NullPointerException("OutputStream is null");
		}

		try {
			this.outputWriter = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.US_ASCII.name()));
		} catch (UnsupportedEncodingException e) {

		}
	}

	/**
	 * A method used for Cookielist to write the output
	 * 
	 * @param message the string to be written
	 * @exception IOException if get Ioexceotion when writing
	 */
	public void write(String message) throws IOException {
		outputWriter.write(message);
		
	}

	/**
	 * A method used for Cookielist to flush the output
	 * 
	 * @exception IOException if get Ioexceotion when flush
	 */
	public void flush() throws IOException {
		
		outputWriter.flush();
	}

	/**
	 * A method used for Cookielist to close the output
	 * 
	 * @exception IOException if get Ioexceotion when closed
	 */
	public void close() throws IOException {
		outputWriter.close();
	}
}
