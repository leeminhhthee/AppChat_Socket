package Client;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ChatClient {

	JFrame frame;
	private JButton btnFile;
	private JButton btnSend;
	private JScrollPane chatPanel;
	private JLabel lbReceiver = new JLabel(" ");
	private JTextField txtMessage;
	private JTextPane chatWindow;
	JComboBox<String> onlineUsers = new JComboBox<String>();

	private static String username;
	private static DataInputStream dis;
	private static DataOutputStream dos;

	private HashMap<String, JTextPane> chatWindows = new HashMap<String, JTextPane>();

	Thread receiver;
	private JPanel header;
	private JPanel userPanel;
	private JPanel emojis;
	private JLabel angryIcon;
	private JLabel suspiciousIcon;
	private JLabel madIcon;
	private JLabel sadIcon;
	private JLabel loveIcon;
	private JLabel happyIcon;
	private JLabel bigSmileIcon;
	private JLabel smileIcon;
	private JLabel confusedIcon;
	private JLabel unhappyIcon;
	private JLabel vietnamIcon;
	private JLabel ukIcon;
	private JLabel cakeIcon;
	private JLabel likeIcon;
	private JLabel userImage;
	private JLabel lbUsername;
	private JLabel lblNewLabel;
	private JLabel headerContent;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChatClient window = new ChatClient(username, dis, dos);
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
	public ChatClient(String username, DataInputStream dis, DataOutputStream dos) {
		initialize(username, dis, dos);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize(String username, DataInputStream dis, DataOutputStream dos) {
		frame = new JFrame("LMT Chatting App: " + username);
		frame.getContentPane().setBackground(new Color(0, 0, 0));
		frame.setResizable(false);
		frame.setBounds(100, 100, 506, 521);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		this.username = username;
		this.dis = dis;
		this.dos = dos;
		receiver = new Thread(new Receiver(dis));
		receiver.start();

		btnFile = new JButton("");
		btnFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Hiển thị hộp thoại cho người dùng chọn file để gửi
				JFileChooser fileChooser = new JFileChooser();
				int rVal = fileChooser.showOpenDialog(frame.getParent());
				if (rVal == JFileChooser.APPROVE_OPTION) {
					byte[] selectedFile = new byte[(int) fileChooser.getSelectedFile().length()];
					BufferedInputStream bis;
					try {
						bis = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()));
						// Đọc file vào biến selectedFile
						bis.read(selectedFile, 0, selectedFile.length);

						dos.writeUTF("File");
						dos.writeUTF(lbReceiver.getText());
						dos.writeUTF(fileChooser.getSelectedFile().getName());
						dos.writeUTF(String.valueOf(selectedFile.length));

						int size = selectedFile.length;
						int bufferSize = 2048;
						int offset = 0;

						// Lần lượt gửi cho server từng buffer cho đến khi hết file
						while (size > 0) {
							dos.write(selectedFile, offset, Math.min(size, bufferSize));
							offset += Math.min(size, bufferSize);
							size -= bufferSize;
						}
						dos.flush();
						bis.close();

						// In ra màn hình file
						newFile(username, fileChooser.getSelectedFile().getName(), selectedFile, true);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		btnFile.setEnabled(false);
		btnFile.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\attach.png"));
		btnFile.setBounds(366, 433, 51, 41);
		frame.getContentPane().add(btnFile);

		btnSend = new JButton("");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					dos.writeUTF("Text");
					dos.writeUTF(lbReceiver.getText());
					dos.writeUTF(txtMessage.getText());
					dos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
					newMessage("ERROR", "Network error!", true);
				}

				// In ra tin nhắn lên màn hình chat
				newMessage(username, txtMessage.getText(), true);
				txtMessage.setText("");
				btnSend.setEnabled(false);
			}
		});
		btnSend.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\send.png"));
		btnSend.setBounds(414, 433, 57, 41);
		btnSend.setEnabled(false);
		frame.getContentPane().add(btnSend);
		frame.getRootPane().setDefaultButton(btnSend);

		chatPanel = new JScrollPane();
		chatPanel.setBounds(20, 117, 451, 259);
		chatPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(chatPanel);

		JPanel usernamePanel = new JPanel();
		usernamePanel.setBorder(null);
		usernamePanel.setBackground(new Color(230, 240, 247));
		chatPanel.setColumnHeaderView(usernamePanel);

		lbReceiver.setFont(new Font("Arial", Font.BOLD, 16));
		usernamePanel.add(lbReceiver);

		chatWindow = new JTextPane();
		chatWindow.setEditable(false);
		chatPanel.setViewportView(chatWindow);
		chatWindows.put(" ", new JTextPane());
		chatWindow = chatWindows.get(" ");
		chatWindow.setFont(new Font("Arial", Font.PLAIN, 14));
		chatWindow.setEditable(false);

		txtMessage = new JTextField();
		txtMessage.setEnabled(false);
		txtMessage.setFont(new Font("Tahoma", Font.PLAIN, 13));
		txtMessage.setBounds(20, 433, 347, 41);
		frame.getContentPane().add(txtMessage);
		txtMessage.setColumns(10);
		txtMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (txtMessage.getText().isBlank() || lbReceiver.getText().isBlank()) {
					btnSend.setEnabled(false);
				} else {
					btnSend.setEnabled(true);
				}
			}
		});

		userPanel = new JPanel();
		userPanel.setBorder(null);
		userPanel.setBounds(0, 43, 490, 74);
		userPanel.setBackground(new Color(230, 240, 247));
		frame.getContentPane().add(userPanel);
		userPanel.setLayout(null);

		userImage = new JLabel("");
		userImage.setHorizontalAlignment(SwingConstants.CENTER);
		userImage.setBounds(26, 5, 61, 53);
		userImage.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\User-icon.png"));
		userPanel.add(userImage);

		lbUsername = new JLabel(this.username);
		lbUsername.setFont(new Font("Arial", Font.BOLD, 13));
		lbUsername.setBounds(91, 22, 107, 31);
		userPanel.add(lbUsername);

		lblNewLabel = new JLabel(" CHAT WITH:");
		lblNewLabel.setFont(new Font("Sitka Text", Font.BOLD, 14));
		lblNewLabel.setBounds(267, 22, 107, 31);
		userPanel.add(lblNewLabel);

		onlineUsers.setBounds(365, 22, 107, 31);
		onlineUsers.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					lbReceiver.setText((String) onlineUsers.getSelectedItem());
					if (chatWindow != chatWindows.get(lbReceiver.getText())) {
						txtMessage.setText("");
						chatWindow = chatWindows.get(lbReceiver.getText());
						chatPanel.setViewportView(chatWindow);
						chatPanel.validate();
					}

					if (lbReceiver.getText().isBlank()) {
						btnSend.setEnabled(false);
						btnFile.setEnabled(false);
						txtMessage.setEnabled(false);
					} else {
						btnSend.setEnabled(true);
						btnFile.setEnabled(true);
						txtMessage.setEnabled(true);
					}
				}

			}
		});

		userPanel.add(onlineUsers);

		emojis = new JPanel();
		emojis.setBorder(new TitledBorder(null, "Icon Chat", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		emojis.setBounds(20, 380, 451, 53);
		frame.getContentPane().add(emojis);

		likeIcon = new JLabel("");
		likeIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\like-icon.png"));
		likeIcon.addMouseListener(new IconListener(likeIcon.getIcon().toString()));
		emojis.add(likeIcon);

		smileIcon = new JLabel("");
		smileIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\smile.png"));
		smileIcon.addMouseListener(new IconListener(smileIcon.getIcon().toString()));
		emojis.add(smileIcon);

		bigSmileIcon = new JLabel("");
		bigSmileIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\big-smile.png"));
		bigSmileIcon.addMouseListener(new IconListener(bigSmileIcon.getIcon().toString()));
		emojis.add(bigSmileIcon);

		happyIcon = new JLabel("");
		happyIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\happy.png"));
		happyIcon.addMouseListener(new IconListener(happyIcon.getIcon().toString()));
		emojis.add(happyIcon);

		loveIcon = new JLabel("");
		loveIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\love.png"));
		loveIcon.addMouseListener(new IconListener(loveIcon.getIcon().toString()));
		emojis.add(loveIcon);

		sadIcon = new JLabel("");
		sadIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\sadd.png"));
		sadIcon.addMouseListener(new IconListener(sadIcon.getIcon().toString()));
		emojis.add(sadIcon);

		madIcon = new JLabel("");
		madIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\mad.png"));
		madIcon.addMouseListener(new IconListener(madIcon.getIcon().toString()));
		emojis.add(madIcon);

		suspiciousIcon = new JLabel("");
		suspiciousIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\suspicious.png"));
		suspiciousIcon.addMouseListener(new IconListener(suspiciousIcon.getIcon().toString()));
		emojis.add(suspiciousIcon);

		angryIcon = new JLabel("");
		angryIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\angry.png"));
		angryIcon.addMouseListener(new IconListener(angryIcon.getIcon().toString()));
		emojis.add(angryIcon);

		confusedIcon = new JLabel("");
		confusedIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\confused.png"));
		confusedIcon.addMouseListener(new IconListener(confusedIcon.getIcon().toString()));
		emojis.add(confusedIcon);

		unhappyIcon = new JLabel("");
		unhappyIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\unhappy.png"));
		unhappyIcon.addMouseListener(new IconListener(unhappyIcon.getIcon().toString()));
		emojis.add(unhappyIcon);

		vietnamIcon = new JLabel("");
		vietnamIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\vietnam.png"));
		vietnamIcon.addMouseListener(new IconListener(vietnamIcon.getIcon().toString()));
		emojis.add(vietnamIcon);

		ukIcon = new JLabel("");
		ukIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\uk.png"));
		ukIcon.addMouseListener(new IconListener(ukIcon.getIcon().toString()));
		emojis.add(ukIcon);

		cakeIcon = new JLabel("");
		cakeIcon.setIcon(new ImageIcon("C:\\Users\\HP\\Desktop\\icon\\cake.png"));
		cakeIcon.addMouseListener(new IconListener(cakeIcon.getIcon().toString()));
		emojis.add(cakeIcon);

		header = new JPanel();
		header.setBounds(0, 0, 490, 74);
		frame.getContentPane().add(header);
		header.setBackground(new Color(160, 190, 223));

		headerContent = new JLabel("LMT CHATTING APP");
		headerContent.setFont(new Font("Poor Richard", Font.BOLD, 24));
		header.add(headerContent);

		frame.addWindowListener(new WindowListener() {
			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				try {
					dos.writeUTF("Log out");
					dos.flush();

					try {
						receiver.join();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}

					if (dos != null) {
						dos.close();
					}
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});

	}

	private void autoScroll() {
		chatPanel.getVerticalScrollBar().setValue(chatPanel.getVerticalScrollBar().getMaximum());
	}

	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Insert a emoji into chat pane.
	 */
	private void newEmoji(String username, String emoji, Boolean yourMessage) {

		StyledDocument doc;
		if (username.equals(this.username)) {
			doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
		} else {
			doc = chatWindows.get(username).getStyledDocument();
		}

		Style userStyle = doc.getStyle("User style");
		if (userStyle == null) {
			userStyle = doc.addStyle("User style", null);
			StyleConstants.setBold(userStyle, true);
		}

		if (yourMessage == true) {
			StyleConstants.setForeground(userStyle, Color.red);
		} else {
			StyleConstants.setForeground(userStyle, Color.BLUE);
		}

		// In ra màn hình tên người gửi
		try {
			doc.insertString(doc.getLength(), username + ": ", userStyle);
		} catch (BadLocationException e) {
		}

		Style iconStyle = doc.getStyle("Icon style");
		if (iconStyle == null) {
			iconStyle = doc.addStyle("Icon style", null);
		}

		StyleConstants.setIcon(iconStyle, new ImageIcon(emoji));

		// In ra màn hình Emoji
		try {
			doc.insertString(doc.getLength(), "invisible text", iconStyle);
		} catch (BadLocationException e) {
		}

		// Xuống dòng
		try {
			doc.insertString(doc.getLength(), "\n", userStyle);
		} catch (BadLocationException e) {
		}

		autoScroll();
	}

	/**
	 * Insert a file into chat pane.
	 */
	private void newFile(String username, String filename, byte[] file, Boolean yourMessage) {

		StyledDocument doc;
		String window = null;
		if (username.equals(this.username)) {
			window = lbReceiver.getText();
		} else {
			window = username;
		}
		doc = chatWindows.get(window).getStyledDocument();

		Style userStyle = doc.getStyle("User style");
		if (userStyle == null) {
			userStyle = doc.addStyle("User style", null);
			StyleConstants.setBold(userStyle, true);
		}

		if (yourMessage == true) {
			StyleConstants.setForeground(userStyle, Color.red);
		} else {
			StyleConstants.setForeground(userStyle, Color.BLUE);
		}

		try {
			doc.insertString(doc.getLength(), username + ": ", userStyle);
		} catch (BadLocationException e) {
		}

		Style linkStyle = doc.getStyle("Link style");
		if (linkStyle == null) {
			linkStyle = doc.addStyle("Link style", null);
			StyleConstants.setForeground(linkStyle, Color.BLUE);
			StyleConstants.setUnderline(linkStyle, true);
			StyleConstants.setBold(linkStyle, true);
			linkStyle.addAttribute("link", new HyberlinkListener(filename, file));
		}

		if (chatWindows.get(window).getMouseListeners() != null) {
			// Tạo MouseListener cho các đường dẫn tải về file
			chatWindows.get(window).addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					Element ele = doc.getCharacterElement(chatWindow.viewToModel(e.getPoint()));
					AttributeSet as = ele.getAttributes();
					HyberlinkListener listener = (HyberlinkListener) as.getAttribute("link");
					if (listener != null) {
						listener.execute();
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub

				}

			});
		}

		// In ra đường dẫn tải file
		try {
			doc.insertString(doc.getLength(), "<" + filename + ">", linkStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		// Xuống dòng
		try {
			doc.insertString(doc.getLength(), "\n", userStyle);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}

		autoScroll();
	}

	/**
	 * Insert a new message into chat pane.
	 */
	private void newMessage(String username, String message, Boolean yourMessage) {

		StyledDocument doc;
		if (username.equals(this.username)) {
			doc = chatWindows.get(lbReceiver.getText()).getStyledDocument();
		} else {
			doc = chatWindows.get(username).getStyledDocument();
		}

		Style userStyle = doc.getStyle("User style");
		if (userStyle == null) {
			userStyle = doc.addStyle("User style", null);
			StyleConstants.setBold(userStyle, true);
		}

		if (yourMessage == true) {
			StyleConstants.setForeground(userStyle, Color.red);
		} else {
			StyleConstants.setForeground(userStyle, Color.BLUE);
		}

		// In ra tên người gửi
		try {
			doc.insertString(doc.getLength(), username + ": ", userStyle);
		} catch (BadLocationException e) {
		}

		Style messageStyle = doc.getStyle("Message style");
		if (messageStyle == null) {
			messageStyle = doc.addStyle("Message style", null);
			StyleConstants.setForeground(messageStyle, Color.BLACK);
			StyleConstants.setBold(messageStyle, false);
		}

		// In ra nội dung tin nhắn
		try {
			doc.insertString(doc.getLength(), message + "\n", messageStyle);
		} catch (BadLocationException e) {
		}

		autoScroll();
	}

	/**
	 * Luồng nhận tin nhắn từ server của mỗi client
	 */
	class Receiver implements Runnable {

		private DataInputStream dis;

		public Receiver(DataInputStream dis) {
			this.dis = dis;
		}

		@Override
		public void run() {
			try {

				while (true) {
					// Chờ tin nhắn từ server
					String method = dis.readUTF();

					if (method.equals("Text")) {
						// Nhận một tin nhắn văn bản
						String sender = dis.readUTF();
						String message = dis.readUTF();

						// In tin nhắn lên màn hình chat với người gửi
						newMessage(sender, message, false);
					}

					else if (method.equals("Emoji")) {
						// Nhận một tin nhắn Emoji
						String sender = dis.readUTF();
						String emoji = dis.readUTF();

						// In tin nhắn lên màn hình chat với người gửi
						newEmoji(sender, emoji, false);
					}

					else if (method.equals("File")) {
						// Nhận một file
						String sender = dis.readUTF();
						String filename = dis.readUTF();
						int size = Integer.parseInt(dis.readUTF());
						int bufferSize = 2048;
						byte[] buffer = new byte[bufferSize];
						ByteArrayOutputStream file = new ByteArrayOutputStream();

						while (size > 0) {
							dis.read(buffer, 0, Math.min(bufferSize, size));
							file.write(buffer, 0, Math.min(bufferSize, size));
							size -= bufferSize;
						}

						// In ra màn hình file đó
						newFile(sender, filename, file.toByteArray(), false);

					}

					else if (method.equals("Online users")) {
						// Nhận yêu cầu cập nhật danh sách người dùng trực tuyến
						String[] users = dis.readUTF().split(",");
						onlineUsers.removeAllItems();

						String chatting = lbReceiver.getText();

						boolean isChattingOnline = false;

						for (String user : users) {
							if (user.equals(username) == false) {
								// Cập nhật danh sách các người dùng trực tuyến vào ComboBox onlineUsers (trừ
								// bản thân)
								onlineUsers.addItem(user);
								if (chatWindows.get(user) == null) {
									JTextPane temp = new JTextPane();
									temp.setFont(new Font("Arial", Font.PLAIN, 14));
									temp.setEditable(false);
									chatWindows.put(user, temp);
								}
							}
							if (chatting.equals(user)) {
								isChattingOnline = true;
							}
						}

						if (isChattingOnline == false) {
							// Nếu người đang chat không online thì chuyển hướng về màn hình mặc định và
							// thông báo cho người dùng
							onlineUsers.setSelectedItem(" ");
							JOptionPane.showMessageDialog(null,
									chatting + " is offline!\nYou will be redirect to default chat window");
						} else {
							onlineUsers.setSelectedItem(chatting);
						}

						onlineUsers.validate();
					}

					else if (method.equals("Safe to leave")) {
						// Thông báo có thể thoát
						break;
					}

				}

			} catch (IOException ex) {
				System.err.println(ex);
			} finally {
				try {
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * MouseListener cho các đường dẫn tải file.
	 */
	class HyberlinkListener extends AbstractAction {
		String filename;
		byte[] file;

		public HyberlinkListener(String filename, byte[] file) {
			this.filename = filename;
			this.file = Arrays.copyOf(file, file.length);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			execute();
		}

		public void execute() {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setSelectedFile(new File(filename));
			int rVal = fileChooser.showSaveDialog(frame.getParent());
			if (rVal == JFileChooser.APPROVE_OPTION) {

				// Mở file đã chọn sau đó lưu thông tin xuống file đó
				File saveFile = fileChooser.getSelectedFile();
				BufferedOutputStream bos = null;
				try {
					bos = new BufferedOutputStream(new FileOutputStream(saveFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}

				// Hiển thị JOptionPane cho người dùng có muốn mở file vừa tải về không
				int nextAction = JOptionPane.showConfirmDialog(null,
						"Saved file to " + saveFile.getAbsolutePath() + "\nDo you want to open this file?",
						"Successful", JOptionPane.YES_NO_OPTION);
				if (nextAction == JOptionPane.YES_OPTION) {
					try {
						Desktop.getDesktop().open(saveFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (bos != null) {
					try {
						bos.write(this.file);
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * MouseAdapter cho các Emoji.
	 */
	class IconListener extends MouseAdapter {
		String emoji;

		public IconListener(String emoji) {
			this.emoji = emoji;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (txtMessage.isEnabled() == true) {

				try {
					dos.writeUTF("Emoji");
					dos.writeUTF(lbReceiver.getText());
					dos.writeUTF(this.emoji);
					dos.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
					newMessage("ERROR", "Network error!", true);
				}

				// In Emoji lên màn hình chat với người nhận
				newEmoji(username, this.emoji, true);
			}
		}
	}
}
