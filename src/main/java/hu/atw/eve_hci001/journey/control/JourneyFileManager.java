package hu.atw.eve_hci001.journey.control;

import java.io.FileWriter;
import java.io.IOException;

public class JourneyFileManager {

	private static JourneyFileManager sInstance;

	private FileWriter writer;

	private JourneyFileManager() {
		try {
			this.writer = new FileWriter("../emails_" + System.currentTimeMillis() + ".txt", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static JourneyFileManager getInstance() {
		if (sInstance == null) {
			sInstance = new JourneyFileManager();
		}
		return sInstance;
	}

	public void saveEmal(String email) {
		try {
			writer.write(email + "\r\n");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
