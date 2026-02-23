package com.vuln.board.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Comment {
    private Long id;
    private Long boardId;
    private String writerId;
    private String content;
    private LocalDateTime createdAt;
}
