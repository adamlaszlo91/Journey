package hu.atw.eve_hci001.control;

import hu.atw.eve_hci001.view.JourneyGUI;

/**
 * This thread gives real-time information about the memory state and indicates
 * it on the JourneyGUI.
 * 
 * @author László Ádám
 * 
 */
public class JourneyMemChecker implements Runnable {
	private Thread t;
	private JourneyGUI journeyGUI;
	int mB = 1024 * 1024;

	/**
	 * Constructor for the JourneyMemChecker class.
	 * 
	 * @param journeyGUI
	 *            The graphical interface.
	 */
	public JourneyMemChecker(JourneyGUI journeyGUI) {
		this.journeyGUI = journeyGUI;
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
	 * Periodically sends information about the memory state to the JourneyGUI object.
	 */
	public void run() {
		Thread thisThread = Thread.currentThread();
		Runtime runtime = Runtime.getRuntime();
		/* waiting until the JourneyGUI is ready to receive the data */
		try {
			synchronized (this.t) {
				this.t.wait();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		while (this.t == thisThread) {
			try {
				this.journeyGUI.setMemData(
						(int) (runtime.totalMemory() - runtime.freeMemory())
								/ mB, (int) runtime.totalMemory() / mB,
						(int) runtime.maxMemory() / mB);

				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("JourneyMemChecker: " + e);
			}
		}
	}
}
