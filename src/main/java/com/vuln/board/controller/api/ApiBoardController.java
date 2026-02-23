package com.vuln.board.controller.api;

import com.vuln.board.entity.Board;
import com.vuln.board.repository.AttachmentRepository;
import com.vuln.board.repository.BoardRepository;
import com.vuln.board.repository.CommentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/board")
public class ApiBoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    public ApiBoardController(BoardRepository boardRepository,
                              CommentRepository commentRepository,
                              AttachmentRepository attachmentRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
    }

    private boolean notLoggedIn(HttpSession session) {
        return session.getAttribute("userId") == null;
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderBy,
            HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        List<Board> list = boardRepository.findAll(orderBy, keyword);
        List<Map<String, Object>> items = list.stream().map(b -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("writerId", b.getWriterId());
            m.put("createdAt", b.getCreatedAt() != null ? b.getCreatedAt().toString() : null);
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("list", items));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        Board board = boardRepository.findById(id);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }
        board.setComments(commentRepository.findByBoardId(id));
        board.setAttachments(attachmentRepository.findByBoardId(id));
        Map<String, Object> m = new HashMap<>();
        m.put("id", board.getId());
        m.put("title", board.getTitle());
        m.put("content", board.getContent());
        m.put("writerId", board.getWriterId());
        m.put("createdAt", board.getCreatedAt() != null ? board.getCreatedAt().toString() : null);
        m.put("comments", board.getComments());
        m.put("attachments", board.getAttachments());
        return ResponseEntity.ok(m);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title == null || title.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "제목을 입력하세요."));
        }
        Board board = new Board();
        board.setTitle(title);
        board.setContent(content != null ? content : "");
        board.setWriterId((String) session.getAttribute("userId"));
        Long id = boardRepository.insert(board);
        return ResponseEntity.ok(Map.of("id", id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body, HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        Board board = boardRepository.findById(id);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }
        String title = body.get("title");
        String content = body.get("content");
        if (title != null) board.setTitle(title);
        if (content != null) board.setContent(content);
        boardRepository.update(board);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, HttpSession session) {
        if (notLoggedIn(session)) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 필요"));
        }
        Board board = boardRepository.findById(id);
        if (board == null) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.findByBoardId(id).forEach(c -> commentRepository.deleteById(c.getId()));
        attachmentRepository.deleteByBoardId(id);
        boardRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
