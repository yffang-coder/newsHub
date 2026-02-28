package com.newshub.backend.interfaces.dto;

import lombok.Data;
import java.util.List;

@Data
public class NotificationRequest {
    private Long userId;
    private List<Long> userIds;
    private String title;
    private String content;
    private String type;
    private Boolean isGlobal;
}
