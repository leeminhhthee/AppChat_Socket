package Server;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class ServerFrame {

	private JFrame frame;
	private JLabel lblStatus;
	private JButton btnStartServer;
	public static ServerSocket serverSocket;
	private static JTextArea txtMessage;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerFrame window = new ServerFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public static void updateMessage(String msg) {
		txtMessage.append(msg + "\n");
	}

	/**
	 * Create the application.
	 */
	public ServerFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("LMT Chatting App: SERVER");
		frame.setResizable(false);
		frame.setBounds(100, 100, 450, 291);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("SERVER CHATTING APP");
		lblNewLabel.setFont(new Font("Wide Latin", Font.PLAIN, 16));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 11, 414, 38);
		frame.getContentPane().add(lblNewLabel);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Server InFormation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(255, 255, 255)));
		panel.setBackground(new Color(112, 128, 144));
		panel.setBounds(240, 48, 200, 107);
		panel.setLayout(null);
		panel.setBounds(240, 48, 184, 107);
		frame.getContentPane().add(panel);

		JLabel lbNew = new JLabel("Status");
		lbNew.setForeground(new Color(255, 255, 255));
		lbNew.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lbNew.setBounds(10, 42, 67, 21);
		panel.add(lbNew);

		lblStatus = new JLabel("OFF");
		lblStatus.setForeground(Color.RED);
		lblStatus.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblStatus.setBounds(74, 31, 100, 43);
		panel.add(lblStatus);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(
				new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)),
				"Click here to start server", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(10, 60, 176, 76);
		frame.getContentPane().add(panel_1);

		btnStartServer = new JButton("Start Server");
		btnStartServer.setFocusable(false);
		btnStartServer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Thread t = new Thread() {
					public void run() {
						try {
							new Server();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				};
				t.start();
				ServerFrame.updateMessage("SERVER STARTING ON PORT 2003");
				lblStatus.setText("<html><font color='green'>RUNNING...</font></html>");
				btnStartServer.setEnabled(false);
			}
		});
		btnStartServer.setFont(new Font("Tahoma", Font.PLAIN, 18));
		panel_1.add(btnStartServer);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "Annotation", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		txtMessage = new JTextArea();
		txtMessage.setBackground(Color.BLACK);
		txtMessage.setForeground(Color.WHITE);
		txtMessage.setFont(new Font("Courier New", Font.PLAIN, 18));
		panel_2.setBounds(10, 158, 414, 90);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));
		JScrollPane scrollPane = new JScrollPane(txtMessage);
		panel_2.add(scrollPane);
		frame.getContentPane().add(panel_2);
	}

}
