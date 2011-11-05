package forskbot;

import forskbot.irc.IrcWriter;

/**
 * A stateful protocol handler.
 * 
 * @author noname
 * 
 */
public interface IIrcProtocolHandler {

	/**
	 * 
	 * Example: <:irc.freenode.com> <376> <:End of /MOTD command.>
	 */
	public void onServerMessage(IrcWriter writer, String subject, String cmd, String rest) throws IrcException;

}
