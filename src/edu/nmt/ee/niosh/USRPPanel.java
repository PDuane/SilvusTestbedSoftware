package edu.nmt.ee.niosh;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public class USRPPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7642918563564458152L;
	
	private ButtonGroup rxTxSelector = new ButtonGroup();
	private JRadioButton rx = new JRadioButton("RX");
	private JRadioButton tx = new JRadioButton("TX");
	
	private JLabel freqLabel = new JLabel("Frequency: ");
	private JTextField freq = new JTextField(10);
	private JLabel freqUnits = new JLabel("MHz");
	
	private JButton inc = new JButton("+");
	private JButton dec = new JButton("-");
	
	private JLabel stepLabel = new JLabel("Step: ");
	private JTextField step = new JTextField(10);
	private JLabel stepUnits = new JLabel("MHz");
	
	private JLabel gainLabel = new JLabel("Gain: ");
	private JTextField gain = new JTextField(10);
	private JLabel gainUnits = new JLabel("dB");
	
	private String[] modulations = new String[] {"psk", "gmsk", "gfsk", "cpm"};
	private JLabel modulationLabel = new JLabel("Modulation: ");
	private JComboBox<String> mods = new JComboBox<>(modulations);
	
	private JLabel rateLabel = new JLabel("Bitrate: ");
	private JTextField rate = new JTextField(10);
	private JLabel rateUnits = new JLabel("kbps");
	
	private JLabel otptLabel = new JLabel("Output File: ");
	private JTextField otptFilename = new JTextField(50);
	private JButton browse;
	
	private JLabel runtimeLabel = new JLabel("Test Duration: ");
	private JTextField runtime = new JTextField(10);
	private JLabel runtimeUnits = new JLabel("seconds");
	
	private USRPPanel instance;
	
	private static String TX_SCRIPT_LOC;
	private static String RX_SCRIPT_LOC;
	
	static {
//		try {
//			JAR_FOLDER = new File(USRPPanel.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
//		} catch (URISyntaxException e) {
//			
//			JAR_FOLDER = "./";
//			
//		}
//		
//		if (!JAR_FOLDER.endsWith("/")) JAR_FOLDER += "/";
//		
//		TX_SCRIPT_LOC = JAR_FOLDER + "digital_bert_tx.py";
//		RX_SCRIPT_LOC = JAR_FOLDER + "digital_bert_rx.py";
		
		TX_SCRIPT_LOC = "/usr/share/gnuradio/examples/digital/narrowband/digital_bert_tx.py";
		RX_SCRIPT_LOC = "/usr/share/gnuradio/examples/digital/narrowband/digital_bert_rx.py";
		
//		URL url = USRPPanel.class.getResource("/edu/nmt/ee/niosh/resources/digital_bert_tx.py");
//		if (url == null) {
//			TX_SCRIPT_LOC = "scripts/digital_bert_tx.py";
//		} else {
//			TX_SCRIPT_LOC = url.toString();
//			if (TX_SCRIPT_LOC.startsWith("file:/")) TX_SCRIPT_LOC = TX_SCRIPT_LOC.substring(6);
//			else if (TX_SCRIPT_LOC.startsWith("file:")) TX_SCRIPT_LOC = TX_SCRIPT_LOC.substring(5);
//			TX_SCRIPT_LOC = TX_SCRIPT_LOC.replace("%20", " ");
//		}
//		
//		url = USRPPanel.class.getResource("/edu/nmt/ee/niosh/resources/digital_bert_rx.py");
//		if (RX_SCRIPT_LOC == null) {
//			RX_SCRIPT_LOC = "scripts/digital_bert_rx.py";
//		} else {
//			RX_SCRIPT_LOC = url.toString();
//			if (RX_SCRIPT_LOC.startsWith("file:/")) RX_SCRIPT_LOC = RX_SCRIPT_LOC.substring(6);
//			else if (RX_SCRIPT_LOC.startsWith("file:")) RX_SCRIPT_LOC = RX_SCRIPT_LOC.substring(5);
//			RX_SCRIPT_LOC = RX_SCRIPT_LOC.replace("%20", " ");
//		}
	}
	
	public USRPPanel() {
		instance = this;
		rxTxSelector.add(tx);
		rxTxSelector.add(rx);
		tx.setSelected(true);
		
		JPanel rxTxPanel = new JPanel();
		rxTxPanel.setLayout(new BoxLayout(rxTxPanel, BoxLayout.Y_AXIS));
		rxTxPanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
		JLabel mode = new JLabel("USRP Role:");
		rxTxPanel.add(mode);
		rxTxPanel.add(tx);
		rxTxPanel.add(rx);
		
		JPanel freqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		freqPanel.add(freqLabel);
		((PlainDocument)freq.getDocument()).setDocumentFilter(new NumbersFilter());
		freq.setText("2400");
		freqPanel.add(freq);
		freqPanel.add(freqUnits);
		freqPanel.add(Box.createHorizontalStrut(10));
		
		inc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int val = Integer.parseInt(freq.getText());
				int stepVal = Integer.parseInt(step.getText());
				
				val += stepVal;
				if (val > 10000) val = 10000;
				freq.setText(Integer.toString(val));
			}
		});
		
		dec.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int val = Integer.parseInt(freq.getText());
				int stepVal = Integer.parseInt(step.getText());
				
				val -= stepVal;
				if (val < 0) val = 0;
				freq.setText(Integer.toString(val));
			}
		});
		
		freqPanel.add(inc);
		freqPanel.add(dec);
		freqPanel.setSize(freqPanel.getPreferredSize().width, 20);
		
		JPanel stepPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		stepPanel.add(stepLabel);
		((PlainDocument)step.getDocument()).setDocumentFilter(new NumbersFilter());
		step.setText("100");
		stepPanel.add(step);
		stepPanel.add(stepUnits);
		
		JPanel gainPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gainPanel.add(gainLabel);
		gain.setText("1");
		gainPanel.add(gain);
		gainPanel.add(gainUnits);
		
		JPanel modPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modPanel.add(modulationLabel);
		modPanel.add(mods);
		mods.setSelectedItem("psk");
		
		JPanel ratePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ratePanel.add(rateLabel);
		rate.setText("250");
		ratePanel.add(rate);
		ratePanel.add(rateUnits);
		
		JPanel freqSelectPanel = new JPanel();
		freqSelectPanel.setLayout(new GridLayout(3, 2));
		freqSelectPanel.add(freqPanel);
		freqSelectPanel.add(stepPanel);
		freqSelectPanel.add(gainPanel);
		freqSelectPanel.add(modPanel);
		freqSelectPanel.add(ratePanel);
		freqSelectPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JPanel intermed = new JPanel(new FlowLayout(FlowLayout.LEFT));
		intermed.add(rxTxPanel);
		add(Box.createVerticalStrut(10));
		add(intermed);
		add(Box.createVerticalStrut(30));
		add(freqSelectPanel);
		
		if (DCGui.FOLDER_ICON != null) {
			browse = new JButton(DCGui.FOLDER_ICON);
		} else {
			browse = new JButton("B");
		}
		
		JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filePanel.add(otptLabel);
		filePanel.add(otptFilename);
		filePanel.add(browse);
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfs = new JFileChooser(System.getProperty("user.dir"));
				jfs.setApproveButtonText("Select");
				int retval = jfs.showOpenDialog(instance);
				if (retval == JFileChooser.APPROVE_OPTION) {
					otptFilename.setText(jfs.getSelectedFile().getPath());
				}
			}
		});
		add(Box.createVerticalStrut(30));
		
		JPanel runtimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		runtimePanel.add(runtimeLabel);
		runtimePanel.add(runtime);
		runtime.setText("60");
		runtimePanel.add(runtimeUnits);
		
		JPanel fileAndRuntimePanel = new JPanel(new GridLayout(1, 1));
