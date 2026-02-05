package com.newshub.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Settings {
    private String key;
    private String value;
    private LocalDateTime updatedAt;
}
