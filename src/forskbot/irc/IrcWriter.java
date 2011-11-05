package forskbot.irc;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.apache.log4j.Logger;

import forskbot.IrcException;

/**
 * @TODO wrap before passing it around.
 * @author noname
 * 
 */
public class IrcWriter {

	private Logger log = Logger.getLogger(IrcWriter.class.getName());
	private BufferedWriter writer;
	private static final String nl = "\r\n";

	public IrcWriter(Socket socket) throws IOException {
		this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
	}

	/**
	 * @TODO is a newline required?
	 * @param rawLine
	 * @throws IOException
	 *             write error
	 */
	public void rawWrite(String rawLine) throws IrcException {
		try {
			log.info(rawLine);
			writer.write(rawLine + nl);
			writer.flush();
		} catch (IOException ioe) {
			throw new IrcException(ioe.getMessage(), ioe);
		}
	}

	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Write a message to a channel. If channel name doesn't start with #, it
	 * will be appended.
	 * 
	 * @param channel
	 * @param message
	 * @throws IrcException
	 */
	public void writePublic(String channelName, String message) throws IrcException {
		if (!channelName.startsWith("#")) {
			channelName = "#" + channelName;
		}
		rawWrite("PRIVMSG " + channelName + " :" + message);
	}

	/**
	 * 
	 * @param nick
	 * @param message
	 * @throws IrcException
	 */
	public void writePrivate(String nick, String message) throws IrcException {
		/**
		 * @TODO decide if this is necessary in a separate method (almost same
		 *       as writePublic)
		 */
		rawWrite("PRIVMSG " + nick + " :" + message);
	}

}
