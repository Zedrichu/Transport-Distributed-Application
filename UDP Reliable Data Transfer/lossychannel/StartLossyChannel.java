package lossychannel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class StartLossyChannel extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1;
	protected JTextField senderPortTF;
	protected JTextField receiverHostTF;
	protected JTextField receiverPortTF;
	protected JTextField packetDelayTF;
	protected JTextField lossRatioTF;
	
	protected LossyProxy proxy;
	
	JTextArea eventLog;
	
	JFrame container;	
	// Client (port 9874) --file--> Proxy (port 9875) --file--> Server (port 9876)
	public StartLossyChannel(String s) throws SocketException, UnknownHostException {
		super(s);

		setLayout(new BorderLayout());
		
		senderPortTF = new JTextField("9875", 20);
		JLabel senderPortLabel = new JLabel("Sender port: ");
		senderPortLabel.setLabelFor(senderPortTF);
		senderPortTF.addActionListener(this);
		
		receiverHostTF = new JTextField("localhost", 20);
		JLabel receiverHostLabel = new JLabel("Receiver host: ");
		receiverHostLabel.setLabelFor(receiverHostTF);	
		receiverHostTF.addActionListener(this);
		
		receiverPortTF = new JTextField("9876", 20);
		JLabel receiverPortLabel = new JLabel("Receiver port: ");
		receiverPortLabel.setLabelFor(receiverPortTF);
		receiverPortTF.addActionListener(this);
		
		packetDelayTF = new JTextField("200", 20);
		JLabel packetDelayLabel = new JLabel("Packet delay [ms]: ");
		packetDelayLabel.setLabelFor(packetDelayTF);
		packetDelayTF.addActionListener(this);
		
		lossRatioTF = new JTextField("0.0", 20);
		JLabel lossRatioLabel = new JLabel("Loss ratio: ");
		lossRatioLabel.setLabelFor(lossRatioTF);
		lossRatioTF.addActionListener(this);
        
		JPanel controlsPane = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		controlsPane.setLayout(gridbag);

		JLabel[] labels = {senderPortLabel, receiverHostLabel, receiverPortLabel, packetDelayLabel, lossRatioLabel};
		JTextField[] textFields = {senderPortTF, receiverHostTF, receiverPortTF, packetDelayTF, lossRatioTF};
		addLabelTextRows(labels, textFields, gridbag, controlsPane);

		c.gridwidth = GridBagConstraints.REMAINDER; 
		c.anchor = GridBagConstraints.CENTER;
		c.weightx = 1.0;
		controlsPane.setBorder(
				BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Lossy Channel Parameters"),
						BorderFactory.createEmptyBorder(5,5,5,5)));

		//Put everything together.
		JPanel leftPane = new JPanel(new BorderLayout());
		leftPane.add(controlsPane, BorderLayout.PAGE_START);
		getContentPane().add(controlsPane, BorderLayout.LINE_START);       
		
		//Create the text area for the status log and configure it.
        eventLog = new JTextArea(20, 45);
        eventLog.setEditable(false);
        JScrollPane scrollPaneForLog = new JScrollPane(eventLog);

        getContentPane().add(scrollPaneForLog, BorderLayout.PAGE_END);
        
		proxy = new LossyProxy(this, 9875, "localhost", 9876);
	}
	
	private void addLabelTextRows(JLabel[] labels,
			JTextField[] textFields,
			GridBagLayout gridbag,
			Container container) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.EAST;
		int numLabels = labels.length;

		for (int i = 0; i < numLabels; i++) {
			c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
			c.fill = GridBagConstraints.NONE;      //reset to default
			c.weightx = 0.0;                       //reset to default
			container.add(labels[i], c);

			c.gridwidth = GridBagConstraints.REMAINDER;     //end row
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			container.add(textFields[i], c);
		}
	}	

	public static void main(String[] args) {
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					//Create and set up the window.
					JFrame frame = new StartLossyChannel("TP6 Lossy Channel");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

					//Display the window.
					frame.pack();
					frame.setLocationByPlatform(true);
					frame.setVisible(true);
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public void log(String s)
	{
		eventLog.append(s+"\n");
		eventLog.setCaretPosition(eventLog.getText().length()-1);
	}


	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == senderPortTF) {
			int port = Integer.parseInt(senderPortTF.getText());
			try {
				proxy.changeSender(port);
			} catch (Exception e) {
				log("Error in changing sender port. CHANNEL DOWN.");
			}

		}
		else if (ae.getSource() == receiverHostTF) {
			try {
				proxy.changeReceiverHost(receiverHostTF.getText());
				log("Changed recevier IP.");
			} catch (UnknownHostException e) {
				log("Error in changing recevier IP. Keeping old IP.");
			}
		}
		else if (ae.getSource() == receiverPortTF) {
			int port = Integer.parseInt(receiverPortTF.getText());
			proxy.changeReceiverPort(port);
			log("Changed recevier port to " + port);
		}
		else if (ae.getSource() == packetDelayTF) {
			int delay = Integer.parseInt(packetDelayTF.getText());
			proxy.setPacketDelay(delay);
			log("Changed channel delay.");

		}
		else if (ae.getSource() == lossRatioTF) {
			float ratio = Float.parseFloat(lossRatioTF.getText());
			proxy.setLossRatio(ratio);
			log("Changed channel loss ratio.");
		}
	}

}
