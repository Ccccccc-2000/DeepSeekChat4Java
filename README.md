# 深度求索（DeepSeek）智能对话 SpringBoot 集成方案
本项目基于SpringBoot实现与DeepSeek大模型的对话交互，适用于需要智能对话功能的Web应用。

## 主要特性
- 5分钟快速集成
- 支持流式响应（像ChatGPT那样一个字一个字往外蹦）

## 技术栈
- Java 1.8+ 
- SpringBoot 2.x 
- Lombok 
- SseEmitter 

## 快速开始
### 1. 先配置密钥
# application.yml
deepseek:
  api-key: 你的密钥 # 去官网注册
  model: deepseek-reasoner
  base-url: https://api.deepseek.com # 可选：默认为官方 API 地址
  log-requests: true # 可选：是否记录请求日志
  log-responses: true # 可选：是否记录响应日志
  log-level: INFO
