package hu.atw.eve_hci001.journey.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.DefaultCaret;

import hu.atw.eve_hci001.journey.control.JourneyController;

/**
 * Graphical interface for Journey.
 * 
 * @author László Ádám
 * 
 */
public class JourneyGUI implements Runnable, ActionListener {
	private Thread t;
	private JourneyController journeyController;
	private JButton start;
	private JButton stop;
	private JTextField url;
	private JComboBox<String> threads;
	private JTextArea eMails;
	private JLabel label3;
	private JLabel label4;
	private JLabel label5;
	private JLabel label6;
	private JScrollPane scroll;

	/**
	 * Constructor of the JourneyGUI class.
	 * 
	 * @param journeyController
	 *            The controller object.
	 */
	public JourneyGUI(JourneyController journeyController) {
		this.journeyController = journeyController;
	}

	/**
	 * The run() method of the thread. CInitielizes the JourneyGUI elements and
	 * waits for interaction.
	 */
	public void run() {
		JFrame frame = new JFrame("Journey 0.2.1");
		frame.setSize(800, 600);
		frame.setLayout(null);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				close();
			}
		});

		JLabel label1 = new JLabel("Start:");
		label1.setLocation(10, 10);
		label1.setSize(40, 20);
		label1.setVisible(true);
		frame.add(label1);

		this.url = new JTextField();
		this.url.setSize(400, 20);
		this.url.setLocation(55, 10);
		this.url.setVisible(true);
		this.url.addActionListener(this);
		frame.add(this.url);

		JLabel label2 = new JLabel("Threads:");
		label2.setLocation(480, 10);
		label2.setSize(50, 20);
		label2.setVisible(true);
		frame.add(label2);

		String[] darab = { "1", "2", "5", "10", "20", "40", "80" };
		this.threads = new JComboBox<String>(darab);
		this.threads.setLocation(535, 10);
		this.threads.setSize(50, 20);
		this.threads.setVisible(true);
		frame.add(this.threads);

		this.start = new JButton("Start");
		this.start.setLocation(610, 10);
		this.start.setSize(80, 20);
		this.start.setVisible(true);
		this.start.addActionListener(this);
		frame.add(this.start);

		this.stop = new JButton("Stop");
		this.stop.setLocation(700, 10);
		this.stop.setSize(80, 20);
		this.stop.setVisible(true);
		this.stop.addActionListener(this);
		this.stop.setEnabled(false);
		frame.add(this.stop);

		this.label3 = new JLabel("0 link is checked");
		this.label3.setLocation(10, 50);
		this.label3.setSize(150, 20);
		this.label3.setVisible(true);
		frame.add(this.label3);

		this.label4 = new JLabel("0 link is in queue");
		this.label4.setLocation(170, 50);
		this.label4.setSize(150, 20);
		this.label4.setVisible(true);
		frame.add(this.label4);

		this.label5 = new JLabel("0 email is found");
		this.label5.setLocation(340, 50);
		this.label5.setSize(150, 20);
		this.label5.setVisible(true);
		frame.add(this.label5);

		this.label6 = new JLabel("Memory (MB):  ? / ?     Max: ?");
		this.label6.setLocation(550, 50);
		this.label6.setSize(250, 20);
		this.label6.setVisible(true);
		frame.add(this.label6);

		JSeparator separator = new JSeparator();
		separator.setLocation(10, 80);
		separator.setSize(775, 20);
		separator.setVisible(true);
		frame.add(separator);

		this.eMails = new JTextArea();
		this.scroll = new JScrollPane(this.eMails);
		this.scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.scroll.setLocation(10, 100);
		this.scroll.setSize(775, 430);
		this.scroll.setVisible(true);
		this.eMails.setEditable(false);

		DefaultCaret caret = (DefaultCaret) this.eMails.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		frame.add(this.scroll);

		frame.setVisible(true);
		this.journeyController.onGUIReady();
	}

	/**
	 * Method to start the thread.
	 */
	public void start() {
		this.t = new Thread(this);
		this.t.start();
	}

	/**
	 * MEthod to stop the thread.
	 */
	public void stop() {
		this.t = null;
	}

	/**
	 * Handles interactions.
	 */
	public void actionPerformed(ActionEvent e) {
		/* on Enter press or Start button press */
		if (e.getSource() == this.start || e.getSource() == this.url) {
			if (this.url.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "Please add a starting URL!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			/* enabling/disabling buttons */
			this.start.setEnabled(false);
			this.stop.setEnabled(true);
			int i = Integer.parseInt(this.threads.getSelectedItem().toString());
			/* start crawling */
			this.journeyController.startCrawling(this.url.getText(), i);
		}
		if (e.getSource() == this.stop) {
			/* enabling/disabling buttons */
			this.stop.setEnabled(false);
			this.start.setEnabled(true);
			/* stop crawling */
			this.journeyController.stopCrawling();
		}
	}

	/**
	 * Prints a text (e-mail address) on the JourneyGUI.
	 * 
	 * @param s
	 *            The text to be printed.
	 */
	public synchronized void println(String s) {
		String text = this.eMails.getText();
		this.eMails.setText(text + "\n" + s);
	}

	public void setURLChecked(int i) {
		this.label3.setText(i + " link(s) are checked");
	}

	public void setInQueue(int i) {
		this.label4.setText(i + " link(s) are in queue");
	}

	public void setEMailFound(int i) {
		this.label5.setText(i + " email(s) are found");
	}

	public void clearOutput() {
		this.eMails.setText("");
	}

	/**
	 * Set informationd about the memory state.
	 * 
	 * @param used
	 *            Uses memory.
	 * @param available
	 *            Available memory.
	 * @param max
	 *            The memory reserved by the JVM.
	 */
	public void setMemData(long used, long available, long max) {
		this.label6.setText("Memory (MB):  " + used + " / " + available + "     Max: " + max);
	}

	/**
	 * Exits the program.
	 */
	public void close() {
		this.journeyController.exit();
	}

}
