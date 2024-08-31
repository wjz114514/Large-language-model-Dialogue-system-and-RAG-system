package com.example.chatwithai.backend;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class Chat {

    public static void main(String[] args) {
        //写一个scanner
        System.out.println("请输入你的问题:");
        Scanner scanner = new Scanner(System.in);
        String question = scanner.nextLine();

        String requestUrl = "http://localhost:11434/api/generate";
        String requestBody = "{\"model\": \"llama3\", \"prompt\": \"" + question + "\", \"stream\": false}";
        scanner.close();
        try {
            // 创建 URL 对象
            URL url = new URL(requestUrl);

            // 打开连接
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // 设置请求方法为 POST
            connection.setRequestMethod("POST");

            // 设置允许输出
            connection.setDoOutput(true);

            // 设置请求头 Content-Type 为 application/json
            connection.setRequestProperty("Content-Type", "application/json; utf-8");

            // 获取输出流
            OutputStream outputStream = connection.getOutputStream();

            // 写入请求体
            outputStream.write(requestBody.getBytes());
            outputStream.flush();
            outputStream.close();

            // 获取响应码
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();


                // 提取并打印 "response" 字段的内容
                JSONObject jsonObject = new JSONObject(response.toString());
                String Response = jsonObject.getString("response");
                System.out.println("回答是: " + Response);
            } else {
                System.out.println("Failed : HTTP error code : " + responseCode);
            }

            // 关闭连接
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
