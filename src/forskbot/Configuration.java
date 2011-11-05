package forskbot;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class Configuration {

	public static final String PROP_HOST = "forskbot.host";
	public static final String PROP_PORT = "forskbot.port";
	public static final String PROP_NICK = "forskbot.nick";
	public static final String PROP_CHANNELS = "forskbot.channels";
	public static final String PROP_NAME = "forskbot.name";
	public static final String PROP_HTTPGET_RATEDELAY = "forskbot.httpgetratedelay";

	/**
	 * Bot masters can be checked for in IIrcMessageHandlers to allow for
	 * privileged operations. This is a comma separated list of usernames.
	 */
	public static final String PROP_BOTMASTERS = "forskbot.masters";

	/**
	 * If this is set, it will write it on channel join.
	 */
	public static final String PROP_JOINMSG = "forskbot.joinmsg";
	/**
	 * Will not reply if the last reply from a specific subject was within this
	 * time (ms)
	 */
	public static final String PROP_REPLYRATEDELAY = "forskbot.replyratedelay";

	//
	//
	//
	private static Configuration self;
	private Properties props;
	//
	private Logger log = Logger.getLogger(Configuration.class);
	private List<String> channels;
	private String nick;
	private String host;
	private int port;
	private String name;
	private int replyRateDelay = 1000;
	private List<String> botmasters;
	private int httpGetRateDelay = 1000; // ms

	private Configuration() {
		channels = new ArrayList<String>();
		props = new Properties();
		botmasters = new ArrayList<String>();
	}

	/**
	 * Parse a configuration file
	 */
	public void parseConfiguration(File configuration) throws IrcException {
		BufferedInputStream bis = null;
		try {
			try {
				bis = new BufferedInputStream(new FileInputStream(configuration));
				props.load(bis);
			} finally {
				bis.close();
			}
		} catch (IOException ioe) {
			throw new IrcException("Failed to read configuration. " + ioe.getMessage(), ioe);
		}
		parseRawConfig(props);
	}

	/**
	 * Parse configuration into this class.
	 * 
	 * @param props
	 */
	void parseRawConfig(Properties props) throws IrcException {
		// Log properties
		props.list(new PrintWriter(System.out) {
			@Override
			public void write(String s) {
				log.info(s);
			}
		});
		this.props = props;

		String prop = "";
		if ((prop = propNotNullOrEmpty(PROP_CHANNELS)) != null) {
			String[] parts = prop.split(",");
			for (String part : parts) {
				String channel = part.trim();
				if (!channel.startsWith("#")) {
					channel = "#" + channel;
				}
				channels.add(channel);
			}
		}

		if ((prop = propNotNullOrEmpty(PROP_NICK)) != null) {
			this.nick = prop;
		}

		if ((prop = propNotNullOrEmpty(PROP_HOST)) != null) {
			this.host = prop;
		}

		if ((prop = propNotNullOrEmpty(PROP_PORT)) != null) {
			this.port = Integer.valueOf(prop);
		}

		if ((prop = propNotNullOrEmpty(PROP_NAME)) != null) {
			this.name = prop;
		}

		if ((prop = propNotNullOrEmpty(PROP_REPLYRATEDELAY)) != null) {
			this.replyRateDelay = Integer.valueOf(prop);
		}

		if ((prop = propNotNullOrEmpty(PROP_BOTMASTERS)) != null) {
			String[] parts = prop.split(",");
			for (String part : parts) {
				this.botmasters.add(part.trim());
			}
		}

		if ((prop = propNotNullOrEmpty(PROP_HTTPGET_RATEDELAY)) != null) {
			this.httpGetRateDelay = Integer.parseInt(prop);
		}

	}

	private String propNotNullOrEmpty(String propName) throws IrcException {
		String prop = props.getProperty(propName);
		if (prop == null || prop.isEmpty()) {
			throw new IrcException("Property not set: " + propName);
		}
		return prop;
	}

	public int getHttpGetRateDelayMs() {
		return httpGetRateDelay;
	}

	public static Configuration getSelf() {
		if (self == null) {
			self = new Configuration();
		}
		return self;
	}

	public String getNick() {
		return nick;
	}

	public List<String> getChannels() {
		return channels;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getName() {
		return name;
	}

	public int getReplyRateDelay() {
		return replyRateDelay;
	}

	public List<String> getBotmasters() {
		return botmasters;
	}

	public boolean isBotmaster(String nick) {
		return botmasters.contains(nick);
	}

	public void addBotmaster(String nick) {
		botmasters.add(nick);
	}

	/**
	 * Get other configuration options. May return null if not set.
	 */
	public String getOther(String propName) {
		return props.getProperty(propName);
	}
}
