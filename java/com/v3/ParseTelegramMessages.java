package com.v3;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ParseTelegramMessages {

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Administrator\\Downloads\\Telegram Desktop\\ChatExport_2026-02-24\\result.json"; // 你可以改为从命令行参数读取

        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            JSONObject root = new JSONObject(content);
            JSONArray messages = root.getJSONArray("messages");

            for (int i = 0; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);

                // 只处理 type = "message"
                if (!"message".equals(msg.optString("type"))) {
                    continue;
                }

                System.out.println("=== 记录 " + (i + 1) + " ===");

                // 读取 photo 字段
                if (msg.has("photo")) {
                    System.out.println("photo: " + msg.getString("photo"));
                } else {
                    System.out.println("photo: 无");
                }

                // 读取 text_entities 数组
                JSONArray textEntities = msg.optJSONArray("text_entities");
                if (textEntities != null && !textEntities.isEmpty()) {
                    StringBuilder plainText = new StringBuilder();
                    String link = null;

                    for (int j = 0; j < textEntities.length(); j++) {
                        JSONObject entity = textEntities.getJSONObject(j);
                        String type = entity.optString("type");
                        String text = entity.optString("text");

                        if ("plain".equals(type)) {
                            plainText.append(text);
                        } else if ("link".equals(type)) {
                            link = text;
                        }
                    }

                    if (plainText.length() > 0) {
                        System.out.println("plain text: " + plainText.toString());
                    }

                    if (link != null) {
                        System.out.println("link: " + link);
                    }
                } else {
                    System.out.println("text_entities: 无内容");
                }

                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
