package com.vuln.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Vue SPA: 루트·로그인·회원가입·게시판 경로는 index.html로 포워드하여 Vue Router가 처리하도록 함.
 */
@Controller
public class SpaController {

    @GetMapping({ "/", "/login", "/join", "/board", "/board/**" })
    public String index() {
        return "forward:/index.html";
    }
}
