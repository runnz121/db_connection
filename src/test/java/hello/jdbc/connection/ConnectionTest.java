package hello.jdbc.connection;

import static hello.jdbc.connection.ConnectionConst.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectionTest {

	@Test
	public void driverManager() throws SQLException {
		//connection 획득
		//서로 다른 connection을 2번 호출

		//driver manager -> connection 획득 할 떄 마다 url, username, pass 를 매번 넘겨야 한다(파라미터가 매번 넘어감)
		Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		log.info("connection = {}, class = {}", con1, con1.getClass());
		log.info("connection = {}, class = {}", con2, con2.getClass());
	}


	//히카리 풀 컨넥션 생성 테스트
	@Test
	public void dataSourceConnectionPool() throws SQLException, InterruptedException {
		//커넥션 풀링
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(URL);
		dataSource.setUsername(USERNAME);
		dataSource.setPassword(PASSWORD);
		dataSource.setMaximumPoolSize(10);
		dataSource.setPoolName("MyPool");

		userDataSource(dataSource);
		Thread.sleep(1000);
	}



	@Test
	public void dataSourceDriverManager() throws SQLException {
		//DriverManagerDatasource - 항상 새로운 커넥션을 획득(spring에서 제공하는 jdbc data source)
		//datasource를 구현함으로(drivermanger) -> 이걸로 받을 수 있다.

		//초기 datasource 객체 생성에만 파라미터를 넘기고 추후 connection 만 갖고옴
		DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
		userDataSource(dataSource);
	}

	private void userDataSource(DataSource dataSource) throws SQLException {
		Connection con1 = dataSource.getConnection();
		Connection con2 = dataSource.getConnection();
		log.info("connection = {}, class = {}", con1, con1.getClass());
		log.info("connection = {}, class = {}", con2, con2.getClass());
	}
}
