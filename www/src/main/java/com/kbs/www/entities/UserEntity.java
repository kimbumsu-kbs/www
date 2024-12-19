package com.kbs.www.entities;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserEntity {
    private String email;
    private String password;
    private String nickName;
    private String contact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private boolean isAdmin;
    private boolean isSuspended;
    private boolean isVerified;
    private int warning;
}
