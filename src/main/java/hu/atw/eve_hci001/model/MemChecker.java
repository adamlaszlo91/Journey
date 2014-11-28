package hu.atw.eve_hci001.model;

import hu.atw.eve_hci001.view.GUI;

/**
 * Real-time információt ad a memóriaállapotról.
 * 
 * @author Ádám László
 * 
 */
public class MemChecker implements Runnable {
	private Thread t;
	private GUI gui;
	int mB = 1024 * 1024;

	/**
	 * 
	 * @param gui
	 *            A grafikus interfész.
	 */
	public MemChecker(GUI gui) {
		this.gui = gui;
	}

	/**
	 * A szál elindítására szolgáló metódus.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * A szál leállítására szolgáló metódus.
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
		/* Várakozás, amíg a GUI kész nem lesz fogadni az adatokat */
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
