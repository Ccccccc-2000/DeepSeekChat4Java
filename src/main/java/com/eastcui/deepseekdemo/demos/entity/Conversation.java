package com.eastcui.deepseekdemo.demos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

/**
 * @Classname Conversation
 * @Description TODO
 * @Date 2025/3/5 9:52
 * @Created by czd
 */
@Data
@Builder
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String time;
}
