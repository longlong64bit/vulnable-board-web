package com.vuln.board.repository;

import com.vuln.board.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 취약점: SQL Injection - 사용자 입력을 쿼리에 문자열 연결로 삽입
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUserId(rs.getString("user_id"));
        u.setPassword(rs.getString("password"));
        u.setName(rs.getString("name"));
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    };

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** SQL Injection 취약: user_id로 로그인 시 쿼리 조작 가능 */
    public User findByUserId(String userId) {
        String sql = "SELECT * FROM users WHERE user_id = '" + userId + "'";
        List<User> list = jdbc.query(sql, ROW_MAPPER);
        return list.isEmpty() ? null : list.get(0);
    }

    /** SQL Injection 취약: 검색/목록 등 */
    public List<User> findByCondition(String condition) {
        String sql = "SELECT * FROM users WHERE " + condition;
        return jdbc.query(sql, ROW_MAPPER);
    }

    public int insert(User user) {
        String sql = "INSERT INTO users (user_id, password, name) VALUES ('"
                + user.getUserId().replace("'", "''") + "', '"
                + user.getPassword() + "', '"
                + (user.getName() != null ? user.getName().replace("'", "''") : "") + "')";
        return jdbc.update(sql);
    }
}
