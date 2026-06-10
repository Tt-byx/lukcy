package com.lucky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DanmakuSettings {
    
    private String area;
    
    private Integer opacity;
    
    private Integer fontSize;
    
    private Integer speed;
}