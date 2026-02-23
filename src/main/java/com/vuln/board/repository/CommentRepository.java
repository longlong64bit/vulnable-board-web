package com.vuln.board.repository;

import com.vuln.board.entity.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
public class CommentRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Comment> ROW_MAPPER = (rs, rowNum) -> {
        Comment c = new Comment();
        c.setId(rs.getLong("id"));
        c.setBoardId(rs.getLong("board_id"));
        c.setWriterId(rs.getString("writer_id"));
        c.setContent(rs.getString("content"));
        if (rs.getTimestamp("created_at") != null) {
            c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return c;
    };

    public CommentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Comment> findByBoardId(Long boardId) {
        return jdbc.query("SELECT * FROM comment WHERE board_id = ? ORDER BY id ASC", ROW_MAPPER, boardId);
    }

    public Long insert(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO comment (board_id, writer_id, content) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, comment.getBoardId());
            ps.setString(2, comment.getWriterId());
            ps.setString(3, comment.getContent());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM comment WHERE id = ?", id);
    }
}
