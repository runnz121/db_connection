### 공부하는 이유
1. spring에서 DB에 접근하는 기술은 점진적으로 발전해왔다.
2. 기술중에는 옛날 기술인 jdbc, mybatis 등 부터 최신 기술인 jpa, querydsl과 같이 많은 기술들이 있는데,
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


3. jdbc connection 쿼리 작성
```
@Slf4j
public class MemberRepositoryV0 {

	public Member save(Member member) throws SQLException {
		String sql = "insert into member(member_id, money) values (?,?)";

		Connection con = null; // -> sql 쿼리문을 그대로 넣음
		PreparedStatement pstm = null; // -> 파라미터를 바인딩


		//sql exception이 발상햄으로 try catch 사용
		try {
			con = getConnection();
			pstm = con.prepareStatement(sql);
			pstm.setString(1, member.getMemberId());
			pstm.setInt(2, member.getMoney());

			pstm.executeUpdate(); // 준비된 쿼리를 실행하는 부분 -> 쿼리 실행으로 인해 영향을 받은 row의 수를 리턴

			return member;
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally { // finally에서 close를 해야 한다. -> 항상 호출되는 것을 보장해도록
			close(con, pstm, null);
		}
	}

	//사용한 자원들을 모두 닫아줘야 한다.
	private void close(Connection con, Statement statement, ResultSet resultSet) {

		if (resultSet != null) {
			try {
				resultSet.close(); // 외부 리소스를 쓰는 것임으로 close로 닫아주어야 함(연결 부분을)
			} catch (SQLException e) {
				log.info("error", e);
			}
		}


		if (statement != null) {
			try {
				statement.close(); // exception 발생시 ->  이 try, catch 문에서 끝남으로 아래 con.close에는 영향을 주지 않는다.
			} catch (SQLException e) {
				log.info("error", e);
			}
		}

		if (con != null) {
			try {
				con.close(); // 외부 리소스를 쓰는 것임으로 close로 닫아주어야 함(연결 부분을)
			} catch (SQLException e) {
				log.info("error", e);
			}
		}
	}
```
1. 자원을 사용하였으면 모두 close()로 닫아 주어야 한다
2. finally 부분에 선언하여 exception이 발생하여도 close가 실행될 수 있도록 한다.
3. select 시
```
   public Member findById(String memberId) throws SQLException {
   String sql = "select * from member where member_id = ?";

   	Connection con = null; // finally catch 때문에 밖에다 선언 해야 함
   	PreparedStatement pstmt = null;
   	ResultSet res = null;
   	
   	try {
   		con = getConnection();
   		pstmt = con.prepareStatement(sql);
   		pstmt.setString(1, memberId);
   		
   		res = pstmt.executeQuery(); // select는 executeQueyr로 해야함
   		
   		if (res.next()) {
   			Member member = new Member();
   			member.setMemberId(res.getString("member_id"));
   			member.setMoney(res.getInt("money"));
   			return member;
   		} else {
   			throw new NoSuchElementException("member not found memberId = " + memberId);
   		}
   		
   	} catch (SQLException e) {
   		log.error("db error", e);
   		throw e;
   	} finally {
   		close(con, pstmt,res);
   	}
   }
```
1. select 구문 실행시 executeQuery() 로 실행해야한다.
2. ResultSet 내부에는 커서가 있다. 따라서 res.next() 를 최초 한번 호출해야 커서가 이동하며
3. 커서가 없다면 데이터가 없다는 뜻이다.

