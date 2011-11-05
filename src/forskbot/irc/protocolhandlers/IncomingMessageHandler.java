package forskbot.irc.protocolhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import forskbot.Configuration;
import forskbot.IIrcMessageHandler;
import forskbot.IIrcProtocolHandler;
import forskbot.IrcException;
import forskbot.irc.IrcWriter;

/**
 * One can register with this handler to receive channel or private messages.
 * 
 * 
 * This does rate limiting
 * 
 * @author noname
 * 
 */
public class IncomingMessageHandler implements IIrcProtocolHandler {

	private Logger log = Logger.getLogger(IncomingMessageHandler.class);
	private List<IIrcMessageHandler> msgHandlers;
	private Map<String, Long> receivedTimings;
	private long replyRateDelay = 1000;

	public IncomingMessageHandler() {
		msgHandlers = new ArrayList<IIrcMessageHandler>();
		receivedTimings = new WeakHashMap<String, Long>();
		this.replyRateDelay = Configuration.getSelf().getReplyRateDelay();
	}

	@Override
	public void onServerMessage(IrcWriter writer, String subject, String cmd, String rest) throws IrcException {

		if (isDropServerMessage(subject)) {
			return;
		}

		String[] subjLine = subject.substring(1).split("!");
		String subjNick = subjLine[0];
		String subjHostmask = subjLine[1];
		subject = null;

		String[] mnemonicPayload = rest.split("\\s+", 2);
		if (mnemonicPayload != null && mnemonicPayload.length >= 2) {

			String chanOrPriv = mnemonicPayload[0].trim();
			boolean isChannelMessage = chanOrPriv.startsWith("#");

			// We might need to fire on both

			if (isChannelMessage) {
				fireOnChannelMessage(writer, subjNick, subjHostmask, chanOrPriv, mnemonicPayload[1].substring(1));
			}

			if (!isChannelMessage) {
				fireOnPrivateMessage(writer, subjNick, subjHostmask, mnemonicPayload[1].substring(1));
			}

		} else {
			log.error("Invalid message received: " + rest + " from: " + subject);
			return;
		}

	}

	/**
	 * Will attempt to rate-limit messages so it doesn't get flooded by
	 * processing requests. This is on a per-user bases.
	 * 
	 * @param subject
	 * @return
	 */
	private boolean isDropServerMessage(String subject) {
		Long last = receivedTimings.get(subject);
		long now = System.currentTimeMillis();
		if (last != null) {

			if ((now - last) <= replyRateDelay) {
				return true;
			} else {
				receivedTimings.put(subject, now);
				return false;
			}

		} else {
			receivedTimings.put(subject, now);
			return false;
		}
	}

	public void addMessageHandler(IIrcMessageHandler msgHandler) {
		msgHandlers.add(msgHandler);
	}

	private void fireOnChannelMessage(IrcWriter writer, String nick, String hostmask, String channelName, String message) {
		for (IIrcMessageHandler handlers : msgHandlers) {
			handlers.onChannelMessage(writer, nick, hostmask, channelName, message);
		}
	}

	private void fireOnPrivateMessage(IrcWriter writer, String nick, String hostmask, String message) {
		for (IIrcMessageHandler handlers : msgHandlers) {
			handlers.onPrivateMessage(writer, nick, hostmask, message);
		}
	}
}
