package com.lucky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lucky.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService extends IService<SensitiveWord> {

    /**
     * 检查内容是否包含敏感词
     * @return 如果包含敏感词返回 true
     */
    boolean containsSensitiveWord(String content);

    /**
     * 获取所有敏感词
     */
    List<String> getAllWords();

    /**
     * 替换内容中的敏感词为 ***
     */
    String filterContent(String content);
}