```
@Slf4j
class MemberRepositoryV0Test {

	MemberRepositoryV0 memberRepositoryV0 = new MemberRepositoryV0();

	@Test
	void curd() throws SQLException {

		//save
		Member member = new Member("memberV6", 10000);
		memberRepositoryV0.save(member);

		//findById
		Member findMemeber = memberRepositoryV0.findById(member.getMemberId());

		//@Data //equalsAndHashcode를 @data가 같이 구현해준다.
		log.info("findMember = {}", findMemeber);
		log.info("member != findMember {}", member == findMemeber);
		log.info("member equals findMember {}", member.equals(findMemeber));
		assertThat(findMemeber).isEqualTo(member);

		//update: money :10000 -> 20000
		memberRepositoryV0.update(member.getMemberId(), 20000);
		Member updateMember = memberRepositoryV0.findById(member.getMemberId());
		//assertThat(updateMember).isEqualTo(member);
		assertThat(updateMember.getMoney()).isEqualTo(20000); //이걸로 해야함

		//delete -> assertThatThrowBy -> 예외를 던지는 assert -> 예외를 던져야 테스트가 성공 -> 예외가 남으로 반복적으로 실행이 가능!!
		//하지만 중간에 다른예외로 익셉션을 터트리면 하위 메소드가 호출이 되지 않는다 .
		memberRepositoryV0.delete(member.getMemberId());
		assertThatThrownBy(() -> memberRepositoryV0.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);

		//이미 삭제된 멤버는 확인할 수 없음으로
		//Member deletedMember = memberRepositoryV0.findById("memberV6");

	}
}
```
1. assertThatThrownBy -> 예외가 터져야 성공하는 경우(중복 방지값이 들어가는 테스트를 할 경우 여러번 실행 가능!)
2. 하지만 중간에 얘외가 터지는 다른 검증로직이 들어갈 경우 하위 로직까지 실행되지 않음으로 문제가 있음

# 커넥션 풀
1. db 드라이브 -> tcp/ip -> db 커넥션 생성
2. id, pass 인증 완료 후 db session 생성
3. 이러한 커넥션을 새로 만드는 것이 오래걸림 -> 커넥션 풀을 만들고 거기서 커넥션을 갖고옴

4. 커넥션 풀 -> 위의 커넥션된 세션을 여러개 넣어 놓음 
5. 이미 연결이 되어 있는 상태임으로 db에 sql을 바로 전달
6. 커넥션 다 쓰면 종료가 아닌 풀에 다시 반환
7. hikaricp , commons-dbcp2, tomcat-jdbc pool -> 커넥션 풀 오픈소스

### 커넥션을 획득하는 방법
1. DriverManager 사용 
2. DBCP2 커넥션 풀에서 조회
3. HikariCP 커넥션 풀에서 조회

-> 이와 같이 여러가지 방법으로 커넥션을 획득 할 수 있다.
-> 이러한 방법을 추상화 시킴!!!!(공통적인 핵심 기능을 집중 시킴)
-> 이게 DataSoruce
-> 필요한 부분을 직접 구현하면 됨


### DriverManager vs DataSource 의 차이

DriverManger
```
Connection con1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		Connection con2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
		log.info("connection = {}, class = {}", con1, con1.getClass());
		log.info("connection = {}, class = {}", con2, con2.getClass());
```
connection 생성시마다 파라미터 넘겨줌

Datasource
```
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
```
초기 datasource 객체 생성시에만 파라미터 한번만 넘겨주면됨
추후 getconnection으로 연결만 획득
 -> 연결 생성과 호출을 분리 할 수 있다!

### connection Pool 생성하기

````
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
````
Hikari dataSource로 생성된 것을 확인 할 수 있음 connection Pool 10개로 지정했음으로 아래 로그에서 10개 모두 위에 설정한 컨넥션으로 설정됨을 알 수 있다.
Thread.sleep을 걸지않으면 로딩이 너무 빠르게 지나가 아래 로그를 확인할 수 없음 
컨넥션 풀 채울시 -> 별도의 스레드를 사용(애플리케이션 실행시간 영향 안주기 위해)

