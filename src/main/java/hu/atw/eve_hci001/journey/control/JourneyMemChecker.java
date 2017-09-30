package hu.atw.eve_hci001.journey.control;

import hu.atw.eve_hci001.journey.util.AbstractRunnableThread;
import hu.atw.eve_hci001.journey.view.JourneyGUI;

/**
 * This thread gives real-time information about the memory state and shows it
 * on the JourneyGUI.
 * 
 * @author László Ádám
 * 
 */
public class JourneyMemChecker extends AbstractRunnableThread {
	private JourneyGUI journeyGUI;
	long mB = 1024 * 1024;
	boolean guiStarted;

	/**
	 * for the JourneyMemChecker class.
	 * 
	 * @param journeyGUI
	 *            The graphical interface.
	 */
	public JourneyMemChecker(JourneyGUI journeyGUI) {
		this.journeyGUI = journeyGUI;
	}

	/**
	 * Periodically sends information about the memory state to the JourneyGUI
	 * object.
	 */
	public void performRun() {
		/* Waiting until the JourneyGUI is ready to receive the data */
		if (!guiStarted) {
			try {
				synchronized (this.getThread()) {
					this.getThread().wait();
					guiStarted = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			Runtime runtime = Runtime.getRuntime();
			this.journeyGUI.setMemData((runtime.totalMemory() - runtime.freeMemory()) / mB, runtime.totalMemory() / mB,
					runtime.maxMemory() / mB);
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
