package com.eastcui.deepseekdemo.demos.web;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eastcui.deepseekdemo.demos.entity.*;
import com.eastcui.deepseekdemo.demos.service.ConversationContentService;
import com.eastcui.deepseekdemo.demos.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@CrossOrigin
public class ChatController {

    private static final Map<String, String> msgs = new HashMap<>();

    @Resource
    private ConversationService conversationService;

    @Resource
    private ConversationContentService conversationContentService;

    @GetMapping(value = "/chat", produces = "text/event-stream")
    @Transactional(rollbackFor = Exception.class)
    public SseEmitter chatNew(String q, String conversionId) {
        if (!StringUtils.hasLength(q)) {
            return null;
        }

        String url = "https://api.deepseek.com/chat/completions";

        DeepSeekParam deepSeekParam = getDeepSeekParam(q, conversionId);

        SseEmitter sseEmitter = new SseEmitter();
        String jsonStr = JSONUtil.toJsonStr(deepSeekParam);
        RequestBody body = RequestBody.create(
                okhttp3.MediaType.parse("application/json"), jsonStr);

        Request request = new Request.Builder().url(url)
                .addHeader("Accept", "text/event-stream")
                .addHeader("Authorization", "Bearer sk-7d13f61afd7b473887f1a307eea599c0")
                .post(body).build();

        String id = saveConversationContent(conversionId, q, "user");

        EventSourceListener listener = getEventSourceListener(sseEmitter, id);

        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS).readTimeout(10, TimeUnit.MINUTES).build();
        EventSource.Factory factory = EventSources.createFactory(client);
        factory.newEventSource(request, listener);
        return sseEmitter;
    }

    private String saveConversationContent(String cid, String q, String role) {
        Conversation conversation = null;
        if (StringUtils.hasLength(cid)) {
            conversation = conversationService.getById(cid);
        }
        Integer conversationId;
        if (conversation == null) {
            Conversation build = Conversation.builder()
                    .title(q.substring(0, Math.min(q.length(), 6)) + "...")
                    .time(DateUtil.now())
                    .build();
            conversationService.save(build);
            conversationId = build.getId();
        } else {
            conversationId = conversation.getId();
        }

        ConversationContent build = ConversationContent.builder()
                .cid(conversationId)
                .role(role)
                .content(q)
                .time(DateUtil.now())
                .build();

        conversationContentService.save(build);
        return conversationId + "";
    }

    @PostMapping("listConversion")
    public R listConversion() {
        return R.ok(conversationService.list());
    }

    @PostMapping("delConversion")
    public R delConversion(String cid) {
        return R.ok(conversationService.removeById(cid));
    }

    @PostMapping("getConversionContent")
    public R getConversionContent(String cid) {
        LambdaQueryWrapper<ConversationContent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationContent::getCid, cid)
                .orderByAsc(ConversationContent::getId);
        List<ConversationContent> list = conversationContentService.list(wrapper);
        return R.ok(list);
    }

    private @NotNull EventSourceListener getEventSourceListener(SseEmitter sseEmitter, String conversationId) {
        // 使用EventSourceListener处理来自服务器的SSE事件
        return new EventSourceListener() {
            @Override
            public void onOpen(@NotNull EventSource eventSource, @NotNull Response response) {
                try {
                    sseEmitter.send("{\"id\":\"" + conversationId + "\"}");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.info("Connection opened.");
            }

            @Override
            public void onClosed(@NotNull EventSource eventSource) {
                log.info("Connection closed.");
                sseEmitter.complete();
            }

            @Override
            public void onEvent(@NotNull EventSource eventSource, @Nullable String id, @Nullable String type, @NotNull String data) {
                try {
                    if ("[DONE]".equals(data)) {
                        String msgt = msgs.get("t" + conversationId);
                        saveConversationContent(conversationId, msgt, "think");
                        String msg = msgs.get(conversationId);
                        saveConversationContent(conversationId, msg, "assistant");
                        msgs.remove(conversationId);
                        msgs.remove("t" + conversationId);
                        sseEmitter.send(data);
                        return;
                    }

                    String content = getContent(data);
                    String reasoningContent = getReasonContent(data);

                    if (StringUtils.hasLength(content)) {
                        JSONObject object = new JSONObject();
                        object.set("content", content);
                        object.set("reasoningContent", "null");
                        System.out.print(content);
                        saveResponse(content, conversationId);
                        sseEmitter.send(object.toString());
                    } else if (StringUtils.hasLength(reasoningContent)) {
                        JSONObject object = new JSONObject();
                        object.set("reasoningContent", reasoningContent);
                        object.set("content", "null");
                        saveResponse(reasoningContent, "t" + conversationId);
                        System.out.print(reasoningContent);
                        sseEmitter.send(object.toString());
                    }
                } catch (Exception e) {
                    log.error("推送数据失败", e);
                }
            }


            @Override
            public void onFailure(@NotNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                log.error("Connection failed.", t);
                assert t != null;
                sseEmitter.completeWithError(t);
            }
        };
    }

    private void saveResponse(String content, String conversationId) {
        String msg = msgs.get(conversationId);
        if (StringUtils.hasLength(msg)) {
            msg += content;
            msgs.put(conversationId, msg);
        } else {
            msgs.put(conversationId, content);
        }
    }

    public String getContent(String data) {
        JSONObject delta = getDelta(data);
        return delta.getStr("content");
    }

    public String getReasonContent(String data) {
        JSONObject delta = getDelta(data);
        return delta.getStr("reasoning_content");
    }

    private JSONObject getDelta(@NotNull String data) {
        String a = data.replace("data: ", "");
        JSONObject js = JSONUtil.parseObj(a);
        JSONArray array = js.getJSONArray("choices");
        JSONObject jsonObject1 = array.getJSONObject(0);
        return jsonObject1.getJSONObject("delta");
    }

    private @NotNull DeepSeekParam getDeepSeekParam(String q, String conversionId) {
        DeepSeekParam deepSeekParam = new DeepSeekParam();
        deepSeekParam.setStream(true);
        deepSeekParam.setModel("deepseek-reasoner");

        List<DeepSeekMsg> messages = getDeepSeekMsgs(q, conversionId);

        deepSeekParam.setMessages(messages);
        return deepSeekParam;
    }

    private @NotNull List<DeepSeekMsg> getDeepSeekMsgs(String q, String conversionId) {
        List<ConversationContent> conversationContents = null;
        if (StringUtils.hasLength(conversionId)) {
            conversationContents = conversationContentService
                    .list(new LambdaQueryWrapper<ConversationContent>()
                            .eq(ConversationContent::getCid, conversionId)
                            .ne(ConversationContent::getRole, "think")
                            .orderByAsc(ConversationContent::getId));
        }

        List<DeepSeekMsg> messages = new ArrayList<>();

        DeepSeekMsg deepSeekMsg2 = new DeepSeekMsg();
        deepSeekMsg2.setRole("system");
        deepSeekMsg2.setContent("你是一个什么都会的 ai，回答问题可以说脏话，但是不是一定要说脏话，模仿人类情绪决定是否要说脏话！并且回答问题要以理服人，结合脏话，不仅仅是要说脏话，背后的道理更重要！");
        messages.add(deepSeekMsg2);

        if (conversationContents != null && !conversationContents.isEmpty()) {
            for (ConversationContent conversationContent : conversationContents) {
                DeepSeekMsg deepSeekMsg1 = new DeepSeekMsg();
                deepSeekMsg1.setRole(conversationContent.getRole());
                deepSeekMsg1.setContent(conversationContent.getContent());
                messages.add(deepSeekMsg1);
            }
        }

        DeepSeekMsg deepSeekMsg = new DeepSeekMsg();
        deepSeekMsg.setRole("user");
        deepSeekMsg.setContent(q);
        messages.add(deepSeekMsg);
        return messages;
    }
}
