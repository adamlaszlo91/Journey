package hu.atw.eve_hci001.journey.control;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.validator.routines.EmailValidator;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

/**
 * It's a thread, checks a webpage with a given URL address and collects e-mail
 * addresses and further URL addresses from it.
 * 
 * @author László Ádám
 * 
 */
public class JourneyCrawler implements Runnable {
	private Thread t;
	private String link;
	private JourneyController journeyController;
	/* JSoup data members */
	private Document doc;
	private Elements links;
	private URL url;
	/* Containers */
	private ArrayList<String> eMailAddresses;
	private ArrayList<String> urlAddresses;

	/**
	 * Constructor for the JourneyCrawler class.
	 * 
	 * @param journeyController
	 *            The controller object.
	 */
	public JourneyCrawler(JourneyController journeyController) {
		this.journeyController = journeyController;
		this.eMailAddresses = new ArrayList<String>();
		this.urlAddresses = new ArrayList<String>();
	}

	/**
	 * Method to start the thread.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * Method to stop the Thread.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * The run() method of the thread. Collects e-mail and URL addresses from web
	 * pages.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			/* Getting next URL */
			this.link = this.journeyController.getNextURLAddress();
			if (this.link != null) {
				try {
					url = new URL(link);
					this.doc = Jsoup.connect(link).get();
					/* Looking for e-mail addresses */
					this.mailCheck(doc.toString());
					/* Looking for further URL addresses */
					this.links = doc.select("a[href]");
					for (Element elink : this.links) {
						String l = elink.attr("href");
						// TODO: White list
						if (!l.startsWith("http")) { // https included
							this.urlAddresses.add("http://" + url.getHost() + l);
						} else {
							this.urlAddresses.add(l);
						}
					}
					/* Ignore these exceptions */
				} catch (UnknownHostException uhe) {
				} catch (SocketTimeoutException ste) {
				} catch (HttpStatusException hse) {
				} catch (UnsupportedMimeTypeException hse) {
				} catch (SSLHandshakeException she) {
				} catch (Exception e) {
					System.out.println(e);
				} finally {
					/* Delivering gathered information and clearing containers */
					this.journeyController.addEMailAddresses(this.eMailAddresses);
					this.journeyController.addURLAddresses(this.urlAddresses);
					this.eMailAddresses.clear();
					this.urlAddresses.clear();
				}
			} else {
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	/**
	 * Looks for e-mail addresses in a given webpage.
	 * 
	 * @param page
	 *            The actual webpage in string format.
	 */
	public void mailCheck(String page) {
		
		Matcher m = Pattern.compile(
				// Do not delete, already a custom regex
				"(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")(\\[at\\]|\\[kukac\\]|@)(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
				.matcher(page);
		while (m.find()) {
			if (EmailValidator.getInstance().isValid(m.group())) {
				this.eMailAddresses.add(m.group());
			}
		}
	}

}
