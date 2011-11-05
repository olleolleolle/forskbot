package forskbot.irc;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.SocketFactory;

import org.apache.log4j.Logger;

import forskbot.Configuration;
import forskbot.IIrcConnection;
import forskbot.IIrcProtocolHandler;
import forskbot.IrcException;

/**
 *
 */
public class IrcConnection implements IIrcConnection, Runnable {

	private Logger log = Logger.getLogger(IrcConnection.class);
	private Thread connectionThread;
	private String host;
	private int port;
	private String nick;
	private String name;
	private Socket socket;
	private IrcReader reader;
	private IrcWriter writer;
	//
	private Map<String, List<IIrcProtocolHandler>> protocolHandlers;

	public IrcConnection() throws IrcException {
		Configuration config = Configuration.getSelf();

		this.host = config.getHost();
		this.port = config.getPort();
		this.nick = config.getNick();
		this.name = config.getName();
		protocolHandlers = new HashMap<String, List<IIrcProtocolHandler>>();
	}

	/**
	 * Initialize the connection after adding handlers.
	 */
	public void connect(boolean daemonThread) throws IrcException {
		try {
			socket = SocketFactory.getDefault().createSocket(host, port);

			if (socket.isConnected()) {
				reader = new IrcReader(socket);
				writer = new IrcWriter(socket);
				writer.rawWrite("NICK " + nick);
				writer.rawWrite("USER " + name + " " + name + " " + host + " :" + name);
			} else {
				throw new IrcException("Failed to establish connection");
			}

		} catch (Exception ex) {
			throw new IrcException("Failed to connect to " + host + ":" + port, ex);
		}
		this.connectionThread = new Thread(this);
		this.connectionThread.setDaemon(daemonThread);
		this.connectionThread.start();
	}

	/**
	 * Loop over reads from server. The lines received will be in the form of
	 * "<server id> <irc protocol command> <the rest ...>"
	 */
	@Override
	public void run() {
		try {
			while (socket.isConnected()) {
				synchronized (this) {
					String line = reader.rawRead();
					if (line != null) {
						fireHandlers(line);
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Handlers handle irc protocol comands and blocks (No more reads) until the
	 * handler has finished executing.
	 * 
	 * @param line
	 * @throws IrcException
	 */
	protected void fireHandlers(String line) throws IrcException {
		String[] parts = line.split("\\s", 3);

		if (parts.length >= 3) {
			List<IIrcProtocolHandler> handlers = null;
			if ((handlers = protocolHandlers.get(parts[1])) != null) {

				for (IIrcProtocolHandler handler : handlers) {
					log.debug("Handling server line with: " + handler.getClass().getName());
					handler.onServerMessage(writer, parts[0], parts[1], parts[2]);
				}
			}
		} else {
			// Could be a PING (Which is not part of the standard irc protocol?
			if (line.trim().startsWith("PING")) {
				String[] theGame = line.split("\\s", 2);
				writer.rawWrite("PONG " + theGame[1].substring(1));
			} else {
				log.warn("Server sent an invalid line: " + line);
			}
		}

	}

	/**
	 * Add a protocol handler
	 * 
	 * @param ircCmd
	 *            protocol cmd (What to call them, mnemonics?)
	 * @param handler
	 */
	public void addHandler(String ircCmd, IIrcProtocolHandler handler) {
		List<IIrcProtocolHandler> handlers = protocolHandlers.get(ircCmd);
		if (handlers == null) {
			handlers = new ArrayList<IIrcProtocolHandler>();
			handlers.add(handler);
			protocolHandlers.put(ircCmd, handlers);
		} else {
			handlers.add(handler);
		}
	}

	/**
	 * @TODO Maybe send disconnect to shutdown cleanly without timeout.
	 * 
	 * @throws IrcException
	 */
	public void releaseConnection() throws IrcException {
		synchronized (this) {
			try {
				writer.close();
				reader.close();
				socket.close();
			} catch (IOException e) {
				throw new IrcException("Failed to perform a clean connection shutdown.", e);
			}
		}
	}
}
