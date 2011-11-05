package forskbot;

import org.apache.log4j.Logger;

public class IrcException extends Exception {

	private Logger log = Logger.getLogger(IrcException.class);
	private static final long serialVersionUID = 1L;

	public IrcException(String message, Throwable t) {
		super(message, t);
		log.error(message, t);
	}

	public IrcException(String message) {
		super(message);
		log.error(message, this);
	}

	public IrcException(Throwable t) {
		super(t);
		log.error(getMessage(), t);
	}

}
