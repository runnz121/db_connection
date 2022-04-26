package hello.jdbc.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data //equalsAndHashcode를 @data가 같이 구현해준다.
public class Member {

	private String memberId;
	private int money;

	public Member() {

	}

	public Member(String memberId, int money) {
		this.memberId = memberId;
		this.money = money;
	}

}
