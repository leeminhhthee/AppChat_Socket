package Server;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConnectDB {
	Connection conn;
	Statement stmt;
	ResultSet rs;
	public void Connect() {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			// start connecting EmployeeDB database
			conn = DriverManager.getConnection("jdbc:sqlserver://HP;databaseName=Chatting;user=sa;password=sa");
			System.out.println("Connected");
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	//thao tac voi du lieu
	public int executeDB(String sql) {
		int n=0;		
		try {
			Connect();
			stmt = conn.createStatement();
			n = stmt.executeUpdate(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return n;
	}
	//truy van du lieu
	public ResultSet queryDB(String sql) {
		try {
			Connect();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return rs;
	}
}