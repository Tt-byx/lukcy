package com.lucky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lucky.dto.DanmakuSettings;
import com.lucky.entity.ScreenConfig;
import org.springframework.web.multipart.MultipartFile;

public interface ScreenConfigService extends IService<ScreenConfig> {

    ScreenConfig getConfig();

    ScreenConfig updateBackground(String type, MultipartFile file);

    void clearBackground();

    ScreenConfig updateMobileBackground(String type, MultipartFile file);

    void clearMobileBackground();

    void updateDanmakuSettings(DanmakuSettings settings);
}
