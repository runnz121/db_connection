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