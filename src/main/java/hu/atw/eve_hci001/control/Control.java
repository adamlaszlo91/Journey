package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.Crawler;
import hu.atw.eve_hci001.model.MemChecker;
import hu.atw.eve_hci001.view.GUI;

import java.util.ArrayList;

/**
 * Control class for the Journey.
 * 
 * @author László Ádám
 * 
 */
public class Control {
	private ArrayList<String> urlAddresses;
	private ArrayList<String> eMailAddresses;
	private ArrayList<Crawler> crawlers;
	private int maxThreadNum;
	private GUI gui;
	private MemChecker memChecker;
	private int linkIndex;
	/* locks for synchronized blocks */
	private Object linkSync;
	private Object mailSync;

	/**
	 * Constructor for the Control class.
	 */
	public Control() {
		this.linkSync = new Object();
		this.mailSync = new Object();
		this.gui = new GUI(this);
		this.gui.start();
		this.memChecker = new MemChecker(gui);
		this.memChecker.start();
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
		this.gui.setURLChecked(0);
		this.gui.setEMailFound(0);
		this.gui.setInQueue(0);
		this.linkIndex = 0;
		/* initielizing crawlers */
		this.crawlers = new ArrayList<Crawler>();
		for (int i = 1; i <= this.maxThreadNum; i++) {
			Crawler crawler = new Crawler(this);
			this.crawlers.add(crawler);
			crawler.start();
		}
		this.gui.clearOutput();
		this.gui.println("Start from:\n" + url + "\n\n");
	}

	/**
	 * Stops current crawling.
	 */
	public void stop() {
		for (int i = 0; i < this.maxThreadNum; i++) {
			this.crawlers.get(i).stop();
		}
	}

	/**
	 * Exits the program.
	 */
	public void exit() {
		if (this.crawlers != null && this.crawlers.size() != 0) {
			for (int i = 0; i < this.maxThreadNum; i++) {
				this.crawlers.get(i).stop();
			}
		}
		this.memChecker.stop();
		this.gui.stop();
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
					this.gui.setEMailFound(this.eMailAddresses.size());
					this.gui.println(eMailAddresses.get(i));
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
				this.gui.setURLChecked(this.linkIndex);
				this.gui.setInQueue(this.urlAddresses.size() - this.linkIndex);
				return temp;
			}
			return null;
		}
	}

	/**
	 * Intended to be called when the GUI is ready- <br>
	 * Starts the memory checker thread.
	 */
	public void guiReady() {
		synchronized (this.memChecker.getT()) {
			this.memChecker.getT().notify();
		}
	}
}
