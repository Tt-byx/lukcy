package com.lucky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lucky.entity.SensitiveWord;
import com.lucky.mapper.SensitiveWordMapper;
import com.lucky.service.SensitiveWordService;
import com.lucky.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord>
        implements SensitiveWordService {

    private final RedisUtil redisUtil;

    /**
     * Redis key前缀
     */
    private static final String SENSITIVE_WORDS_KEY = "lucky:sensitive:words";

    /**
     * 获取敏感词列表（带Redis缓存）
     */
    @Override
    public List<String> getAllWords() {
        // 先从Redis获取
        Set<Object> cachedWords = redisUtil.setMembers(SENSITIVE_WORDS_KEY);
        if (cachedWords != null && !cachedWords.isEmpty()) {
            return cachedWords.stream()
                    .map(Object::toString)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
        }

        // Redis中没有，从数据库加载
        synchronized (this) {
            // 双重检查
            cachedWords = redisUtil.setMembers(SENSITIVE_WORDS_KEY);
            if (cachedWords != null && !cachedWords.isEmpty()) {
                return cachedWords.stream()
                        .map(Object::toString)
                        .map(String::toLowerCase)
                        .collect(Collectors.toList());
            }

            // 从数据库加载
            List<String> words = list().stream()
                    .map(SensitiveWord::getWord)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            // 存入Redis，设置过期时间1小时
            if (!words.isEmpty()) {
                for (String word : words) {
                    redisUtil.setAdd(SENSITIVE_WORDS_KEY, word);
                }
                redisUtil.expire(SENSITIVE_WORDS_KEY, 1, TimeUnit.HOURS);
            }

            log.info("敏感词缓存已加载到Redis，共 {} 个词", words.size());
            return words;
        }
    }

    /**
     * 清除缓存（增删改时调用）
     */
    public void clearCache() {
        redisUtil.delete(SENSITIVE_WORDS_KEY);
        log.info("敏感词Redis缓存已清除");
    }

    @Override
    public boolean containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String lowerContent = content.toLowerCase();
        for (String word : getAllWords()) {
            if (lowerContent.contains(word)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String filterContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        String result = content;
        for (String word : getAllWords()) {
            // 替换所有匹配的敏感词（不区分大小写）
            result = result.replaceAll("(?i)" + word, "***");
        }
        return result;
    }

    /**
     * 添加敏感词（重写以清除缓存）
     */
    @Override
    public boolean save(SensitiveWord entity) {
        boolean result = super.save(entity);
        if (result) {
            clearCache();
        }
        return result;
    }

    /**
     * 删除敏感词（重写以清除缓存）
     */
    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            clearCache();
        }
        return result;
    }

    /**
     * 批量删除敏感词（重写以清除缓存）
     */
    @Override
    public boolean removeByIds(java.util.Collection<?> idList) {
        boolean result = super.removeByIds(idList);
        if (result) {
            clearCache();
        }
        return result;
    }
}
