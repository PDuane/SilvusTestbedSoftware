package edu.nmt.ee.niosh;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EtchedBorder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DCGui extends JFrame {
	
	private static final long serialVersionUID = -6820646516793494599L;
	public static ImageIcon FOLDER_ICON;
	private DCGui instance;
	private JTextField localRadioIp, remoteRadioIp;
	private JTextField distance;
	private JCheckBox iperfTestCB;
	private JTextField iperfTestIp;
	private JTextField outputFilename;
	private JButton browse;
	private JCheckBox append;
	private JTextField iterations;
	private JTextArea description;
	private JButton run;
//	private JOptionPane runOptPane;
	private JLabel runOptPaneMsg;
//	private JDialog runDialog;
	private JButton runDlgDone;
	
	static {
		try {
			URL img_loc = DCGui.class.getResource("/edu/nmt/ee/niosh/resources/folder_icon.png");
			FOLDER_ICON = new ImageIcon(ImageIO.read(img_loc).getScaledInstance(16, 16, BufferedImage.SCALE_SMOOTH));
		} catch (IOException e) {
			FOLDER_ICON = null;
		}
	}
	
	private class IPEntryRestrictor implements KeyListener{

		@Override
		public void keyTyped(KeyEvent e) {
			char c = e.getKeyChar();
			// If the entered character is not a decimal point or a digit, don't enter it
			if (!(c == '.' || (c - '0' >= 0 && c - '0' < 10))) {
				e.consume();
			}
		}

		@Override public void keyPressed(KeyEvent e) {}
		@Override public void keyReleased(KeyEvent e) {}
		
	}
	
	public DCGui() {
        try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {}
		instance = this;
		setTitle("Testbed Data Collection Tool");
		setSize(800, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		JTabbedPane tabs = new JTabbedPane();
		
		JPanel p, q; // Variables for temporarily storing panels
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		
		JSONObject sessionInfo;
		
		File sessionInfoFile = new File("session.json");
		if (sessionInfoFile.exists()) {
			try {
				sessionInfo = (JSONObject)new JSONParser().parse(new FileReader(sessionInfoFile));
			} catch (IOException | ParseException e1) {
				e1.printStackTrace();
				sessionInfo = new JSONObject();
			}
		} else {
			sessionInfo = new JSONObject();
		}
		
		// ----------------------------------------------------
		//            CONNECTION INFORMATION PANEL
		
		localRadioIp = new JTextField(20);
		remoteRadioIp = new JTextField(20);
		distance = new JTextField(5);
		
		localRadioIp.addKeyListener(new IPEntryRestrictor());
		remoteRadioIp.addKeyListener(new IPEntryRestrictor());
		distance.addKeyListener(new IPEntryRestrictor());
		
		JLabel localIpLabel = new JLabel("Connected radio IP:");
		JLabel remoteIpLabel = new JLabel("Destination radio IP:");
		JLabel distanceLabel = new JLabel("Distance:");
		JLabel distUnitLabel = new JLabel("meters");
		
		JPanel connectionInfo = new JPanel();
		connectionInfo.setLayout(new GridLayout(2, 2));
		connectionInfo.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.add(localIpLabel);
		p.add(localRadioIp);
		q = new JPanel();
		q.add(p);
		connectionInfo.add(q);
		
		p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.add(remoteIpLabel);
		p.add(remoteRadioIp);
		q = new JPanel();
		q.add(p);
		connectionInfo.add(q);
		
		p = new JPanel(new FlowLayout(FlowLayout.CENTER));
		p.add(distanceLabel);
		p.add(distance);
		p.add(distUnitLabel);
		q = new JPanel(new FlowLayout(FlowLayout.LEFT));
		q.add(p);
		connectionInfo.add(q);
		
		contentPanel.add(connectionInfo);
		tabs.add(contentPanel, "StreamCaster");
		tabs.add(new USRPPanel(), "USRP");
		add(tabs);
		
		//               END CONNECTION INFORMATION
		// ----------------------------------------------------
		
		
		// ----------------------------------------------------
		//                     IPERF PANEL
		
		JPanel iperfPanel = new JPanel();
		JLabel iperfIpLabel = new JLabel("IPerf Server IP:");
		iperfIpLabel.setEnabled(false);
		iperfTestIp = new JTextField(20);
		iperfTestIp.setToolTipText("If the destination IP points to a radio, specify\r\n"
				                 + "the IP address of the device running the IPerf server.");
		iperfTestIp.addKeyListener(new IPEntryRestrictor());
		iperfTestIp.setEnabled(false);
		
		iperfTestCB = new JCheckBox("Run IPerf Throughput Test");
		iperfTestCB.setSelected(false);
		iperfTestCB.setToolTipText("Run a throughput and packet loss test using IPerf.\r\n"
				                 + "There must be a device running an IPerf server\r\n"
				                 + "connected to the remote radio");
		iperfTestCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				iperfTestIp.setEnabled(iperfTestCB.isSelected());
				iperfIpLabel.setEnabled(iperfTestCB.isSelected());
			}
		});
		
		iperfPanel.setLayout(new GridLayout(2, 1));
		
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(iperfTestCB);
		iperfPanel.add(p);
		
		p = new JPanel();
		p.add(iperfIpLabel);
		p.add(iperfTestIp);
		q = new JPanel(new FlowLayout(FlowLayout.LEFT));
		q.add(p);
		iperfPanel.add(q);
		
		contentPanel.add(iperfPanel);
		
		//                      END IPERF
		// ----------------------------------------------------
		
		
		// ----------------------------------------------------
		//                FILE INFORMATION PANEL
		
		JPanel filePanel = new JPanel();
		filePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		JLabel filenameLabel = new JLabel("Output File:");
		outputFilename = new JTextField(50);
		if (FOLDER_ICON != null) {
			browse = new JButton(FOLDER_ICON);
		} else {
			browse = new JButton("B");
		}
		browse.setToolTipText("Browse");
		JFileChooser jfs = new JFileChooser(System.getProperty("user.dir"));
		jfs.setApproveButtonText("Select");
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int retval = jfs.showOpenDialog(instance);
				if (retval == JFileChooser.APPROVE_OPTION) {
					outputFilename.setText(jfs.getSelectedFile().getPath());
				}
			}
		});
		
		append = new JCheckBox("Append to file");
		
		JLabel descriptionLabel = new JLabel("Notes");
		descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(18.0f));
		description = new JTextArea(5, 60);
		description.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(filenameLabel);
		p.add(outputFilename);
		p.add(browse);
		
		filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
		filePanel.add(p);
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(append);
		filePanel.add(p);
		
		JLabel iterationLabel = new JLabel("Iterations: ");
		iterations = new JTextField(10);
		iterations.setText("1");
		p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(iterationLabel);
		p.add(iterations);
		filePanel.add(p);
		
		filePanel.add(Box.createHorizontalStrut(30));
		filePanel.add(descriptionLabel);
		p = new JPanel();
		p.add(description);
		filePanel.add(p);
		
		contentPanel.add(filePanel);
		
		//                 END FILE INFOMRATION
		// ----------------------------------------------------
		
		
		// ----------------------------------------------------
		//               VALUE PRELOADING SECTION
		
		if (sessionInfo.containsKey(IPerfTest.LOCAL_RADIO_IP)) {
			localRadioIp.setText((String)sessionInfo.get(IPerfTest.LOCAL_RADIO_IP));
		}
		if (sessionInfo.containsKey(IPerfTest.DESTINATION_IP)) {
			remoteRadioIp.setText((String)sessionInfo.get(IPerfTest.DESTINATION_IP));
		}
		if (sessionInfo.containsKey(IPerfTest.DISTANCE)) {
			distance.setText((String)sessionInfo.get(IPerfTest.DISTANCE));
		}
		if (sessionInfo.containsKey("doIperfTest")) {
			iperfTestCB.setSelected(((String)sessionInfo.get("doIperfTest")).equalsIgnoreCase("true"));
		}
		if (sessionInfo.containsKey("iperf_ip")) {
			iperfTestIp.setText((String)sessionInfo.get("iperf_ip"));
			if (iperfTestCB.isSelected()) {
				iperfTestIp.setEnabled(true);
				iperfIpLabel.setEnabled(true);
			}
		}
		if (sessionInfo.containsKey("output_filename")) {
			outputFilename.setText((String)sessionInfo.get("output_filename"));
		}
		
		if (sessionInfo.containsKey("iterations")) {
			iterations.setText((String)sessionInfo.get("iterations"));
		}
		
		if (sessionInfo.containsKey(IPerfTest.DESCRIPTION)) {
			description.setText((String)sessionInfo.get(IPerfTest.DESCRIPTION));
		}
				
		//                 END VALUE PRELOADING
		// ----------------------------------------------------
		
		// ----------------------------------------------------
		//                  RUN BUTTON SECTION
		
		run = new JButton("Run");
		run.setFont(run.getFont().deriveFont(16.0f));
		
		new JLabel("Test Text");
		runDlgDone = new JButton("Done");
		runDlgDone.setEnabled(false);
		
		runDlgDone.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				if (runDialog != null) {
//					runDlgDone.setEnabled(false);
//					setShouldAppend(true);
//					run.setEnabled(true);
//					runDialog.dispose();
//				}
				Window w = SwingUtilities.getWindowAncestor(runDlgDone);
				if (w != null) {
//					w.setVisible(false);
					w.dispose();
				}
			}
		});
		
		runOptPaneMsg = new JLabel("");
