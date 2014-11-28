package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.Crawler;
import hu.atw.eve_hci001.model.MemChecker;
import hu.atw.eve_hci001.view.GUI;

import java.util.ArrayList;

/**
 * Control osztály a Journey-hez.
 * 
 * @author Ádám László
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
	private Object linkSync;
	private Object mailSync;

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
	 * Az e-mail címek keresésének indítása.
	 * 
	 * @param url
	 *            A kiinduló URL.
	 * @param maxThreadNum
	 *            A használt szálak száma.
	 */
	public void init(String url, int maxThreadNum) {
		/* Input url kiegészítése */
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			if (url.startsWith("www.")) {
				url = "http://" + url;
			} else {
				url = "http://www." + url;
			}
		}
		this.urlAddresses.clear();
		this.eMailAddresses.clear();
		this.urlAddresses.add(url);
		this.maxThreadNum = maxThreadNum;
		this.gui.setURLChecked(0);
		this.gui.setEMailFound(0);
		this.gui.setInQueue(0);
		this.linkIndex = 0;
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
	 * A folyamatban lévõ keresés leállítása.
	 */
	public void stop() {
		for (int i = 0; i < this.maxThreadNum; i++) {
			this.crawlers.get(i).stop();
		}
	}

	/**
	 * A program leállítása.
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
	 * Linkek hozzáadása a várakozási sorhoz.
	 * 
	 * @param links
	 *            A hozzáadni kívánt linkek listája.
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
	 * Talált e-mail címek hozzáadása.
	 * 
	 * @param eMailAddresses
	 *            E-mail címek listája.
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
	 * Visszaadja a várakozási sorban következõ oldal UEL-jét.
	 * 
	 * @return A következõ átnézendõ oldal webcíme.
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
	 * A GUI betöltése után meghívódó függvény, jelzést küld a memória
	 * figyelõnek.
	 */
	public void guiReady() {
		synchronized (this.memChecker.getT()) {
			this.memChecker.getT().notify();
		}
	}
}
