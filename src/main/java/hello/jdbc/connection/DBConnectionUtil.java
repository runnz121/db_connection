package hello.jdbc.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import lombok.extern.slf4j.Slf4j;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

	//java가 제공하는 기본 커넥션
	//try catch로 선언해야 함
	public static Connection getConneciton() {
		try {
			//Driver를 통해서 DB에 접속하는데, DriverManager가 (구현체) 이를 찾아서 연결
			Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			log.info("get connection ={}, class = {}", connection, connection.getClass());
			return connection;
			//org.h2.jdbc.JdbcConnection -> 이게 connection 구현체


		} catch (SQLException e) { //checked exception을 runtime excetpion으로
			throw new IllegalStateException(e);
		}
	}
}
