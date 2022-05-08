package hello.jdbc.repository;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.zaxxer.hikari.HikariDataSource;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class MemberRepositoryV1Test {

	MemberRepositoryV1 memberRepositoryV1;

	// /**
	//  * driver manager 사용시
	//  */
	// @BeforeEach
	// void beforeEach() {
	// 	//기본 DriverManager - 항상 새로운 커넥션을 획득
	// 	DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
	// 	memberRepositoryV1 = new MemberRepositoryV1(dataSource);
	// }

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


	@DisplayName("crud 테스트")
	@Test
	void crud() throws SQLException, InterruptedException {

		//save
		Member member = new Member("memberV6", 10000);
		memberRepositoryV1.save(member);

		//findById
		Member findMemeber = memberRepositoryV1.findById(member.getMemberId());

		//@Data //equalsAndHashcode를 @data가 같이 구현해준다.
		log.info("findMember = {}", findMemeber);
		log.info("member != findMember {}", member == findMemeber);
		log.info("member equals findMember {}", member.equals(findMemeber));
		assertThat(findMemeber).isEqualTo(member);

		//update: money :10000 -> 20000
		memberRepositoryV1.update(member.getMemberId(), 20000);
		Member updateMember = memberRepositoryV1.findById(member.getMemberId());
		//assertThat(updateMember).isEqualTo(member);
		assertThat(updateMember.getMoney()).isEqualTo(20000); //이걸로 해야함

		//delete -> assertThatThrowBy -> 예외를 던지는 assert -> 예외를 던져야 테스트가 성공 -> 예외가 남으로 반복적으로 실행이 가능!!
		//하지만 중간에 다른예외로 익셉션을 터트리면 하위 메소드가 호출이 되지 않는다 .
		memberRepositoryV1.delete(member.getMemberId());
		assertThatThrownBy(() -> memberRepositoryV1.findById(member.getMemberId())).isInstanceOf(NoSuchElementException.class);

		//이미 삭제된 멤버는 확인할 수 없음으로
		//Member deletedMember = memberRepositoryV0.findById("memberV6");

		Thread.sleep(1000);

	}
}