//		fileAndRuntimePanel.add(filePanel);
		fileAndRuntimePanel.add(runtimePanel);
		add(fileAndRuntimePanel);
		
		JPanel buttonPanel = new JPanel();
		JButton run = new JButton("Run");
		run.setFont(run.getFont().deriveFont(16.0f));
		
		buttonPanel.add(run);
		
		add(new Box.Filler(new Dimension(0, 0), new Dimension(0, Short.MAX_VALUE), new Dimension(0, Short.MAX_VALUE)));
		add(buttonPanel);
		add(Box.createVerticalStrut(20));
		
		run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton done = new JButton("Close");
				done.setEnabled(false);
				done.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SwingUtilities.getWindowAncestor(done).setVisible(false);
					}
				});
				new Thread() {
					public void run() {
						JOptionPane.showOptionDialog(instance, "Done", "Running", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new JButton[] {done}, null);
					};
				}.start();
				
				ProcessBuilder pb;
				LinkedList<String> argList = new LinkedList<>();
				argList.add("sudo");
				argList.add("python");
				if (tx.isSelected()) {
					argList.add(TX_SCRIPT_LOC);
					argList.add("--freq=" + (Long.parseLong(freq.getText()) * 1000000));
					argList.add("--modulation=" + (String)mods.getSelectedItem());
					argList.add("--bitrate=" + (Integer.parseInt(rate.getText()) * 1000));
					argList.add("--amplitude=1");
					argList.add("--tx-gain=" + gain.getText());
				} else {
					argList.add(RX_SCRIPT_LOC);
					argList.add("--freq=" + (Long.parseLong(freq.getText()) * 1000000));
					argList.add("--modulation=" + (String)mods.getSelectedItem());
					argList.add("--bitrate=" + (Integer.parseInt(rate.getText()) * 1000));
					argList.add("--rx-gain=" + gain.getText());
				}
				if (argList != null && argList.size() > 0) {
					System.out.print("Command: ");
					for (int i = 0; i < argList.size(); i++) {
						if (i != 0) {
							System.out.print(" ");
						}
						System.out.print(argList.get(i));
					}
					
					pb = new ProcessBuilder(argList);
					try {
						long endTime = Long.parseLong(runtime.getText()) * 1000;
						endTime += System.currentTimeMillis();
						
						pb.inheritIO();
						Process proc = pb.start();
						while (proc.isAlive() && System.currentTimeMillis() < endTime)
							;
						done.setEnabled(true);
					} catch (IOException e1) {
						System.out.println("Error Occurred:");
						e1.printStackTrace();
						JOptionPane.showMessageDialog(instance, "Error running benchmark program: " + e1.getLocalizedMessage(), "Error running BER program", JOptionPane.WARNING_MESSAGE);
						return;
					}
				}
			}
		});
		
	}
	
	void killAllChildren(ProcessHandle proc) {
		Iterator<ProcessHandle> it = proc.children().distinct().iterator();
		ProcessHandle child;
		while (it.hasNext()) {
			child = it.next();
			killAllChildren(child);
			child.destroyForcibly();
		}
	}
	
	class NumbersFilter extends DocumentFilter {
		@Override
		public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
				throws BadLocationException {
			Document doc = fb.getDocument();
			
			StringBuilder sb = new StringBuilder();
			sb.append(doc.getText(0, doc.getLength()));
			
			try {
				Integer.parseInt(string);
				sb.insert(offset, string);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(instance, "Cannot enter non-digit values", "Non-digit entered", JOptionPane.WARNING_MESSAGE);
			}
			
			super.insertString(fb, offset, string, attr);
		}
	}
	

}
