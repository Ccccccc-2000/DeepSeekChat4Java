package com.eastcui.deepseekdemo.demos.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eastcui.deepseekdemo.demos.dao.ConversionDao;
import com.eastcui.deepseekdemo.demos.entity.Conversation;
import com.eastcui.deepseekdemo.demos.service.ConversationService;
import org.springframework.stereotype.Service;

/**
 * @Classname ConversationServiceImpl
 * @Description TODO
 * @Date 2025/3/5 9:57
 * @Created by czd
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversionDao, Conversation> implements ConversationService {
}
