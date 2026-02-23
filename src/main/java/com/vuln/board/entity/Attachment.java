package com.vuln.board.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Attachment {
    private Long id;
    private Long boardId;
    private String originalName;
    private String storedName;
    private LocalDateTime createdAt;
}
