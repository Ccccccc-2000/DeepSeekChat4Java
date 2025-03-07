package com.eastcui.deepseekdemo.demos.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;

/**
 * @Classname ConversationContent
 * @Description TODO
 * @Date 2025/3/5 9:53
 * @Created by czd
 */
@Data
@Builder
public class ConversationContent {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer cid;
    private String role;
    private String content;
    private String time;
}
