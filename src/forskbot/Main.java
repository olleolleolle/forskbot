package forskbot;

import java.io.File;

import forskbot.irc.IrcConnection;
import forskbot.irc.protocolhandlers.IncomingMessageHandler;
import forskbot.irc.protocolhandlers.PostServerConnectHandler;
import forskbot.messagehandlers.IrcPollHandler;
import forskbot.messagehandlers.IrcTitleHandler;

/**
 * @TODO do properly
 * @author noname
 *
 */
public class Main {

	/**
	 * 
	 * 
	 * @param args <path/to/configuration.properties> <path/to/log4j.xml>
	 * @throws IrcException 
	 */
	public static void main(String[] args) throws IrcException {
		File config = new File(args[0]);
		String log4jConfig = args[1];
		System.setProperty("log4j.configuration", log4jConfig);
		
		Configuration.getSelf().parseConfiguration(config);
		
		IrcConnection conn = new IrcConnection();
		conn.addHandler("255", new PostServerConnectHandler());
		
		IncomingMessageHandler msgHandler = new IncomingMessageHandler();
		msgHandler.addMessageHandler(new IrcPollHandler());
		msgHandler.addMessageHandler(new IrcTitleHandler());
		conn.addHandler("PRIVMSG", msgHandler);
		
		conn.connect(false);
	}

}
