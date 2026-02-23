package com.vuln.board.controller;

import com.vuln.board.entity.User;
import com.vuln.board.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * 취약점: SQL Injection(로그인), XSS(이름), CSRF 미적용, 약한 비밀번호 저장
 */
@Controller
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /** SQL Injection: user_id에 ' OR '1'='1 등 입력 시 인증 우회 가능 */
    @PostMapping("/login")
    public String login(
            @RequestParam String userId,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes ra) {
        User user = userRepository.findByUserId(userId);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getName());
            return "redirect:/board/list";
        }
        ra.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    /** 취약: 비밀번호 평문 저장, SQL Injection(insert는 일부 이스케이프만) */
    @PostMapping("/join")
    public String join(
            @RequestParam String userId,
            @RequestParam String password,
            @RequestParam(required = false) String name,
            RedirectAttributes ra) {
        if (userRepository.findByUserId(userId) != null) {
            ra.addFlashAttribute("error", "이미 존재하는 아이디입니다.");
            return "redirect:/join";
        }
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        user.setName(name != null ? name : userId);
        userRepository.insert(user);
        ra.addFlashAttribute("message", "회원가입이 완료되었습니다. 로그인하세요.");
        return "redirect:/login";
    }
}
