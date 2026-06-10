package com.lucky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.dto.DanmakuSettings;
import com.lucky.entity.ScreenConfig;
import com.lucky.mapper.ScreenConfigMapper;
import com.lucky.service.OssService;
import com.lucky.service.ScreenConfigService;
import com.lucky.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ScreenConfigServiceImpl extends ServiceImpl<ScreenConfigMapper, ScreenConfig>
        implements ScreenConfigService {

    private final OssService ossService;

    @Override
    public ScreenConfig getConfig() {
        ScreenConfig config = getById(1L);
        if (config == null) {
            config = new ScreenConfig();
            config.setId(1L);
        }
        return config;
    }

    @Override
    public ScreenConfig updateBackground(String type, MultipartFile file) {
        ScreenConfig config = getConfig();
        ossService.delete(config.getBackgroundUrl());
        String url;
        try {
            url = ossService.upload(file);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage(), e);
        }
        config.setBackgroundType(type);
        config.setBackgroundUrl(url);
        saveOrUpdate(config);
        return config;
    }

    @Override
    public void clearBackground() {
        ScreenConfig config = getConfig();
        ossService.delete(config.getBackgroundUrl());
        config.setBackgroundType(null);
        config.setBackgroundUrl(null);
        saveOrUpdate(config);
    }

    @Override
    public ScreenConfig updateMobileBackground(String type, MultipartFile file) {
        ScreenConfig config = getConfig();
        ossService.delete(config.getMobileBackgroundUrl());
        String url;
        try {
            url = ossService.upload(file);
        } catch (Exception e) {
            throw new BusinessException("文件上传失败: " + e.getMessage(), e);
        }
        config.setMobileBackgroundType(type);
        config.setMobileBackgroundUrl(url);
        saveOrUpdate(config);
        return config;
    }

    @Override
    public void clearMobileBackground() {
        ScreenConfig config = getConfig();
        ossService.delete(config.getMobileBackgroundUrl());
        config.setMobileBackgroundType(null);
        config.setMobileBackgroundUrl(null);
        saveOrUpdate(config);
    }

    @Override
    public void updateDanmakuSettings(DanmakuSettings settings) {
        ScreenConfig config = getConfig();
        config.setDanmakuArea(settings.getArea());
        config.setDanmakuOpacity(settings.getOpacity());
        config.setDanmakuFontSize(settings.getFontSize());
        config.setDanmakuSpeed(settings.getSpeed());
        saveOrUpdate(config);
    }
}