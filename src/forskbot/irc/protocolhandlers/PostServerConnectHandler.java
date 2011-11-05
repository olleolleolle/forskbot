package forskbot.irc.protocolhandlers;

import forskbot.Configuration;
import forskbot.IIrcProtocolHandler;
import forskbot.IrcException;
import forskbot.irc.IrcWriter;

/**
 * Joins channels upon reading 255 RPL_LUSERME from server.
 * 
 * @TODO Necessary to expect the 255? Or is it even needed to wait some time
 *       before joining?
 * 
 * @author noname
 * 
 */
public class PostServerConnectHandler implements IIrcProtocolHandler {

	@Override
	public void onServerMessage(IrcWriter writer, String subject, String cmd, String rest) throws IrcException {

		writer.rawWrite("PING " + subject.substring(1));

		for (String channel : Configuration.getSelf().getChannels()) {
			writer.rawWrite("JOIN " + channel);

			String joinmsg = Configuration.getSelf().getOther(Configuration.PROP_JOINMSG);
			if (joinmsg != null) {
				writer.writePublic(channel, joinmsg);
			}
		}
	}

}
