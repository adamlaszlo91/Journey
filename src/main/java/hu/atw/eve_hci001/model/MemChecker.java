package hu.atw.eve_hci001.model;

import hu.atw.eve_hci001.view.GUI;

/**
 * Real-time inform�ci�t ad a mem�ria�llapotr�l.
 * 
 * @author �d�m L�szl�
 * 
 */
public class MemChecker implements Runnable {
	private Thread t;
	private GUI gui;
	int mB = 1024 * 1024;

	/**
	 * 
	 * @param gui
	 *            A grafikus interf�sz.
	 */
	public MemChecker(GUI gui) {
		this.gui = gui;
	}

	/**
	 * A sz�l elind�t�s�ra szolg�l� met�dus.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * A sz�l le�ll�t�s�ra szolg�l� met�dus.
	 */
	public void stop() {
		this.t = null;
	}

	public Thread getT() {
		return this.t;
	}

	public void run() {
		Thread thisThread = Thread.currentThread();
		Runtime runtime = Runtime.getRuntime();
		/* V�rakoz�s, am�g a GUI k�sz nem lesz fogadni az adatokat */
		try {
			synchronized (this.t) {
				this.t.wait();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		while (this.t == thisThread) {
			try {
				this.gui.setMemData(
						(int) (runtime.totalMemory() - runtime.freeMemory())
								/ mB, (int) runtime.totalMemory() / mB,
						(int) runtime.maxMemory() / mB);

				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("MemChecker: " + e);
			}
		}
	}
}