//		runOptPane = new JOptionPane(runOptPaneMsg, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null, new Object[] {runDlgDone});
		
		run.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if (localRadioIp == null || localRadioIp.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(instance, "Local IP Not Specified");
					return;
				}
				
				if (remoteRadioIp == null || remoteRadioIp.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(instance, "Remote IP Not Specified");
					return;
				}
				
				if (distance != null && distance.getText().trim().equals("")) {
					distance.setText("-1");
				}
				
				if (iperfTestCB == null || iperfTestIp == null) {
					JOptionPane.showMessageDialog(instance, "IPerf IP Not Specified");
					return;
				}
				
				if (iperfTestCB.isSelected() && iperfTestIp.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(instance, "IPerf IP Not Specified");
					return;
				}
				
				if (outputFilename == null || outputFilename.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(instance, "Output File Not Specified");
					return;
				}
				
				if (iterations == null || iterations.getText().trim().equals("")) {
					JOptionPane.showMessageDialog(instance, "Number of Iterations Not Specified");
					return;
				} else try {
					Integer.parseInt(iterations.getText().trim());
				} catch (NumberFormatException e1) {
					JOptionPane.showMessageDialog(instance, "Value in \"Iterations\" field non-numeric");
				}
				
				File f = new File(outputFilename.getText());
				boolean shouldRun = true;
				if (!append.isSelected() && f.exists()) {
					int retval = JOptionPane.showConfirmDialog(instance, f.getName() + " already exists. Overwrite?", "Warning: File Exists", JOptionPane.YES_NO_OPTION);
					shouldRun = (retval == JOptionPane.YES_OPTION);
				}
