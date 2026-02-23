package com.vuln.board.controller;

import com.vuln.board.entity.Attachment;
import com.vuln.board.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 취약점: 파일 업로드(확장자/타입 검증 없음, 웹쉘 업로드 가능),
 * 파일 다운로드(Path Traversal - stored_name 또는 original_name 조작),
 * IDOR(다른 게시글 첨부파일 다운로드 가능)
 */
@Controller
@RequestMapping("/file")
public class FileController {

    private final AttachmentRepository attachmentRepository;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    public FileController(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    /** 취약: 확장자/MIME/크기 제한 없음, 원본 파일명 그대로 저장 가능 → Path Traversal */
    @PostMapping("/upload")
    @ResponseBody
    public ResponseEntity<?> upload(
            @RequestParam Long boardId,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어 있습니다.");
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                originalName = "unnamed";
            }
            String storedName = UUID.randomUUID().toString() + "_" + originalName;
            Path target = dir.resolve(storedName);
            file.transferTo(target.toFile());

            Attachment att = new Attachment();
            att.setBoardId(boardId);
            att.setOriginalName(originalName);
            att.setStoredName(storedName);
            attachmentRepository.insert(att);
            return ResponseEntity.ok().body(java.util.Map.of("success", true, "storedName", storedName, "originalName", originalName));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * 취약: Path Traversal - id 대신 stored_name 또는 original_name을 조작해
     * 서버 내 임의 파일 다운로드 가능하도록 할 수 있음.
     * 여기서는 id로 DB 조회 후 stored_name 사용하지만, Content-Disposition에
     * original_name을 그대로 넣어 클라이언트에서 파일명 조작 가능.
     * IDOR: 다른 사용자 게시글의 첨부 id만 알면 다운로드 가능 (권한 미검증)
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        Attachment att = attachmentRepository.findById(id);
        if (att == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = dir.resolve(att.getStoredName()).normalize();
            if (!filePath.startsWith(dir)) {
                return ResponseEntity.badRequest().build();
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            String filename = att.getOriginalName();
            if (filename == null) {
                filename = "download";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 취약: Path Traversal - name 파라미터에 ../ 등으로 서버 내 임의 파일 노출
     * 예: /file/get?name=../../../etc/passwd
     */
    @GetMapping("/get")
    public ResponseEntity<Resource> getFile(@RequestParam String name, HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = dir.resolve(name).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
