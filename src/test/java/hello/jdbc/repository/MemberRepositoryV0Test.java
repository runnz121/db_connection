package hello.jdbc.repository;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

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