//				runDialog = runOptPane.createDialog(instance, "Collecting Data");
				
				saveSession();
				
				if (shouldRun) {
					run.setEnabled(false);
					runOptPaneMsg.setText("Starting");
//					runDialog = runOptPane.createDialog(instance, "Collecting Data");
//					runDialog.setModalityType(ModalityType.MODELESS);
//					runDialog.setVisible(true);
					new Thread() {
						public void run() {
							JOptionPane.showOptionDialog(instance, runOptPaneMsg, "Collecting Data", JOptionPane.OK_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[] {runDlgDone}, null);
						};
					}.start();
					for (int i = 0; i < Integer.parseInt(iterations.getText()); i++) {
						runOptPaneMsg.setText("Iteration " + (i + 1));
						onRun(i + 1);
						if (i == 0) {
							append.setSelected(true);
						}
					}
				}
			}
		});
		
		contentPanel.add(Box.createHorizontalStrut(50));
		p = new JPanel();
		p.add(run);
		contentPanel.add(p);
		
		//                   END RUN BUTTON
		// ----------------------------------------------------
		
		pack();
		setVisible(true);
	}
	
	public void onRun(int iteration) {}
	
	@SuppressWarnings("unchecked")
	public void saveSession() {
		JSONObject session = new JSONObject();
		session.put(IPerfTest.LOCAL_RADIO_IP, localRadioIp.getText().trim());
		session.put(IPerfTest.DESTINATION_IP, remoteRadioIp.getText().trim());
		session.put(IPerfTest.DISTANCE, distance.getText().trim());
		session.put("doIperfTest", iperfTestCB.isSelected() ? "true" : "false");
		session.put("iperf_ip", iperfTestIp.getText().trim());
		session.put("output_filename", outputFilename.getText().trim());
		session.put("iterations", iterations.getText().trim());
		session.put(IPerfTest.DESCRIPTION, description.getText());
		
		try {
			FileOutputStream fos = new FileOutputStream(new File("session.json"));
			fos.write(session.toJSONString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void setRunDialogText(String text) {
		System.out.println(text);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runOptPaneMsg.setText(text);
			}
		});
	}
	
	public synchronized void setDialogDone(boolean done) {
		runDlgDone.setEnabled(done);
	}
	
	public String getLocalIp() {
		return (localRadioIp == null) ? null : localRadioIp.getText();
	}
	
	public String getRemoteIp() {
		return (remoteRadioIp == null) ? null : remoteRadioIp.getText();
	}
	
	public double getDistance() {
		try {
			return (distance == null) ? Double.NaN : Double.parseDouble(distance.getText());
		} catch (NumberFormatException e) {
			return Double.NaN;
		}
	}
	
	public boolean shouldRunIperf() {
		return (iperfTestCB == null) ? false : iperfTestCB.isSelected();
	}
	
	public String getIperfIp() {
		return (iperfTestIp == null) ? null : iperfTestIp.getText();
	}
	
	public String getOutputFilename() {
		return (outputFilename == null) ? null : outputFilename.getText();
	}
	
	public boolean shouldAppend() {
		return (append == null) ? true : append.isSelected();
	}
	
	public void setShouldAppend(boolean shouldAppend) {
		append.setSelected(shouldAppend);
	}
	
	public String getNotes() {
		return (description == null) ? null : description.getText();
	}
	
}
