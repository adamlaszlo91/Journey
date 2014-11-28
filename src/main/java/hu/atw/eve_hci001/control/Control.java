package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.model.Crawler;
import hu.atw.eve_hci001.model.MemChecker;
import hu.atw.eve_hci001.view.GUI;

import java.util.ArrayList;

/**
 * Control oszt�ly a Journey-hez.
 * 
 * @author �d�m L�szl�
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
	 * Az e-mail c�mek keres�s�nek ind�t�sa.
	 * 
	 * @param url
	 *            A kiindul� URL.
	 * @param maxThreadNum
	 *            A haszn�lt sz�lak sz�ma.
	 */
	public void init(String url, int maxThreadNum) {
		/* Input url kieg�sz�t�se */
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
	 * A folyamatban l�v� keres�s le�ll�t�sa.
	 */
	public void stop() {
		for (int i = 0; i < this.maxThreadNum; i++) {
			this.crawlers.get(i).stop();
		}
	}

	/**
	 * A program le�ll�t�sa.
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
	 * Linkek hozz�ad�sa a v�rakoz�si sorhoz.
	 * 
	 * @param links
	 *            A hozz�adni k�v�nt linkek list�ja.
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
	 * Tal�lt e-mail c�mek hozz�ad�sa.
	 * 
	 * @param eMailAddresses
	 *            E-mail c�mek list�ja.
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
	 * Visszaadja a v�rakoz�si sorban k�vetkez� oldal UEL-j�t.
	 * 
	 * @return A k�vetkez� �tn�zend� oldal webc�me.
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
	 * A GUI bet�lt�se ut�n megh�v�d� f�ggv�ny, jelz�st k�ld a mem�ria
	 * figyel�nek.
	 */
	public void guiReady() {
		synchronized (this.memChecker.getT()) {
			this.memChecker.getT().notify();
		}
	}
}
