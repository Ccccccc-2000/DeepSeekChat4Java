package com.eastcui.deepseekdemo.demos.entity;

import lombok.Data;

import java.util.List;

/**
 * @Classname DeepSeekParam
 * @Description TODO
 * @Date 2025/3/4 16:06
 * @Created by czd
 */
@Data
public class DeepSeekParam {
    private String model;
    private List<DeepSeekMsg> messages;
    private Boolean stream;
}
