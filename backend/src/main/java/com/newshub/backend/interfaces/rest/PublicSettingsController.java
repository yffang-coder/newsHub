package com.newshub.backend.interfaces.rest;

import com.newshub.backend.domain.model.Settings;
import com.newshub.backend.infrastructure.persistence.SettingsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicSettingsController {

    @Autowired
    private SettingsMapper settingsMapper;

    @GetMapping(value = "/settings", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, String>> getSettings() {
        List<Settings> settingsList = settingsMapper.findAll();
        Map<String, String> settingsMap = new HashMap<>();
        for (Settings setting : settingsList) {
            settingsMap.put(setting.getKey(), setting.getValue());
        }
        return ResponseEntity.ok(settingsMap);
    }

    @GetMapping(value = "/settings/{key}", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> getSetting(@PathVariable String key) {
        Settings setting = settingsMapper.findByKey(key);
        if (setting != null) {
            return ResponseEntity.ok(setting.getValue());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
