package hu.atw.eve_hci001.journey.util;

/**
 * Wrapper class for threads
 * 
 * @author Ádám László
 *
 */
public abstract class AbstractRunnableThread implements Runnable {
	private Thread t;
	protected boolean repeating = true; // True if the thread needs to do a repetitive job, false for run once

	/**
	 * Method to start the thread.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * Method to stop the Thread.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * Gets the underlying Thread object
	 * 
	 * @return Thread object
	 */
	public Thread getThread() {
		return t;
	}

	public void run() {
		if (repeating) {
			Thread thisThread = Thread.currentThread();
			while (this.t == thisThread) {
				performRun();
			}
		} else {
			performRun();
		}
	}

	/**
	 * Subclasses must implement this
	 */
	public abstract void performRun();

}
