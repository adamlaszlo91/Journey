package hu.atw.eve_hci001.model;

import hu.atw.eve_hci001.control.Control;

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

/**
 * It's a thread, checks a webpage with a given URL address and collects e-mail
 * addresses and further URL addresses from it.
 * 
 * @author László Ádám
 * 
 */
public class Crawler implements Runnable {
	private Thread t;
	private String link;
	private Control control;
	/* JSoup data members */
	private Document doc;
	private Elements links;
	private URL url;
	/* containers */
	private ArrayList<String> atReplacements;
	private ArrayList<String> eMailAddresses;
	private ArrayList<String> urlAddresses;
	private ArrayList<String> toThrowAway;

	/**
	 * Constructor for the Crawler class.
	 * 
	 * @param control
	 *            The controller object.
	 */
	public Crawler(Control control) {
		this.control = control;
		this.eMailAddresses = new ArrayList<String>();
		this.urlAddresses = new ArrayList<String>();
		this.atReplacements = new ArrayList<String>();
		this.atReplacements.add("[kukac]");
		this.atReplacements.add("[at]");
		this.toThrowAway = new ArrayList<String>();
		this.toThrowAway.add("mailto:");
		this.toThrowAway.add("href:");
		this.toThrowAway.add("%20");
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
	 * The run() method of the thread.<br>
	 * Collects e-mail and URL addresses from web pages.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			/* getting next URL */
			this.link = this.control.getNextURLAddress();
			if (this.link != null) {
				try {
					url = new URL(link);
					this.doc = Jsoup.connect(link).get();
					/* looking for e-mail addresses */
					this.mailCheck(doc.toString());
					/* looking for further URL addresses */
					this.links = doc.select("a[href]");
					for (Element elink : this.links) {
						String l = elink.attr("href");
						if (l.contains("javascript") || l.contains("mailto:")
								|| url.getHost().equals(""))
							continue;
						if (!l.startsWith("http")) {
							this.urlAddresses
									.add("http://" + url.getHost() + l);
						} else {
							this.urlAddresses.add(l);
						}
					}
					/* ignore these exceptions */
				} catch (UnknownHostException uhe) {
				} catch (SocketTimeoutException ste) {
				} catch (HttpStatusException hse) {
				} catch (UnsupportedMimeTypeException hse) {
				} catch (SSLHandshakeException she) {
				} catch (Exception e) {
					System.out.println(e);
				}
			} else {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.out.println("Crawler: " + e);
				}
			}
			/* delivering gathered information and clearing containers */
			this.control.addEMailAddresses(this.eMailAddresses);
			this.control.addURLAddresses(this.urlAddresses);
			this.eMailAddresses.clear();
			this.urlAddresses.clear();
		}
	}

	/**
	 * Looks for e-mail addresses in a given webpage.
	 * 
	 * @param page
	 *            The actual webpage in string format.
	 */
	public void mailCheck(String page) {
		String tokens[] = page.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			/* restoring substituted tokens */
			for (int k = 0; k < this.atReplacements.size(); k++) {
				tokens[i] = tokens[i].replace(this.atReplacements.get(k), "@");
			}
			/* looking for "@" character */
			if (tokens[i].contains("@")) {
				/* further filtering */
				tokens[i] = this.selectAmongQuotes(tokens[i]);
				tokens[i] = this.selectAmongLtGt(tokens[i]);
				tokens[i] = this.selectAmongQuestionMarks(tokens[i]);
				for (int k = 0; k < this.toThrowAway.size(); k++) {
					tokens[i] = tokens[i].replace(this.toThrowAway.get(k), "");
				}
				String tokens2[] = tokens[i].split("@");
				if (tokens2.length != 2) {
					continue;
				}
				if (tokens2[0].length() == 0 || tokens2[1].length() == 0) {
					continue;
				}
				String tokens3[] = tokens2[1].split("\\.");
				if (tokens3.length != 2) {
					continue;
				}
				if (tokens3[0].length() == 0 || tokens3[1].length() == 0) {
					continue;
				}
				/* e-mail validation */
				if (EmailValidator.getInstance().isValid(tokens[i]))
					this.eMailAddresses.add(tokens[i]);

			}
		}
	}

	/**
	 * Removes HTML tags from a text.
	 * 
	 * @param s
	 *            The text to be cleared form HTML tags.
	 * @return The text without HTML tags.
	 */
	private String selectAmongLtGt(String s) {
		String temp = "";
		int c = 0;
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '<') {
				c++;
			} else if (s.charAt(i) == '>') {
				c--;
			} else if (c == 0) {
				temp = temp + s.charAt(i);
			}
		}
		return temp;
	}

	/**
	 * Detects and removes tokens separated with apostrophes.
	 * 
	 * @param s
	 *            A text to be filtered.
	 * @return The token that contains the "@" character.
	 */
	private String selectAmongQuotes(String s) {
		if (!s.contains("\""))
			return s;
		String[] temp = s.split("\"");
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].contains("@"))
				return temp[i];
		}
		return s;
	}

	/**
	 * Detects and removes tokens separated with question marks.
	 * 
	 * @param s
	 *            A text to be filtered.
	 * @return The token that contains the "?" character.
	 */
	private String selectAmongQuestionMarks(String s) {
		if (!s.contains("?"))
			return s;
		String[] temp = s.split("\\?");
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].contains("@"))
				return temp[i];
		}
		return s;
	}

}
