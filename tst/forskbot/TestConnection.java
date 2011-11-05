package forskbot;

import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import forskbot.irc.IrcConnection;
import forskbot.irc.protocolhandlers.IncomingMessageHandler;
import forskbot.irc.protocolhandlers.PostServerConnectHandler;
import forskbot.messagehandlers.IrcPollHandler;
import forskbot.messagehandlers.IrcTitleHandler;

public class TestConnection {
	
	@BeforeClass
	public static void init() throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
		
		Logger.getRootLogger().getLoggerRepository().getLogger("forskbot").setLevel(Level.ALL);
		Logger.getRootLogger().getLoggerRepository().getLogger("forskbot").setAdditivity(false);
		Logger.getRootLogger().getLoggerRepository().getLogger("forskbot").addAppender(new ConsoleAppender(new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
	}

	@Test
	public void dummy()  {
		// no-op
	}
	
	@Ignore
	@Test
	public void testConnectAndDoStuff() throws Exception {
		
		
		Properties props = new Properties();
		props.setProperty(Configuration.PROP_CHANNELS, "#nine1238");
		props.setProperty(Configuration.PROP_NICK, "Wintermute9k");
		props.setProperty(Configuration.PROP_NAME, "forskbot");
		props.setProperty(Configuration.PROP_HOST, "irc.freenode.org");
		props.setProperty(Configuration.PROP_PORT, "6667");
		props.setProperty(Configuration.PROP_JOINMSG, "Toodlepip!");
		props.setProperty(Configuration.PROP_REPLYRATEDELAY, "1000");
		props.setProperty(Configuration.PROP_BOTMASTERS, "init3");
		props.setProperty(Configuration.PROP_HTTPGET_RATEDELAY, "" +  1000);
		
		Configuration config = Configuration.getSelf();
		config.parseRawConfig(props);
		
		
		
		IrcConnection conn = new IrcConnection();
		conn.addHandler("255", new PostServerConnectHandler());
		
		IncomingMessageHandler msgHandler = new IncomingMessageHandler();
		msgHandler.addMessageHandler(new IrcPollHandler());
		msgHandler.addMessageHandler(new IrcTitleHandler());
		
		conn.addHandler("PRIVMSG", msgHandler);
		
		
		conn.connect(false);
		
		
		
		
		
		Thread.sleep(120000);
		conn.releaseConnection();
	}

}