hikari가 관리하고 있는 컨넥션 
````
13:58:54.181 [Test worker] INFO hello.jdbc.connection.ConnectionTest - connection = HikariProxyConnection@416201381 wrapping conn0: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class = class com.zaxxer.hikari.pool.HikariProxyConnection
13:58:54.181 [Test worker] INFO hello.jdbc.connection.ConnectionTest - connection = HikariProxyConnection@1178290888 wrapping conn1: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class = class com.zaxxer.hikari.pool.HikariProxyConnection
````


````
13:52:03.198 [MyPool housekeeper] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Pool stats (total=2, active=2, idle=0, waiting=0)
13:52:03.206 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn2: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.210 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn3: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.214 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn4: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.233 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn5: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.238 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn6: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.239 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn7: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.241 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn8: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA
13:52:03.242 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - Added connection conn9: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA

/**
이부분이 위에서 지정한 총 10개의 컨넥선 풀이 생성되었음을 알려줌
**/
13:52:03.242 [MyPool connection adder] DEBUG com.zaxxer.hikari.pool.HikariPool - MyPool - After adding stats (total=10, active=2, idle=8, waiting=0)
````

만일 connection이 설정한 값보다 더 많이 호출 된다면 -> 컨넥션 반환될 떄까지 대기 상태 들어감 -> 설정을 통해 수정할 수 있음


### DataSource, JdbcUtils 사용(MemberRepositoryV1)

````
@Slf4j
public class MemberRepositoryV1 {

	private final DataSource dataSource;

