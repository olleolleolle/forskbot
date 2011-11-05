package forskbot.messagehandlers;

import org.apache.log4j.Logger;

import forskbot.Configuration;
import forskbot.IIrcMessageHandler;
import forskbot.irc.IrcWriter;

/**
 * Allows for poll creation/participation. Only botmasters are allowed to create
 * polls.
 * 
 * @TODO not implemented
 * 
 * @author noname
 * 
 */
public class IrcPollHandler implements IIrcMessageHandler {

	private Configuration config;
	private Logger log = Logger.getLogger(IrcPollHandler.class);

	public IrcPollHandler() {
		this.config = Configuration.getSelf();
	}

	@Override
	public void onChannelMessage(IrcWriter writer, String nick, String hostmask, String channelName, String message) {

		// Determine first if it comes from a botmaster if it's a poll
		if (message.startsWith("!poll") && config.isBotmaster(nick)) {

			String[] parts = message.split("\\s+", 3);
			// We want !poll <command> <msg>
			if (parts.length == 3) {
				String cmd = parts[1];

				if (cmd.equals("create")) {

				}

			}

		} else {
			log.warn("Warning, user: " + nick + " attempted to perform a privileged operation: " + message + " in channel: " + channelName);
		}
	}

	@Override
	public void onPrivateMessage(IrcWriter writer, String nick, String hostmask, String message) {
		// no-op
	}

	private class Poll {
		public Poll(String question) {

		}
	}

}
