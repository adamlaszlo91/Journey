package hu.atw.eve_hci001.model;

import hu.atw.eve_hci001.control.Control;

import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * E-mail címeket és további linkeket gyûjtõ osztály.
 * 
 * @author Ádám László
 * 
 */
public class Crawler implements Runnable {
	private Thread t;
	private String link;
	private Control control;
	private Document doc;
	private Elements links;
	private URL url;
	private ArrayList<String> atReplacements;
	private ArrayList<String> eMailAddresses;
	private ArrayList<String> urlAddresses;
	private ArrayList<String> toThrowAway;

	/**
	 * 
	 * @param control
	 *            A Conntrol objektum.
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
	 * Metódus a szál elindítására.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * Metódus a szál leállítására.
	 */
	public void stop() {
		this.t = null;
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		while (this.t == thisThread) {
			/* Következõ url beszerzése */
			this.link = this.control.getNextURLAddress();
			if (this.link != null) {
				try {
					url = new URL(link);
					this.doc = Jsoup.connect(link).get();
					/* E-mail címek keresése */
					this.mailCheck(doc.toString());
					/* További linkek keresése */
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
				} catch (UnknownHostException uhe) {
				} catch (SocketTimeoutException ste) {
				} catch (HttpStatusException hse) {
				} catch (UnsupportedMimeTypeException hse) {
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
			this.control.addEMailAddresses(this.eMailAddresses);
			this.control.addURLAddresses(this.urlAddresses);
			this.eMailAddresses.clear();
			this.urlAddresses.clear();
		}
	}

	/**
	 * E-mail címek keresése.
	 * 
	 * @param page
	 *            Az aktuális weboldal szöveges formátumban.
	 */
	public void mailCheck(String page) {
		String tokens[] = page.split(" ");
		for (int i = 0; i < tokens.length; i++) {
			/* Helyettesítõ tokenek visszaállítása */
			for (int k = 0; k < this.atReplacements.size(); k++) {
				tokens[i] = tokens[i].replace(this.atReplacements.get(k), "@");
			}
			/* "@" karakter keresése */
			if (tokens[i].contains("@")) {
				/* további szûrés */
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
				try {
					new InternetAddress(tokens[i]);
					this.eMailAddresses.add(tokens[i]);
				} catch (AddressException ex) {
					/* Nem mailcim */
				}
			}
		}
	}

	/**
	 * Eltávolítja a html tageket a szövegbõl.
	 * 
	 * @param s
	 *            A szûrni kívánt szöveg.
	 * @return A szöveg html tagek nélkül.
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
	 * Kiválasztja a "@" arakterrel rendelkezõ tokent azok közül, amiket
	 * aposztrófok választanak el.
	 * 
	 * @param s
	 *            A szûrni kívánt szöveg.
	 * @return A "@" karaktert tartalmazó token.
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
	 * Kiválasztja a "@" arakterrel rendelkezõ tokent azok közül, amikek
	 * kérdõjelek választanak el.
	 * 
	 * @param s
	 *            A szûrni kívánt szöveg.
	 * @return A "@" karaktert tartalmazó token.
	 */
	private String selectAmongQuestionMarks(String s) {
		if (!s.contains("?"))
			return s;
		String[] temp = s.split("\\?");
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].contains("@"))
				return temp[i];
		}
		// may never happen
		return s;
	}

}
