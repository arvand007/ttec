package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/*
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
*/
import java.sql.Connection;
import java.sql.DriverManager;

public class JdbcConnection {
	Logger logger = LogManager.getLogger(JdbcConnection.class);
	private Statement stmt=null;
	Connection conn;
	public  JdbcConnection() {
		/*
		MysqlDataSource dataSource = new MysqlDataSource();
		dataSource.setUser("root");
		dataSource.setPassword("password");
		dataSource.setServerName("localhost");
		dataSource.setDatabaseName("ivrtek");
		dataSource.setPort(3306);*/
		
		try {
			  Class.forName("org.postgresql.Driver");
			  //local connection
			// conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Trivia","Trivia", "Trivia");
			  
			  //Heroku Postgress
			  //postgres://upiwrvfjoehllq:ca88f716d75ea49d2baddbfb1f89633cf17bf555bcc0aae3741815cd0ca2fd56@ec2-107-22-162-82.compute-1.amazonaws.com:5432/d35rq8dbg19utn
			conn = DriverManager.getConnection("jdbc:postgresql://ec2-107-22-162-82.compute-1.amazonaws.com:5432/d35rq8dbg19utn?sslmode=require","upiwrvfjoehllq", "ca88f716d75ea49d2baddbfb1f89633cf17bf555bcc0aae3741815cd0ca2fd56");
			stmt = (Statement) conn.createStatement();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.toString());
			e.printStackTrace();
		}
		
	}
	
	public ResultSet ExecuteQuery(String Query) {
		ResultSet rs=null;
		try {
			rs = stmt.executeQuery(Query);
		} catch (SQLException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}
		return rs;
	}
	
	public void executeUpdate(String Query) {	
		try {
			stmt.executeUpdate(Query);
		} catch (SQLException e) {
			logger.error(e.toString());
			e.printStackTrace();
		}		
	}
	
	public boolean CloseConnection() {
		boolean Returnvalue=true;
		try {
			
			stmt.close();
			conn.close();
			logger.debug("Db connection closed");
		} catch (SQLException e) {
			logger.error("error closing db connection");
			Returnvalue=false;
			logger.error(e.toString());
			e.printStackTrace();
		}
		return Returnvalue;
	}
	
}
