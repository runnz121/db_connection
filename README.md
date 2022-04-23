### 공부하는 이유
1. spring에서 DB에 접근하는 기술은 점진적으로 발전해왔다.
2. 기술중에는 옛날 기술인 jdbc, mybatis 등 부터 최신 기술인 jpa, querydslr과 같이 많은 기술들이 있는데,
3. 왜 이 기술들이 이렇게 발전해 왔는지에 대해 공부하고, 어떤 부분이 개선되어왔는지 알기 위해 공부하게됨


### 1.DB 설정 
1. 생성 : jdbc:h2:~/jdbcTest
2. 접근 : jdbc:h2:tcp://localhost/~/jdbcTest

### 정리
1. JDBC 등장 이유
   1. 옛날(db마다 사용방법이 다름)
      1. 일반적으로 tcp/ip로 db와 서버와 연동함
      2. sql 전달
      3. 결과 응답
   -> 따라서 각 db 마다 연결, 사 방법을 따로 고려해줘야 함
   -> jdbc 의 등장 : java에서 db에 접속하는 방법을 통일
      4. jdbc : db벤더에서 자신의 db에 맞도록 구현해서 라이브러리로 제공
      5. 따라서 jdbc 표준 인터페이스에서만 맞춰서 구현하기만 하면 됨
   2. jdbc의 사용
      1. 요새는 jdbc를 직접 상요하지 않음, Mybatis 나 jpa와 같이 중간에
         연동하여 사용함
   3. mybatis, sqlmapper, jdbctemplate
      1. sql 응답 결과를 객체로 편리하게 반환
      2. 작성자가 sql 를 직접 작성해야함
   4. ORM, jpa(인터페이스), 하이버네이트(구현체), 이클립스 링크
      1. jpa 구현체가 맵핑 정보를 바탕으로 sql 을 만들어 jdbc에 전달
   

### 코드
1. DB 연결
```
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
		} catch (SQLException e) { //checked exception을 runtime excetpion으로
			throw new IllegalStateException(e);
		}
	}
```

2. test 코드작성
```
import static org.assertj.core.api.Assertions.*;

@Slf4j
public class DBConnectionUtilTest {

	@Test
	void connection() {
		Connection connection = DBConnectionUtil.getConneciton();
		assertThat(connection).isNotNull();
	}
}
```

여러개의 드라이버가 있지만 드라이버 메니저가 이를 구분하여 알맞는 드라이버를 선택하게된다.
1. url 정보를 보고 드라이버메니저가 처리할 수 있는지를 확인(ex. h2로 시작하는 url인 경우, 맞는 url일 때까지 라이브러리에 있는 드라이버를 모두 확인)
2. 확인후 실제 컨넥션 연결하여 확인함 
3. 