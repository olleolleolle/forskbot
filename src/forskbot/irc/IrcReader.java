package forskbot.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import org.apache.log4j.Logger;

public class IrcReader {

	private Logger log = Logger.getLogger(IrcReader.class.getName());
	private BufferedReader reader;

	public IrcReader(Socket socket) throws IOException {
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public String rawRead() throws IOException {
		String read = reader.readLine();
		if (read != null) {
			log.info(read);
		}
		return read;
	}

	public void close() throws IOException {
		reader.close();
	}

}
