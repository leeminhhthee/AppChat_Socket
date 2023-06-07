package Server;

import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.swing.JOptionPane;

public class Server {
	private Object lock;

	private ServerSocket s;
	private Socket socket;
	static ArrayList<Handler> clients = new ArrayList<Handler>();

	/**
	 * Tải lên danh sách tài khoản từ SQL Server
	 */
	private void loadAccounts() {
		try {
			ConnectDB cdb = new ConnectDB();
			cdb.Connect();
			Connection conn = DriverManager
					.getConnection("jdbc:sqlserver://HP;databaseName=Chatting;user=sa;password=sa");
			PreparedStatement st = conn.prepareStatement("SELECT * FROM UserAccount");
//				st.setString(1, username);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				String username = rs.getString("Username");
				String passDB = rs.getString("Password");
				String passDB1 = null;
				try {
					// Tạo file chứa private key
					FileInputStream fis = new FileInputStream("E:/AppChat/privateKey.rsa");
					byte[] b = new byte[fis.available()];
					fis.read(b);
					fis.close();
					// Tạo private key
					PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b);
					KeyFactory factory = KeyFactory.getInstance("RSA");
					PrivateKey priKey = factory.generatePrivate(spec);
					// Giải mã dữ liệu
					Cipher c = Cipher.getInstance("RSA");
					c.init(Cipher.DECRYPT_MODE, priKey);
					byte decryptOut[] = c.doFinal(Base64.getDecoder().decode(passDB));
//					System.out.println("Mật khẩu của các user là: " + new String(decryptOut));
					passDB1 = new String(decryptOut, "UTF-8");

					clients.add(new Handler(username, passDB1, false, lock));
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Server() throws IOException {
		try {
			// Object dùng để synchronize cho việc giao tiếp với các người dùng
			lock = new Object();

			// Đọc danh sách tài khoản đã đăng ký
			this.loadAccounts();
			// Socket dùng để xử lý các yêu cầu đăng nhập/đăng ký từ user
			s = new ServerSocket(2003);

			while (true) {
				// Đợi request đăng nhập/đăng xuất từ client
				socket = s.accept();
				
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

				// Đọc yêu cầu đăng nhập/đăng xuất
				String request = dis.readUTF();

				if (request.equals("Sign up")) {
					// Yêu cầu đăng ký từ user

					String username = dis.readUTF();
					String password = dis.readUTF();

					// Kiểm tra tên đăng nhập đã tồn tại hay chưa
					if (isExisted(username) == false) {

						// Tạo một Handler để giải quyết các request từ user này
						Handler newHandler = new Handler(socket, username, password, true, lock);
						clients.add(newHandler);

						// Gửi thông báo đăng nhập thành công cho user
						Thread thread = new Thread() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								String strEncrypt = null;
								try {
									// �?�?c file chứa public key
									FileInputStream fis = new FileInputStream("E:/AppChat/publicKey.rsa");
									byte[] b = new byte[fis.available()];
									fis.read(b);
									fis.close();
									// Tạo public key
									X509EncodedKeySpec spec = new X509EncodedKeySpec(b);
									KeyFactory factory = KeyFactory.getInstance("RSA");
									PublicKey pubKey = factory.generatePublic(spec);
									// Mã hoá dữ liệu
									Cipher c = Cipher.getInstance("RSA");
									c.init(Cipher.ENCRYPT_MODE, pubKey);
									byte encryptOut[] = c.doFinal(password.getBytes());
									strEncrypt = Base64.getEncoder().encodeToString(encryptOut);
//									System.out.println("Chuỗi sau khi mã hoá: " + strEncrypt);
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								ConnectDB cdb = new ConnectDB();
								cdb.Connect();
								String sql = "insert into UserAccount values ('" + username + "','" + strEncrypt + "')";
								int s = cdb.executeDB(sql);
							}
						};
						thread.start();
						dos.writeUTF("Sign up successful");
						dos.flush();

						// Tạo một Thread để giao tiếp với user này
						Thread t = new Thread(newHandler);
						t.start();

						// Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực
						// tuyến
						updateOnlineUsers();
					} else {
						// Thông báo đăng nhập thất bại
						dos.writeUTF("This username is being used");
						dos.flush();
					}
				} else if (request.equals("Log in")) {
					// Yêu cầu đăng nhập từ user

					String username = dis.readUTF();
					String password = dis.readUTF();

					// Kiểm tra tên đăng nhập có tồn tại hay không
					if (isExisted(username) == true) {
						for (Handler client : clients) {
							if (client.getUsername().equals(username)) {
								// Kiểm tra mật khẩu có trùng khớp không
								if (password.equals(client.getPassword())) {
									System.out.println(client.getPassword());
									// Tạo Handler mới để giải quyết các request từ user này
									Handler newHandler = client;
									newHandler.setSocket(socket);
									newHandler.setIsLoggedIn(true);

									// Thông báo đăng nhập thành công cho người dùng
									dos.writeUTF("Log in successful");
									dos.flush();

									// Tạo một Thread để giao tiếp với user này
									Thread t = new Thread(newHandler);
									t.start();

									// Gửi thông báo cho các client đang online cập nhật danh sách người dùng trực
									// tuyến
									updateOnlineUsers();
								} else {
									dos.writeUTF("Password is not correct");
									dos.flush();
								}
								break;
							}
						}

					} else {
						dos.writeUTF("This username is not exist");
						dos.flush();
					}
				}

			}

		} catch (Exception ex) {
			System.err.println(ex);
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Kiểm tra username đã tồn tại hay chưa
	 */
	public boolean isExisted(String name) {
		for (Handler client : clients) {
			if (client.getUsername().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gửi yêu cầu các user đang online cập nhật lại danh sách người dùng trực tuyến
	 * Được gọi mỗi khi có 1 user online hoặc offline
	 */
	public static void updateOnlineUsers() {
		String message = " ";
		for (Handler client : clients) {
			if (client.getIsLoggedIn() == true) {
				message += ",";
				message += client.getUsername();
			}
		}
		for (Handler client : clients) {
			if (client.getIsLoggedIn() == true) {
				try {
					client.getDos().writeUTF("Online users");
					client.getDos().writeUTF(message);
					client.getDos().flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}

/**
 * Luồng riêng dùng để giao tiếp với mỗi user
 */
class Handler implements Runnable {
	// Object để synchronize các hàm cần thiết
	// Các client đều có chung object này được thừa hưởng từ chính server
	private Object lock;

	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private String username;
	private String password;
	private boolean isLoggedIn;

	public Handler(Socket socket, String username, String password, boolean isLoggedIn, Object lock)
			throws IOException {
		this.socket = socket;
		this.username = username;
		this.password = password;
		this.dis = new DataInputStream(socket.getInputStream());
		this.dos = new DataOutputStream(socket.getOutputStream());
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}

	public Handler(String username, String password, boolean isLoggedIn, Object lock) {
		this.username = username;
		this.password = password;
		this.isLoggedIn = isLoggedIn;
		this.lock = lock;
	}

	public void setIsLoggedIn(boolean IsLoggedIn) {
		this.isLoggedIn = IsLoggedIn;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Đóng socket kết nối với client Được gọi khi người dùng offline
	 */

	public boolean getIsLoggedIn() {
		return this.isLoggedIn;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	public DataOutputStream getDos() {
		return this.dos;
	}

	@Override
	public void run() {

		while (true) {
			try {
				String message = null;

				// Đọc yêu cầu từ user
				message = dis.readUTF();

				// Yêu cầu đăng xuất từ user
				if (message.equals("Log out")) {

					// Thông báo cho user có thể đăng xuất
					dos.writeUTF("Safe to leave");
					dos.flush();

					// Đóng socket và chuyển trạng thái thành offline
					socket.close();
					this.isLoggedIn = false;

					// Thông báo cho các user khác cập nhật danh sách người dùng trực tuyến
					Server.updateOnlineUsers();
					break;
				}

				// Yêu cầu gửi tin nhắn dạng văn bản
				else if (message.equals("Text")) {
					String receiver = dis.readUTF();
					String content = dis.readUTF();

					for (Handler client : Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("Text");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(content);
								client.getDos().flush();
								break;
							}
						}
					}
				}

				// Yêu cầu gửi tin nhắn dạng Emoji
				else if (message.equals("Emoji")) {
					String receiver = dis.readUTF();
					String emoji = dis.readUTF();

					for (Handler client : Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("Emoji");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(emoji);
								client.getDos().flush();
								break;
							}
						}
					}
				}

				// Yêu cầu gửi File
				else if (message.equals("File")) {

					// Đọc các header của tin nhắn gửi file
					String receiver = dis.readUTF();
					String filename = dis.readUTF();
					int size = Integer.parseInt(dis.readUTF());
					int bufferSize = 2048;
					byte[] buffer = new byte[bufferSize];

					for (Handler client : Server.clients) {
						if (client.getUsername().equals(receiver)) {
							synchronized (lock) {
								client.getDos().writeUTF("File");
								client.getDos().writeUTF(this.username);
								client.getDos().writeUTF(filename);
								client.getDos().writeUTF(String.valueOf(size));
								while (size > 0) {
									// Gửi lần lượt từng buffer cho người nhận cho đến khi hết file
									dis.read(buffer, 0, Math.min(size, bufferSize));
									client.getDos().write(buffer, 0, Math.min(size, bufferSize));
									size -= bufferSize;
								}
								client.getDos().flush();
								break;
							}
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}
