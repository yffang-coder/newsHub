package com.newshub.backend.interfaces.dto;

import lombok.Data;

@Data
public class LoginByCodeRequest {
    private String email;
    private String code;
}
