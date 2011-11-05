package forskbot.messagehandlers;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.ContentEncodingHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import forskbot.Configuration;
import forskbot.IIrcMessageHandler;
import forskbot.IrcException;
import forskbot.irc.IrcWriter;

/**
 * Titlebot functionality.
 * 
 * @TODO obs, if the bot acquires channel joining capability, it will statically
 *       expand the channelsOperatedIn witout limit, fix.
 * @TODO maybe make it do https?
 * @TODO request timeout
 * @TODO optimize
 * @TODO impose better limits
 * 
 * @author noname
 * 
 */
public class IrcTitleHandler implements IIrcMessageHandler {

	private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64; rv:7.0.1) Gecko/20100101 Firefox/7.0.1";
	private boolean downloadActive = false;
	private Pattern urlPattern = Pattern.compile("^(?i:http://)?[a-zA-Z0-9-\\.]+\\.(?i:com|se|org|mobi|net|tv|eu|us|il)(/.*)?"); // Approximate.
	private Pattern titlePattern = Pattern.compile("^.*(<[\\s+]?(?i:title)[\\s+]?>(.*?)<[\\s+]?/[\\s+]?(?i:title)[\\s+]?>).*$");
	private volatile HttpClient client;
	private ExecutorService pool;
	private int httpGetFloodDelay = 1000;
	private int maxRedirects = 10; // times 4 per httpGetFloodDelay from channel
	private final int maxMatchesPerLine = 4;
	private Map<String, Long> channelsOperatedIn;
	private Logger log = Logger.getLogger(IrcTitleHandler.class);

	public IrcTitleHandler() throws IrcException {
		pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		httpGetFloodDelay = Configuration.getSelf().getHttpGetRateDelayMs();
		channelsOperatedIn = new WeakHashMap<String, Long>();
		initClient();
	}
	
	private void initClient() throws IrcException {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "UTF-8");
		params.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
		params.setParameter(ClientPNames.MAX_REDIRECTS, maxRedirects);
		
		ArrayList<Header> hdr = new ArrayList<Header>();
		hdr.add(new BasicHeader("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7"));
		hdr.add(new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));
		hdr.add(new BasicHeader("Accept-Language", "en"));
		
		params.setParameter(ClientPNames.DEFAULT_HEADERS, hdr);
		
		ClientConnectionManager cm = new ThreadSafeClientConnManager();
		
		client = new ContentEncodingHttpClient(cm, params);

	}

	@Override
	public void onChannelMessage(IrcWriter writer, String fromNick, String fromNickHostmask, String fromChannel, String message) {

		String[] parts = message.split("\\s+");
		log.debug("Entering " + Arrays.toString(parts) + " msg: " + message);

		int matchesThisLine = 0;
		for (String fromPartsPart : parts) {
			String part = fromPartsPart.trim();
			
			if (urlPattern.matcher(part).matches()) {
				log.debug("Matched");
				
				
				if (isTooFastFromChannel(fromChannel)) {
					log.debug("Receiving messages too fast from " + fromChannel + " dropping.");
					return;
				}
				
				if (matchesThisLine == maxMatchesPerLine) {
					log.debug("max matches per line");
					return;
				}

				pool.execute(new TitleHandler(fromChannel, part, writer));

				matchesThisLine = matchesThisLine + 1;
			}
		}
	}

	private boolean isTooFastFromChannel(String channel) {
		Long last = channelsOperatedIn.get(channel);
		long now = System.currentTimeMillis();
		if (last != null && (now - last) < httpGetFloodDelay) {
			return true;
		} else {
			channelsOperatedIn.put(channel, now);
		}
		return false;
	}

	private class TitleHandler implements Runnable {
		private String channel;
		private IrcWriter writer;
		private String url;

		public TitleHandler(String channel, String url, IrcWriter writer) {
			log.debug("Matched url title handling: " + channel + " url: " + url + " writer: " + writer);
			this.channel = channel;
			this.writer = writer;
			this.url = url;
		}

		private Pattern httpStart = Pattern.compile("^(?i:http://).*$");
		public void run() {
			Matcher m = httpStart.matcher(url);
			if(!m.matches()) {
				url = "http://" + url;
			}
			
			try {
				HttpGet get = new HttpGet(URI.create(url));

				String title = client.execute(get, new PageDownloadHandler());
				if(title != null) {
					writer.writePublic(channel, title);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw new RuntimeException(e);
			}
		}
	}
	
	private class PageDownloadHandler implements ResponseHandler<String> {

		@Override
		public String handleResponse(HttpResponse resp) throws ClientProtocolException, IOException {
			HttpEntity entity = resp.getEntity();
			if(entity != null) {
				String page = EntityUtils.toString(entity).replaceAll("\\s+", " ");
				Matcher m = titlePattern.matcher(page);
				if(m.find()) {
					String matchedTitle = m.group(2).trim();
					return matchedTitle;
				}
			}
			return null;
		}
		
	}

	@Override
	public void onPrivateMessage(IrcWriter writer, String fromNick, String fromNickHostmask, String message) {
		// no-op
	}

}
