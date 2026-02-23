package com.vuln.board.controller;

import com.vuln.board.entity.Board;
import com.vuln.board.repository.AttachmentRepository;
import com.vuln.board.repository.BoardRepository;
import com.vuln.board.repository.CommentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * 취약점: SQL Injection(검색/정렬), XSS(제목/내용 그대로 출력), 인증/권한 검증 부재
 */
@Controller
@RequestMapping("/board")
public class BoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;

    public BoardController(BoardRepository boardRepository,
                           CommentRepository commentRepository,
                           AttachmentRepository attachmentRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.attachmentRepository = attachmentRepository;
    }

    @PostMapping("/write")
    public String write(
            @RequestParam String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Board board = new Board();
        board.setTitle(title);
        board.setContent(content);
        board.setWriterId((String) session.getAttribute("userId"));
        Long id = boardRepository.insert(board);
        ra.addFlashAttribute("message", "글이 등록되었습니다.");
        return "redirect:/board/view/" + id;
    }

    @PostMapping("/edit/{id}")
    public String edit(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            HttpSession session,
            RedirectAttributes ra) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        Board board = boardRepository.findById(id);
        if (board == null) {
            return "redirect:/board/list";
        }
        board.setTitle(title);
        board.setContent(content);
        boardRepository.update(board);
        ra.addFlashAttribute("message", "수정되었습니다.");
        return "redirect:/board/view/" + id;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login";
        }
        commentRepository.findByBoardId(id).forEach(c -> commentRepository.deleteById(c.getId()));
        attachmentRepository.deleteByBoardId(id);
        boardRepository.deleteById(id);
        ra.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/board/list";
    }
}
