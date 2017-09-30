package hu.atw.eve_hci001.journey.control;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.re2j.Matcher;
import com.google.re2j.Pattern;

import hu.atw.eve_hci001.journey.util.AbstractRunnableThread;
import hu.atw.eve_hci001.journey.util.CombinedList;

/**
 * Checks a webpage with a given URL address and collects e-mail addresses and
 * further URL addresses from it.
 * 
 * @author László Ádám
 * 
 */
public class JourneyCrawler extends AbstractRunnableThread {

	private String currentLink;
	private JourneyController journeyController;
	private CombinedList<String> eMailAddresses;
	private CombinedList<String> urlAddresses;

	private Document doc;
	private Elements collectedLinks;
	private URL url;

	/**
	 * Constructor
	 * 
	 * @param journeyController
	 *            The controller object.
	 */
	public JourneyCrawler(JourneyController journeyController) {
		this.journeyController = journeyController;
		this.eMailAddresses = new CombinedList<String>();
		this.urlAddresses = new CombinedList<String>();
	}

	/**
	 * Collects e-mail and URL addresses from web pages.
	 */
	public void performRun() {
		// Get next available URL from the controller
		this.currentLink = this.journeyController.getNextURLAddress();
		if (this.currentLink != null) {
			try {
				this.url = new URL(currentLink);
				this.doc = Jsoup.connect(currentLink).get();
				/* Looking for e-mail addresses */
				this.findEmailAddresses(doc.toString());
				/* Looking for further URL addresses */
				this.collectedLinks = doc.select("a[href]");
				for (Element elink : this.collectedLinks) {
					String link = elink.attr("href");
					if (!checkLink(link)) {
						continue;
					}
					if (!link.startsWith("http")) { // https included, may not work
						this.urlAddresses.add("http://" + url.getHost() + link);
					} else {
						this.urlAddresses.add(link);
					}
				}
				/* Ignore these exceptions */
			} catch (UnknownHostException uhe) {
			} catch (SocketTimeoutException ste) {
			} catch (HttpStatusException hse) {
			} catch (UnsupportedMimeTypeException hse) {
			} catch (SSLHandshakeException she) {
			} catch (ConnectException ce) {
			} catch (SocketException se) {
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				/* Delivering gathered information and clearing containers */
				this.journeyController.addEMailAddresses(this.eMailAddresses);
				this.journeyController.addURLAddresses(this.urlAddresses);
				this.eMailAddresses.clear();
				this.urlAddresses.clear();
			}
		} else {
			// There is no link in the queue
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Checks if the found link is valid
	 * 
	 * @param link
	 * 
	 */
	private boolean checkLink(String link) {
		return UrlValidator.getInstance().isValid(link);
	}

	/**
	 * Looks for e-mail addresses in a given webpage.
	 * 
	 * @param page
	 *            The actual webpage in string format.
	 */
	private void findEmailAddresses(String page) {
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
