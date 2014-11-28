package hu.atw.eve_hci001.model;

import hu.atw.eve_hci001.view.GUI;

/**
 * This thread gives real-time information about the memory state and indicates
 * it on the GUI.
 * 
 * @author László Ádám
 * 
 */
public class MemChecker implements Runnable {
	private Thread t;
	private GUI gui;
	int mB = 1024 * 1024;

	/**
	 * Constructor for the MemChecker class.
	 * 
	 * @param gui
	 *            The graphical interface.
	 */
	public MemChecker(GUI gui) {
		this.gui = gui;
	}

	/**
	 * Method to start the thread.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * Method to stop the thread.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * 
	 * @return The Thread data ember of the class.
	 */
	public Thread getT() {
		return this.t;
	}

	/**
	 * The run() method of the thread.<br>
	 * Periodically sends information about the memory state to the GUI object.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		Runtime runtime = Runtime.getRuntime();
		/* waiting until the GUI is ready to receive the data */
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
