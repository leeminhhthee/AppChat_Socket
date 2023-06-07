package Client;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import javax.swing.SwingConstants;
import javax.swing.ImageIcon;

public class LoginFrame {

	private JFrame frame;
	private JTextField txtUsername;
	private JPasswordField txtPassword;

	private String host = "localhost";
	private int port = 2003;
	private Socket socket;

	private DataInputStream dis;
	private DataOutputStream dos;
	private String username;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginFrame window = new LoginFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("LMT Chatting App: LOGIN");
		frame.getContentPane().setBackground(Color.LIGHT_GRAY);
		frame.setResizable(false);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lbuser = new JLabel("");
		lbuser.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\User-icon.png"));
		lbuser.setHorizontalAlignment(SwingConstants.CENTER);
		lbuser.setFont(new Font("SimSun", Font.BOLD, 16));
		lbuser.setBounds(65, 46, 92, 62);
		frame.getContentPane().add(lbuser);

		txtUsername = new JTextField();
		txtUsername.setColumns(10);
		txtUsername.setBounds(167, 70, 186, 29);
		frame.getContentPane().add(txtUsername);

		JLabel lbPass = new JLabel("");
		lbPass.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\password-icon.png"));
		lbPass.setHorizontalAlignment(SwingConstants.CENTER);
		lbPass.setFont(new Font("SimSun", Font.BOLD, 16));
		lbPass.setBounds(61, 119, 96, 51);
		frame.getContentPane().add(lbPass);

		txtPassword = new JPasswordField();
		txtPassword.setBounds(167, 130, 186, 29);
		frame.getContentPane().add(txtPassword);

		JButton btnSignup = new JButton("Sign Up");
		btnSignup.setEnabled(false);

		JButton btnLogin = new JButton("Login");
		btnLogin.setEnabled(false);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String response = Login(txtUsername.getText(), String.copyValueOf(txtPassword.getPassword()));

				// đăng nhập thành công thì server sẽ trả về chuỗi "Log in successful"
				if (response.equals("Log in successful")) {
					username = txtUsername.getText();
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								ChatClient chat = new ChatClient(username, dis, dos);
								chat.frame.setVisible(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					frame.dispose();
				} else {
					btnLogin.setEnabled(false);
					btnSignup.setEnabled(false);
					txtPassword.setText("");
					JOptionPane.showMessageDialog(null, response, "Lỗi", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnLogin.setForeground(Color.WHITE);
		btnLogin.setFont(new Font("Stencil", Font.PLAIN, 14));
		btnLogin.setBackground(Color.BLUE);
		btnLogin.setBounds(178, 196, 110, 34);
		frame.getContentPane().add(btnLogin);
		frame.getRootPane().setDefaultButton(btnLogin);

		btnSignup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPasswordField confirm = new JPasswordField();

				// Hiển thị hộp thoại xác nhận password
				int action = JOptionPane.showConfirmDialog(null, confirm, "Comfirm your password",
						JOptionPane.OK_CANCEL_OPTION);
				if (action == JOptionPane.OK_OPTION) {
					if (String.copyValueOf(confirm.getPassword())
							.equals(String.copyValueOf(txtPassword.getPassword()))) {
						String response = Signup(txtUsername.getText(), String.copyValueOf(txtPassword.getPassword()));

						// đăng ký thành công thì server sẽ trả về chuỗi "Log in successful"
						if (response.equals("Sign up successful")) {
							username = txtUsername.getText();
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									try {
										// In ra thông báo đăng kí thành công
										int confirm = JOptionPane.showConfirmDialog(null,
												"Sign up successful\nWelcome to LMT CHATTING", "Sign up successful",
												JOptionPane.DEFAULT_OPTION);

										ChatClient chat = new ChatClient(username, dis, dos);
										chat.frame.setVisible(true);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
							frame.dispose();
						} else {
							btnLogin.setEnabled(false);
							btnSignup.setEnabled(false);
							txtPassword.setText("");
							JOptionPane.showMessageDialog(null, response, "Lỗi", JOptionPane.ERROR_MESSAGE);
						}
					} else {
						JOptionPane.showMessageDialog(null, "Confirm password does not match", "Lỗi",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		btnSignup.setFont(new Font("Stencil", Font.PLAIN, 13));
		btnSignup.setBackground(new Color(0, 250, 154));
		btnSignup.setBounds(10, 196, 140, 34);
		frame.getContentPane().add(btnSignup);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null, "BẠN CÓ CHẮC CHẮN THOÁT KHÔNG?", "Thoát hệ thống?",
						JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_NO_OPTION)
					System.exit(0);
			}
		});
		btnCancel.setFont(new Font("Stencil", Font.PLAIN, 14));
		btnCancel.setBackground(Color.RED);
		btnCancel.setBounds(309, 196, 105, 34);
		frame.getContentPane().add(btnCancel);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(25, 25, 112));
		panel.setBounds(0, 0, 434, 43);
		frame.getContentPane().add(panel);

		JLabel lb1 = new JLabel("WELCOME TO LMT CHATTING APP ");
		panel.add(lb1);
		lb1.setHorizontalAlignment(SwingConstants.CENTER);
		lb1.setForeground(Color.WHITE);
		lb1.setFont(new Font("Algerian", Font.ITALIC, 23));
		lb1.setBackground(Color.ORANGE);

		txtUsername.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
					btnLogin.setEnabled(false);
					btnSignup.setEnabled(false);
				} else {
					btnLogin.setEnabled(true);
					btnSignup.setEnabled(true);
				}
			}
		});

		txtPassword.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtUsername.getText().isBlank() || String.copyValueOf(txtPassword.getPassword()).isBlank()) {
					btnLogin.setEnabled(false);
					btnSignup.setEnabled(false);
				} else {
					btnLogin.setEnabled(true);
					btnSignup.setEnabled(true);
				}
			}
		});

	}

	/**
	 * Gửi yêu cầu đăng nhập đến server Trả về kết quả phản hồi từ server
	 */
	public String Login(String username, String password) {
		try {
			Connect();

			dos.writeUTF("Log in");
			dos.writeUTF(username);
			dos.writeUTF(password);
			dos.flush();

			String response = dis.readUTF();
			return response;

		} catch (IOException e) {
			e.printStackTrace();
			return "Network error: Log in fail";
		}
	}

	/**
	 * Gửi yêu cầu đăng ký đến server Trả về kết quả phản hồi từ server
	 */
	public String Signup(String username, String password) {
		try {
			Connect();

			dos.writeUTF("Sign up");
			dos.writeUTF(username);
			dos.writeUTF(password);
			dos.flush();

			String response = dis.readUTF();
			return response;

		} catch (IOException e) {
			e.printStackTrace();
			return "Network error: Sign up fail";
		}
	}

	/**
	 * Kết nối đến server
	 */
	public void Connect() {
		try {
			if (socket != null) {
				socket.close();
			}
			socket = new Socket(host, port);
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public String getUsername() {
		return this.username;
	}

}
