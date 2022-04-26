package hello.jdbc.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NoSuchElementException;

import hello.jdbc.connection.ConnectionConst;
import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;

/**
 * JDBC - DriverManager 사용
 */
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

	//아이디 찾기 (조회)
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

	// 업데이트 쿼리
	public void update(String memberId, int money) throws SQLException {
		String sql = "update member set money=? where member_id=?";
		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(2, memberId);
			pstmt.setInt(1, money);
			int resultSize = pstmt.executeUpdate();
			log.info("resultSize={}", resultSize);
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, null);
		}
	}

	//삭제쿼리
	public void delete(String memberId) throws SQLException {
		String sql = "delete from member where member_id=?";

		Connection con = null;
		PreparedStatement pstmt = null;

		try {
			con = getConnection();
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, memberId);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error("db error", e);
			throw e;
		} finally {
			close(con, pstmt, null);
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




	private Connection getConnection() {
		return DBConnectionUtil.getConneciton();
	}

}
