package com.vuln.board.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 전자금융기반시설 보안취약점 평가기준(웹/모바일/HTS) 대비 추가 취약 엔드포인트.
 * 내부 점검용으로만 사용할 것.
 */
@Controller
public class VulnExtraController {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /**
     * 디렉토리 리스팅 (설정 오류 시뮬레이션).
     * /uploads/ 접근 시 인덱스 파일 없으면 하위 목록 출력.
     */
    @GetMapping(value = { "/uploads", "/uploads/" })
    public ResponseEntity<String> uploadsDirectoryListing() {
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(dir) || !Files.isDirectory(dir)) {
                return ResponseEntity.notFound().build();
            }
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Index of /uploads/</title></head><body>");
            html.append("<h1>Index of /uploads/</h1><hr><pre>");
            try (Stream<Path> stream = Files.list(dir)) {
                stream.sorted(Comparator.comparing(p -> p.getFileName().toString().toLowerCase()))
                        .forEach(p -> {
                            String name = p.getFileName().toString();
                            if (Files.isDirectory(p)) {
                                html.append("<a href=\"/uploads/").append(name).append("/\">").append(name).append("/</a>\n");
                            } else {
                                html.append("<a href=\"/uploads/").append(name).append("\">").append(name).append("</a>\n");
                            }
                        });
            }
            html.append("</pre><hr></body></html>");
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    // ---------- 관리자 페이지 노출 (평가항목: 관리자 페이지 노출 여부) ----------
    @GetMapping("/admin")
    public String admin(Model model, HttpSession session) {
        model.addAttribute("message", "관리자 대시보드 (인증 없이 접근 가능 - 취약)");
        model.addAttribute("serverInfo", System.getProperty("os.name") + " / " + System.getProperty("java.version"));
        return "admin";
    }

    // ---------- 리다이렉트를 이용한 피싱 (평가항목: 리다이렉트 기능을 이용한 피싱 공격) ----------
    @GetMapping("/redirect")
    public RedirectView openRedirect(@RequestParam(required = false) String url) {
        if (url != null && !url.isEmpty()) {
            return new RedirectView(url);
        }
        return new RedirectView("/");
    }

    // ---------- SSRF (평가항목: 서버 사이드 요청 위조) ----------
    @GetMapping(value = "/fetch", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String ssrf(@RequestParam(required = false) String url, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "로그인 필요";
        }
        if (url == null || url.isEmpty()) {
            return "usage: /fetch?url=http://example.com";
        }
        try {
            URI uri = new URL(url).toURI();
            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(uri.toURL().openStream(), StandardCharsets.UTF_8))) {
                return r.lines().limit(50).collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ---------- 운영체제 명령실행 (평가항목: 운영체제 명령실행) ----------
    @GetMapping(value = "/cmd", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String commandInjection(@RequestParam(required = false) String exec, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "로그인 필요";
        }
        if (exec == null || exec.isEmpty()) {
            return "usage: /cmd?exec=whoami (취약: 명령어 치환 가능)";
        }
        try {
            Process p = Runtime.getRuntime().exec(exec);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String out = r.lines().collect(Collectors.joining("\n"));
            p.waitFor();
            return out.isEmpty() ? "(no output)" : out;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ---------- 디렉토리 목록 노출 (평가항목: 디렉토리 목록 노출) ----------
    @GetMapping("/dir")
    @ResponseBody
    public String directoryListing(@RequestParam(required = false) String path, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "로그인 필요";
        }
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = path != null && !path.isEmpty()
                ? base.resolve(path).normalize()
                : base;
        if (!target.startsWith(base)) {
            return "Access denied";
        }
        try {
            if (!Files.exists(target) || !Files.isDirectory(target)) {
                return "Not a directory or not found: " + target;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Directory listing: ").append(target).append("\n\n");
            Files.list(target).forEach(p -> sb.append(p.getFileName()).append("\n"));
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // ---------- 시스템 운영정보 노출 (평가항목: 시스템 운영정보 노출 여부) ----------
    @GetMapping("/debug")
    @ResponseBody
    public String debugInfo(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "로그인 필요";
        }
        return "Java: " + System.getProperty("java.version")
                + "\nOS: " + System.getProperty("os.name")
                + "\nUser: " + System.getProperty("user.name")
                + "\nUpload dir: " + uploadDir;
    }

    // ---------- SSTI (평가항목: 서버 사이드 템플릿 인젝션) - 단순 치환으로 취약점 시뮬레이션 ----------
    @GetMapping("/ssti")
    public String ssti(@RequestParam(required = false) String name, Model model) {
        if (name == null) {
            name = "guest";
        }
        model.addAttribute("name", name);
        return "ssti";
    }

    // ---------- XXE (평가항목: XML 외부객체 공격) ----------
    @PostMapping(value = "/xml", consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.TEXT_XML_VALUE, "application/xml"}, produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String xxe(@RequestBody(required = false) String body, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "로그인 필요";
        }
        if (body == null || body.isEmpty()) {
            return "POST XML body required";
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(body)));
            return "Parsed root: " + (doc.getDocumentElement() != null ? doc.getDocumentElement().getNodeName() : "null");
        } catch (Exception e) {
            return "Parse error: " + e.getMessage();
        }
    }

    // ---------- RSC API 안내 (React Server Components 스트림 API — GET 시 안내 페이지로) ----------
    @GetMapping("/rsc")
    public String rscApiPage(Model model) {
        return "rsc";
    }

    @GetMapping("/api/rsc/render")
    public RedirectView rscRenderRedirect() {
        return new RedirectView("/rsc");
    }

    @GetMapping("/api/rsc/replay")
    public RedirectView rscReplayRedirect() {
        return new RedirectView("/rsc");
    }
}
