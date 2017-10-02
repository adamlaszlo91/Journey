package hu.atw.eve_hci001.journey;

import hu.atw.eve_hci001.journey.control.JourneyController;
import hu.atw.eve_hci001.journey.control.JourneyFileManager;

/**
 * Main class for Journey.
 * 
 * @author László Ádám
 * 
 */
public class Main {

	public static void main(String[] args) {
		JourneyController controller = new JourneyController();
		String emailTextPath = null;
		String emailDBPath = null;
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
			if (args[i].equals("-txte") && i < args.length - 1) {
				emailTextPath = args[i + 1];
			}
			if (args[i].equals("-dbe") && i < args.length - 1) {
				emailDBPath = args[i + 1];
			}
		}
		JourneyFileManager.getInstance().init(controller, emailTextPath, emailDBPath, true, false);
	}

}
