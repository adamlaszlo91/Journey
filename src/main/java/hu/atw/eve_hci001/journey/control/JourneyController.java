package hu.atw.eve_hci001.journey.control;

import java.util.ArrayList;

import hu.atw.eve_hci001.journey.view.JourneyGUI;

/**
 * JourneyController class for the Journey.
 * 
 * @author László Ádám
 * 
 */
public class JourneyController {
	private ArrayList<String> urlAddresses;
	private ArrayList<String> eMailAddresses;
	private ArrayList<JourneyCrawler> journeyCrawlers;
	private int maxThreadNum;
	private JourneyGUI journeyGUI;
	private JourneyMemChecker journeyMemChecker;
	private int linkIndex;
	/* locks for synchronized blocks */
	private Object linkSync;
	private Object mailSync;

	/**
	 * Constructor for the JourneyController class.
	 */
	public JourneyController() {
		this.linkSync = new Object();
		this.mailSync = new Object();
		this.journeyGUI = new JourneyGUI(this);
		this.journeyGUI.start();
		this.journeyMemChecker = new JourneyMemChecker(journeyGUI);
		this.journeyMemChecker.start();
		this.urlAddresses = new ArrayList<String>();
		this.eMailAddresses = new ArrayList<String>();
	}

	/**
	 * Starts crawling.
	 * 
	 * @param url
	 *            Tha start URL.
	 * @param maxThreadNum
	 *            The number of threads to be used.
	 */
	public void init(String url, int maxThreadNum) {
		/* completing input URL */
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			if (url.startsWith("www.")) {
				url = "http://" + url;
			} else {
				url = "http://www." + url;
			}
		}
		/* clearing containers - crawling may be started multiple times */
		this.urlAddresses.clear();
		this.eMailAddresses.clear();
		this.urlAddresses.add(url);
		this.maxThreadNum = maxThreadNum;
		this.journeyGUI.setURLChecked(0);
		this.journeyGUI.setEMailFound(0);
		this.journeyGUI.setInQueue(0);
		this.linkIndex = 0;
		/* initielizing journeyCrawlers */
		this.journeyCrawlers = new ArrayList<JourneyCrawler>();
		for (int i = 1; i <= this.maxThreadNum; i++) {
			JourneyCrawler journeyCrawler = new JourneyCrawler(this);
			this.journeyCrawlers.add(journeyCrawler);
			journeyCrawler.start();
		}
		this.journeyGUI.clearOutput();
		this.journeyGUI.println("Start from:\n" + url + "\n\n");
	}

	/**
	 * Stops current crawling.
	 */
	public void stop() {
		for (int i = 0; i < this.maxThreadNum; i++) {
			this.journeyCrawlers.get(i).stop();
		}
	}

	/**
	 * Exits the program.
	 */
	public void exit() {
		if (this.journeyCrawlers != null && this.journeyCrawlers.size() != 0) {
			for (int i = 0; i < this.maxThreadNum; i++) {
				this.journeyCrawlers.get(i).stop();
			}
		}
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
	public void addURLAddresses(ArrayList<String> links) {
		synchronized (this.linkSync) {
			for (int i = 0; i < links.size(); i++) {
				if (!this.urlAddresses.contains(links.get(i))) {
					this.urlAddresses.add(links.get(i));
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
	public void addEMailAddresses(ArrayList<String> eMailAddresses) {
		synchronized (this.mailSync) {
			for (int i = 0; i < eMailAddresses.size(); i++) {
				if (!this.eMailAddresses.contains(eMailAddresses.get(i))) {
					this.eMailAddresses.add(eMailAddresses.get(i));
					this.journeyGUI.setEMailFound(this.eMailAddresses.size());
					this.journeyGUI.println(eMailAddresses.get(i));
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
		synchronized (this.linkSync) {
			if (this.urlAddresses.size() > this.linkIndex) {
				String temp = this.urlAddresses.get(this.linkIndex);
				this.linkIndex++;
				this.journeyGUI.setURLChecked(this.linkIndex);
				this.journeyGUI.setInQueue(this.urlAddresses.size() - this.linkIndex);
				return temp;
			}
			return null;
		}
	}

	/**
	 * Intended to be called when the JourneyGUI is ready- <br>
	 * Starts the memory checker thread.
	 */
	public void guiReady() {
		synchronized (this.journeyMemChecker.getT()) {
			this.journeyMemChecker.getT().notify();
		}
	}
}
