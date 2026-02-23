package com.vuln.board.repository;

import com.vuln.board.entity.Board;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * 취약점: SQL Injection - 검색/정렬 등에 사용자 입력 문자열 연결
 */
@Repository
public class BoardRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Board> ROW_MAPPER = (rs, rowNum) -> {
        Board b = new Board();
        b.setId(rs.getLong("id"));
        b.setTitle(rs.getString("title"));
        b.setContent(rs.getString("content"));
        b.setWriterId(rs.getString("writer_id"));
        if (rs.getTimestamp("created_at") != null) {
            b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return b;
    };

    public BoardRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** SQL Injection 취약: orderBy, keyword 등 조작 가능 */
    public List<Board> findAll(String orderBy, String keyword) {
        String sql = "SELECT * FROM board ";
        if (keyword != null && !keyword.isEmpty()) {
            sql += "WHERE title LIKE '%" + keyword + "%' OR content LIKE '%" + keyword + "%' ";
        }
        sql += "ORDER BY " + (orderBy != null && !orderBy.isEmpty() ? orderBy : "id DESC");
        return jdbc.query(sql, ROW_MAPPER);
    }

    public Board findById(Long id) {
        String sql = "SELECT * FROM board WHERE id = " + id;
        List<Board> list = jdbc.query(sql, ROW_MAPPER);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Board board) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO board (title, content, writer_id) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, board.getTitle());
            ps.setString(2, board.getContent());
            ps.setString(3, board.getWriterId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int update(Board board) {
        return jdbc.update("UPDATE board SET title = ?, content = ? WHERE id = ?",
                board.getTitle(), board.getContent(), board.getId());
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM board WHERE id = ?", id);
    }
}
