package hu.atw.eve_hci001.journey.control;

import java.util.ArrayList;

import hu.atw.eve_hci001.journey.util.CombinedList;
import hu.atw.eve_hci001.journey.view.JourneyGUI;

/**
 * JourneyController class for the Journey.
 * 
 * @author László Ádám
 * 
 */
public class JourneyController {
	private CombinedList<String> urlAddresses;
	private CombinedList<String> eMailAddresses;
	private ArrayList<JourneyCrawler> journeyCrawlers;

	private JourneyGUI journeyGUI;
	private JourneyMemChecker journeyMemChecker;

	private int threadNum;
	private int linkIndex;

	private Object linkLock;
	private Object mailLock;

	/**
	 * Constructor
	 */
	public JourneyController() {
		this.linkLock = new Object();
		this.mailLock = new Object();
		this.journeyGUI = new JourneyGUI(this);
		this.journeyGUI.start();
		this.journeyMemChecker = new JourneyMemChecker(journeyGUI);
		this.journeyMemChecker.start();
		this.urlAddresses = new CombinedList<String>();
		this.eMailAddresses = new CombinedList<String>();
		this.journeyCrawlers = new ArrayList<JourneyCrawler>();
	}

	/**
	 * Cleans up data
	 */
	private void clean() {
		this.urlAddresses.clear();
		this.eMailAddresses.clear();
		this.journeyCrawlers.clear();
		this.journeyGUI.setURLChecked(0);
		this.journeyGUI.setEMailFound(0);
		this.journeyGUI.setInQueue(0);
		this.linkIndex = -1;
	}

	/**
	 * Starts crawling.
	 * 
	 * @param startURL
	 *            The start URL.
	 * @param maxThreadNum
	 *            The number of threads to be used.
	 */
	public void startCrawling(String startURL, int threadNum) {
		stopCrawling();
		clean();

		// Add protocol
		if (!startURL.startsWith("http://") && !startURL.startsWith("https://")) { // https? meh...
			if (startURL.startsWith("www.")) {
				startURL = "http://" + startURL;
			} else {
				startURL = "http://www." + startURL;
			}
		}
		this.urlAddresses.add(startURL);
		this.threadNum = threadNum;

		this.journeyGUI.clearOutput();
		this.journeyGUI.println("Start from:\n" + startURL + "\n\n");

		/* Initializing crawlers */
		for (int i = 1; i <= this.threadNum; i++) {
			JourneyCrawler journeyCrawler = new JourneyCrawler(this);
			this.journeyCrawlers.add(journeyCrawler);
			journeyCrawler.start();
		}
	}

	/**
	 * Stops current crawling.
	 */
	public void stopCrawling() {
		for (JourneyCrawler crawler : this.journeyCrawlers) {
			crawler.stop();
		}
	}

	/**
	 * Exits the program.
	 */
	public void exit() {
		stopCrawling();
		this.journeyMemChecker.stop();
		this.journeyGUI.stop();
		System.exit(0);
	}

	/**
	 * Adds URL-s to the queue.
	 * 
	 * @param links
	 *            List of the new URL-s.
	 */
	public void addURLAddresses(CombinedList<String> links) {
		synchronized (this.linkLock) {
			for (String link : links) {
				if (!this.urlAddresses.contains(link)) {
					this.urlAddresses.add(link);
				}
			}
		}
	}

	/**
	 * Adds e-mail addresses to the found addresses.
	 * 
	 * @param eMailAddresses
	 *            List of e-mail addresses.
	 */
	public void addEMailAddresses(CombinedList<String> eMailAddresses) {
		synchronized (this.mailLock) {
			for (String email : eMailAddresses) {
				if (!this.eMailAddresses.contains(email)) {
					this.eMailAddresses.add(email);
					this.journeyGUI.setEMailFound(this.eMailAddresses.size());
					this.journeyGUI.println(email);
					JourneyFileManager.getInstance().saveEmal(email);
				}
			}
		}
	}

	/**
	 * Returns the next URL to be checked.
	 * 
	 * @return URL of the next web page.
	 */
	public String getNextURLAddress() {
		synchronized (this.linkLock) {
			if (this.urlAddresses.size() > this.linkIndex + 1) {
				this.linkIndex++;
				this.journeyGUI.setURLChecked(this.linkIndex + 1);
				this.journeyGUI.setInQueue(this.urlAddresses.size() - this.linkIndex);
				return this.urlAddresses.get(this.linkIndex);
			} else {
				return null;
			}
		}
	}

	/**
	 * Intended to be called when the JourneyGUI is ready Starts the memory checker
	 * thread.
	 */
	public void onGUIReady() {
		synchronized (this.journeyMemChecker.getThread()) {
			this.journeyMemChecker.getThread().notify();
		}
	}
}
