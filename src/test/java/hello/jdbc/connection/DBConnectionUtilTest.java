package hello.jdbc.connection;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class DBConnectionUtilTest {

	@Test
	void connection() {
		Connection connection = DBConnectionUtil.getConneciton();
		assertThat(connection).isNotNull();
	}
}
