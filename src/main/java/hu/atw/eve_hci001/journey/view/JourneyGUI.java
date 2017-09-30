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
import hu.atw.eve_hci001.journey.util.AbstractRunnableThread;

/**
 * Graphical interface for Journey.
 * 
 * @author László Ádám
 * 
 */
public class JourneyGUI extends AbstractRunnableThread implements ActionListener {
	private JourneyController journeyController;
	private JButton startButton;
	private JButton stopButton;
	private JTextField urlTextField;
	private JComboBox<String> threadNumComboBox;
	private JTextArea emailTextArea;
	private JLabel linksCheckedLabel;
	private JLabel linksQueuedLabel;
	private JLabel emailsFoundLabel;
	private JLabel memoryLabel;
	private JScrollPane emailScroll;

	/**
	 * Constructor
	 * 
	 * @param journeyController
	 *            The controller object.
	 */
	public JourneyGUI(JourneyController journeyController) {
		this.journeyController = journeyController;
		this.repeating = false;
	}

	/**
	 * Initializes the JourneyGUI elements and waits for interaction.
	 */
	public void performRun() {
		JFrame frame = new JFrame("Journey v" + getClass().getPackage().getImplementationVersion());
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

		this.urlTextField = new JTextField();
		this.urlTextField.setSize(400, 20);
		this.urlTextField.setLocation(55, 10);
		this.urlTextField.setVisible(true);
		this.urlTextField.addActionListener(this);
		frame.add(this.urlTextField);

		JLabel label2 = new JLabel("Threads:");
		label2.setLocation(480, 10);
		label2.setSize(50, 20);
		label2.setVisible(true);
		frame.add(label2);

		String[] threadNums = { "1", "2", "5", "10", "20", "40", "80", "160" };
		this.threadNumComboBox = new JComboBox<String>(threadNums);
		this.threadNumComboBox.setLocation(535, 10);
		this.threadNumComboBox.setSize(50, 20);
		this.threadNumComboBox.setVisible(true);
		frame.add(this.threadNumComboBox);

		this.startButton = new JButton("Start");
		this.startButton.setLocation(610, 10);
		this.startButton.setSize(80, 20);
		this.startButton.setVisible(true);
		this.startButton.addActionListener(this);
		frame.add(this.startButton);

		this.stopButton = new JButton("Stop");
		this.stopButton.setLocation(700, 10);
		this.stopButton.setSize(80, 20);
		this.stopButton.setVisible(true);
		this.stopButton.addActionListener(this);
		this.stopButton.setEnabled(false);
		frame.add(this.stopButton);

		this.linksCheckedLabel = new JLabel("Links checked: 0");
		this.linksCheckedLabel.setLocation(10, 50);
		this.linksCheckedLabel.setSize(150, 20);
		this.linksCheckedLabel.setVisible(true);
		frame.add(this.linksCheckedLabel);

		this.linksQueuedLabel = new JLabel("Links in queue: 0");
		this.linksQueuedLabel.setLocation(170, 50);
		this.linksQueuedLabel.setSize(150, 20);
		this.linksQueuedLabel.setVisible(true);
		frame.add(this.linksQueuedLabel);

		this.emailsFoundLabel = new JLabel("Emails found: ");
		this.emailsFoundLabel.setLocation(340, 50);
		this.emailsFoundLabel.setSize(150, 20);
		this.emailsFoundLabel.setVisible(true);
		frame.add(this.emailsFoundLabel);

		this.memoryLabel = new JLabel("Memory");
		this.memoryLabel.setLocation(480, 50);
		this.memoryLabel.setSize(320, 20);
		this.memoryLabel.setVisible(true);
		frame.add(this.memoryLabel);

		JSeparator separator = new JSeparator();
		separator.setLocation(10, 80);
		separator.setSize(775, 20);
		separator.setVisible(true);
		frame.add(separator);

		this.emailTextArea = new JTextArea();
		this.emailScroll = new JScrollPane(this.emailTextArea);
		this.emailScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.emailScroll.setLocation(10, 100);
		this.emailScroll.setSize(775, 430);
		this.emailScroll.setVisible(true);
		this.emailTextArea.setEditable(false);

		DefaultCaret caret = (DefaultCaret) this.emailTextArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		frame.add(this.emailScroll);

		frame.setVisible(true);
		this.journeyController.onGUIReady();
	}

	/**
	 * Handles interactions.
	 */
	public void actionPerformed(ActionEvent e) {
		/* on Enter press or Start button press */
		if (e.getSource() == this.startButton || e.getSource() == this.urlTextField) {
			if (this.urlTextField.getText().equals("")) {
				JOptionPane.showMessageDialog(null, "Please add a starting URL!", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			/* enabling/disabling buttons */
			this.startButton.setEnabled(false);
			this.stopButton.setEnabled(true);
			int i = Integer.parseInt(this.threadNumComboBox.getSelectedItem().toString());
			/* start crawling */
			this.journeyController.startCrawling(this.urlTextField.getText(), i);
		}
		if (e.getSource() == this.stopButton) {
			/* enabling/disabling buttons */
			this.stopButton.setEnabled(false);
			this.startButton.setEnabled(true);
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
		String text = this.emailTextArea.getText();
		this.emailTextArea.setText(text + "\n" + s);
	}

	public void setURLChecked(int i) {
		this.linksCheckedLabel.setText("Links checked: " + i);
	}

	public void setInQueue(int i) {
		this.linksQueuedLabel.setText("Links in queue: " + i);
	}

	public void setEMailFound(int i) {
		this.emailsFoundLabel.setText("Emails found: " + i);
	}

	public void clearOutput() {
		this.emailTextArea.setText("");
	}

	/**
	 * Show information about the memory state.
	 * 
	 * @param used
	 *            Uses memory.
	 * @param available
	 *            Available memory.
	 * @param max
	 *            The memory reserved by the JVM.
	 */
	public void setMemData(long used, long available, long max) {
		this.memoryLabel.setText("Memory |    used:  " + used + " MB / " + available + " MB |    max: " + max + " MB");
	}

	/**
	 * Exits the program.
	 */
	public void close() {
		this.journeyController.exit();
	}

}
