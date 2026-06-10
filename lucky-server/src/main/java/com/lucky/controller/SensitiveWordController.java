package com.lucky.controller;

import com.lucky.dto.Result;
import com.lucky.entity.SensitiveWord;
import com.lucky.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensitive-word")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @GetMapping("/list")
    public Result<List<SensitiveWord>> list() {
        return Result.ok(sensitiveWordService.list());
    }

    @PostMapping
    public Result<Void> add(@RequestParam String word) {
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sensitiveWordService.save(sw);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.removeById(id);
        return Result.ok();
    }

    @PostMapping("/batch")
    public Result<Void> batchAdd(@RequestBody List<String> words) {
        for (String word : words) {
            SensitiveWord sw = new SensitiveWord();
            sw.setWord(word);
            sensitiveWordService.save(sw);
        }
        return Result.ok();
    }
}
