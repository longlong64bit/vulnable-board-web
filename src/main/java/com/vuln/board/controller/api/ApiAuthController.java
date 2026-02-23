package com.vuln.board.controller.api;

import com.vuln.board.entity.User;
import com.vuln.board.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {

    private final UserRepository userRepository;

    public ApiAuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String userId = body.get("userId");
        String password = body.get("password");
        if (userId == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "아이디와 비밀번호를 입력하세요."));
        }
        User user = userRepository.findByUserId(userId);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userName", user.getName());
            return ResponseEntity.ok(Map.of("success", true, "userName", user.getName() != null ? user.getName() : user.getUserId()));
        }
        return ResponseEntity.ok(Map.of("success", false, "message", "아이디 또는 비밀번호가 올바르지 않습니다."));
    }

    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String password = body.get("password");
        String name = body.get("name");
        if (userId == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "아이디와 비밀번호를 입력하세요."));
        }
        if (userRepository.findByUserId(userId) != null) {
            return ResponseEntity.ok(Map.of("success", false, "message", "이미 존재하는 아이디입니다."));
        }
        User user = new User();
        user.setUserId(userId);
        user.setPassword(password);
        user.setName(name != null ? name : userId);
        userRepository.insert(user);
        return ResponseEntity.ok(Map.of("success", true, "message", "회원가입이 완료되었습니다. 로그인하세요."));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "userName", session.getAttribute("userName") != null ? session.getAttribute("userName").toString() : userId
        ));
    }
}
