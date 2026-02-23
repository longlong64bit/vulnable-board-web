package com.vuln.board.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String userId;
    private String password;
    private String name;
    private java.time.LocalDateTime createdAt;
}
