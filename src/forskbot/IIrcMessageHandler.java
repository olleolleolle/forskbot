package forskbot;

import forskbot.irc.IrcWriter;

public interface IIrcMessageHandler {

	/**
	 * Catch-all method. You'll have to parse commands from it yourself.
	 */
	public void onChannelMessage(IrcWriter writer, String fromNick, String fromNickHostmask, String fromChannel, String message);

	public void onPrivateMessage(IrcWriter writer, String fromNick, String fromNickHostmask, String message);

}
