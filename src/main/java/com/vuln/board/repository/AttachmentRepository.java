package com.vuln.board.repository;

import com.vuln.board.entity.Attachment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;

@Repository
public class AttachmentRepository {

    private final JdbcTemplate jdbc;

    private static final RowMapper<Attachment> ROW_MAPPER = (rs, rowNum) -> {
        Attachment a = new Attachment();
        a.setId(rs.getLong("id"));
        a.setBoardId(rs.getLong("board_id"));
        a.setOriginalName(rs.getString("original_name"));
        a.setStoredName(rs.getString("stored_name"));
        if (rs.getTimestamp("created_at") != null) {
            a.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return a;
    };

    public AttachmentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Attachment> findByBoardId(Long boardId) {
        return jdbc.query("SELECT * FROM attachment WHERE board_id = ?", ROW_MAPPER, boardId);
    }

    public Attachment findById(Long id) {
        List<Attachment> list = jdbc.query("SELECT * FROM attachment WHERE id = ?", ROW_MAPPER, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long insert(Attachment att) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            var ps = con.prepareStatement(
                    "INSERT INTO attachment (board_id, original_name, stored_name) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, att.getBoardId());
            ps.setString(2, att.getOriginalName());
            ps.setString(3, att.getStoredName());
            return ps;
        }, keyHolder);
        return keyHolder.getKey() != null ? keyHolder.getKey().longValue() : null;
    }

    public int deleteByBoardId(Long boardId) {
        return jdbc.update("DELETE FROM attachment WHERE board_id = ?", boardId);
    }
}
