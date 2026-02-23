package com.vuln.board.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Board {
    private Long id;
    private String title;
    private String content;
    private String writerId;
    private LocalDateTime createdAt;
    private List<Comment> comments;
    private List<Attachment> attachments;
}
