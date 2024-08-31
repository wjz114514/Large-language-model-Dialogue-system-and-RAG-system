package com.example.chatwithai.controller;

import com.example.chatwithai.POJO.Result;
import com.example.chatwithai.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/chat", produces = "application/json; charset=UTF-8")
    public DeferredResult<String> chat(@RequestBody Result result) {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        // 异步生成回答，并提供回调处理回答或错误
        chatService.generateAnswerAsync(result, answer -> {
            try {
                // 成功时设置回答
                deferredResult.setResult(answer);
            } catch (Exception e) {
                // 异常处理：打印堆栈跟踪并设置错误信息
                e.printStackTrace();
                deferredResult.setErrorResult("An error occurred while processing your request.");
            }
        });
        // 返回 DeferredResult 对象
        return deferredResult;
    }
}