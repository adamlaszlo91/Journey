package hu.atw.eve_hci001.journey.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

import hu.atw.eve_hci001.journey.util.CombinedList;

public class JourneyFileManager {

	private static JourneyFileManager sInstance;

	private FileWriter writer;
	Connection conn = null;
	private String emailTextPath;
	private String emailDBPath;
	private boolean textOutput;
	private boolean DBOutput;

	PreparedStatement replaceStatement;

	private JourneyFileManager() {

	}

	public static JourneyFileManager getInstance() {
		if (sInstance == null) {
			sInstance = new JourneyFileManager();
		}
		return sInstance;
	}

	public void init(JourneyController controller, String emailTextPath, String emailDBPath, boolean textOutput,
			boolean DBOutput) {
		this.textOutput = textOutput;
		this.DBOutput = DBOutput;

		// Get text output path, import
		if (emailTextPath != null) {
			this.emailTextPath = emailTextPath;
			importEmailText(controller);
		} else {
			this.emailTextPath = "../emails_" + System.currentTimeMillis() + ".txt";
		}
		if (this.textOutput) {
			try {
				this.writer = new FileWriter(this.emailTextPath, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Get db output path, import
		if (emailDBPath != null) {
			this.emailDBPath = emailDBPath;
			importEmailDB(controller);
		} else {
			this.emailDBPath = "jdbc:sqlite:../emails_" + System.currentTimeMillis() + ".sqlite";
		}
		if (this.DBOutput) {
			try {
				// create a connection to the database
				Class.forName("org.sqlite.JDBC");
				conn = DriverManager.getConnection(this.emailDBPath);

				Statement stm = conn.createStatement();
				stm.execute("CREATE TABLE IF NOT EXISTS emailTable (email TEXT PRIMARY KEY);");
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (conn != null) {
						conn.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

	}

	private void importEmailText(JourneyController controller) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(this.emailTextPath)));
			String line;
			CombinedList<String> emails = new CombinedList<String>();
			while ((line = br.readLine()) != null) {
				emails.add(line);
			}
			controller.addEMailAddresses(emails, true);
			System.out.println("Imported emails: " + emails.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void importEmailDB(JourneyController controller) {

	}

	public void exportEmail(String email) {
		if (this.textOutput) {
			try {
				writer.write(email + "\r\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
