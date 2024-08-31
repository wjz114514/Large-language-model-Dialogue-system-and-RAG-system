package com.example.chatwithai.service;

import com.example.chatwithai.POJO.Result;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

@Service
public class ChatService {
    // 用于确保generateAnswer方法在同一时刻只被一个线程访问
    private final ReentrantLock lock = new ReentrantLock();

    // 自动装配一个指定的线程池任务执行器，用于执行异步任务
    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    public void generateAnswerAsync(Result result, Consumer<String> callback) {
        taskExecutor.execute(() -> {
            try {
                String answer = generateAnswer(result);
                callback.accept(answer);
            } catch (Exception e) {
                callback.accept("Error: " + e.getMessage());
            }
        });
    }


    public String generateAnswer(Result result) throws Exception {
        lock.lock(); // 获取锁

        try {
            // 从结果对象中获取问题
            String question = result.getQuestion();
            System.out.println("问题是: " + question);

            // 准备请求URL和请求体
            String requestUrl = "http://localhost:11434/api/generate";
            String requestBody = "{\"model\": \"llama3\", \"prompt\": \"" + question + "\", \"stream\": false}";

            // 发起HTTP连接
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // 发送请求
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.getBytes());
                outputStream.flush();
            }

            // 获取并处理响应
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    // 解析JSON响应并返回答案
                    JSONObject jsonObject = new JSONObject(response.toString());
                    String answer = jsonObject.getString("response");
                    System.out.println("回答是: " + answer);
                    return "llama3:" + answer;
                }
            } else {
                throw new RuntimeException("Failed : HTTP error code : " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate answer", e);
        } finally {
            lock.unlock(); // 无论是否出现异常都会释放锁
        }
    }
}