	public MemberRepositoryV1(DataSource dataSource) {
		this.dataSource = dataSource;
	}
````
datasource 주입 받음 

````
//사용한 자원들을 모두 닫아줘야 한다.
	private void close(Connection con, Statement statement, ResultSet resultSet) {
		/**
		 * JdbcUtils가 제공하는 close 메소드를 통해 try catch 문을 사용하지 않고 연결을 종료 할 수 있다.
		 */
		JdbcUtils.closeResultSet(resultSet);
		JdbcUtils.closeStatement(statement);
		JdbcUtils.closeConnection(con);
````
연결 종료시 JdbcUtils에서 제공하는 메소드를 이용해서 종₩

````
	/**
	 * datasource에서 
	 * @return
	 * @throws SQLException
	 */
	private Connection getConnection() throws SQLException {
		Connection con = dataSource.getConnection();
		log.info("get conneciton={}, class={}");
		return con;
	}
````
connection 획득시 datasource에서 갖고옴 


### DriverManager로 호출시

호출 때마다 새로운 컨넥션을 생성함 -> 느려짐(MemberRepositoryV1Test)
````
 @BeforeEach
 void beforeEach() {
     //기본 DriverManager - 항상 새로운 커넥션을 획득
     DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
     memberRepositoryV1 = new MemberRepositoryV1(dataSource);
 }
````

````
14:30:39.416 [Test worker] DEBUG org.springframework.jdbc.datasource.DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp://localhost/~/jdbcTest]
14:30:39.418 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=conn2: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:30:39.418 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - resultSize=1
14:30:39.419 [Test worker] DEBUG org.springframework.jdbc.datasource.DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp://localhost/~/jdbcTest]
14:30:39.419 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=conn3: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:30:39.422 [Test worker] DEBUG org.springframework.jdbc.datasource.DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp://localhost/~/jdbcTest]
14:30:39.423 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=conn4: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:30:39.424 [Test worker] DEBUG org.springframework.jdbc.datasource.DriverManagerDataSource - Creating new JDBC DriverManager Connection to [jdbc:h2:tcp://localhost/~/jdbcTest]
14:30:39.424 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=conn5: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
````
creating new JDBC DriverMAanger Connection이라는 문구를 확인할 수 있다


### Connection Pool 사용시
````
 /**
  * 히카리 컨넥션 풀링 사용
  */
 @BeforeEach
 void beforeEach() {
     HikariDataSource dataSource = new HikariDataSource();
     dataSource.setJdbcUrl(URL);
     dataSource.setUsername(USERNAME);
     dataSource.setPassword(PASSWORD);

     memberRepositoryV1 = new MemberRepositoryV1(dataSource);
 }
````

````
14:35:38.735 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=HikariProxyConnection@36531985 wrapping conn0: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:35:38.735 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - resultSize=1
14:35:38.735 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=HikariProxyConnection@815336475 wrapping conn0: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:35:38.737 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=HikariProxyConnection@951988316 wrapping conn0: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
14:35:38.739 [Test worker] INFO hello.jdbc.repository.MemberRepositoryV1 - get conneciton=HikariProxyConnection@1434066477 wrapping conn0: url=jdbc:h2:tcp://localhost/~/jdbcTest user=SA, class=class hello.jdbc.repository.MemberRepositoryV1
````

wrapping coon0 이 모두 conn0 만 사용됨을 알 수 있다. 
왜?
-> connection pool 은 close() 호출시 connection을 종료하는 것이 아닌, 반환을 한다
따라서 반환된 connection을 계속 사용 했기 때문에 저런 현상이 발생!
객체 인스턴스 주소는 다르지만 컨넥션은 같다!!
proxy 객체 생성 -> 컨넥션 담고 연결 -> 반환


# 트랜젝션
### 1. 커밋
모든 작업이 성공해서 데이터베이스에 정상 반영 하는 것

### 2. 롤백
작업 중 하나라도 실패해서 거래 이전으로 되돌리는 것

### 3. 트랜잭션 ACID
원자성 : 트랜젝션 내 실행한 작업들은 모두 성공 혹은 모두 실패
일관성 : 모든 트랜잭션은 일관성 있는 데이터베이스를 유지(무결성 제약 조건)
격리성 : 동시에 실행되는 트랜젝션들이 서로에게 영향을 미치지 않도록 격리
지속성 : 트랜젝션을 성공적으로 끝내면 항상 기록되어야 한다

### 4. 트랜젝션 격리 수준
READ UNCOMMITTED
READ COMMITTED
REPEATABLE READ
SEREIALIZABLE

### 데이터베이스 연결 구조 DB 세션
클라이언트 -> 커넥션 -> DB(커넥션 - 세션)
DB 내에서 커넥션을 통해 세션이 만들어지고 이후 이 세션을 통해 SQL을 실행 
컨넥션 : 세션 = 1 : 1


### 트랜젝션시 세션 상태
커밋 호출 전까지 -> 임시로 데이터 저장 (커밋이 되어야 데이터가 저장)
따라서 트랜젝션 시작한 세션만 변경 데이터가 보이고 다른 세션은 보이지 않음 

만약 다른 세션에서 변경된 데이터가 보인다면?(커밋 전)
-> 데이터 정합성에 문제가 생길 수 있다!

### 자동커밋
쿼리 실행시 자동으로 커밋이됨(쿼리마다 실행 ) -> 우리가 원하는 트랜잭션 기능을 제대로 사용할 수 없다.
````
 set autocommit true; //자동 커밋 모드 설정
insert into member(member_id, money) values ('data1',10000); //자동 커밋 
insert into member(member_id, money) values ('data2',10000); //자동 커밋
````

### 수동커밋
수동커밋으로 설정 -> 트랜잭션을 시작한다고 할 수 있음

````
set autocommit false; //수동 커밋 모드 설정
insert into member(member_id, money) values ('data3',10000);
insert into member(member_id, money) values ('data4',10000); 
commit; //수동 커밋
````

### 확인
h2 session1, 2 각각 띄어놓는다

````
//트랜잭션 시작
set autocommit false; //수동 커밋 모드
insert into member(member_id, money) values ('newId1',10000); 
insert into member(member_id, money) values ('newId2',10000);
````

### DB 락 조회

select for update 구문을 사용

어떨 때 사용?

복잡한 로직을 통해 계산을 수행 중 해당 값이 계산 중간에 변경이 되면 안되는 경우
조회에 락을 걸어 해결 할 수 도 있다.

