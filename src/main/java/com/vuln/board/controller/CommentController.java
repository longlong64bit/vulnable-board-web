package com.vuln.board.controller;

import com.vuln.board.entity.Comment;
import com.vuln.board.repository.CommentRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 댓글 AJAX API. 취약점: XSS(댓글 내용 그대로 반환/저장), 인증 검증 부재
 */
@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentRepository commentRepository;

    public CommentController(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> list(@RequestParam Long boardId, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        List<Comment> comments = commentRepository.findByBoardId(boardId);
        List<Map<String, Object>> result = comments.stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("boardId", c.getBoardId());
            m.put("writerId", c.getWriterId());
            m.put("content", c.getContent());
            m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /** Stored XSS: content를 검증/이스케이프 없이 저장 후 클라이언트에 그대로 전달 */
    @PostMapping(value = "/add", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> add(
            @RequestParam Long boardId,
            @RequestParam String content,
            HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        Comment comment = new Comment();
        comment.setBoardId(boardId);
        comment.setWriterId((String) session.getAttribute("userId"));
        comment.setContent(content);
        Long id = commentRepository.insert(comment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("id", id);
        result.put("content", content);
        result.put("writerId", comment.getWriterId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        commentRepository.deleteById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return ResponseEntity.ok(result);
    }
}
