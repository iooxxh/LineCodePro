package cn.lineai.ai.protocol;

import cn.lineai.ai.ModelCompletionException;
import cn.lineai.ai.ModelCompletionResponse;
import cn.lineai.ai.ModelCancellationToken;
import cn.lineai.ai.ModelStreamCallback;
import cn.lineai.ai.message.ModelMessage;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.ModelContextParser;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class AnthropicMessagesProtocol extends AbstractHttpModelProtocol {
    @Override
    public ModelCompletionResponse complete(ModelConfig config, List<ModelMessage> messages) throws ModelCompletionException {
        try {
            JSONObject body = new JSONObject();
            body.put("model", ModelContextParser.apiModelId(config.getModelId()));
            body.put("max_tokens", 4096);
            body.put("messages", messagesJson(messages));

            String system = systemPrompt(messages);
            if (system.length() > 0) {
                body.put("system", system);
            }

            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-api-key", config.getApiKey());
            headers.put("anthropic-version", "2023-06-01");
            String raw = postJson(endpoint(config.getBaseUrl(), "/v1/messages"), body, headers);
            JSONObject response = new JSONObject(raw);
            return new ModelCompletionResponse(extractContent(response.optJSONArray("content")));
        } catch (ModelCompletionException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCompletionException("Anthropic Messages 协议解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ModelCompletionResponse stream(
            ModelConfig config,
            List<ModelMessage> messages,
            ModelStreamCallback callback,
            ModelCancellationToken cancellationToken
    ) throws ModelCompletionException {
        try {
            JSONObject body = new JSONObject();
            body.put("model", ModelContextParser.apiModelId(config.getModelId()));
            body.put("max_tokens", 5120);
            body.put("messages", messagesJson(messages));
            body.put("stream", true);
            body.put("thinking", new JSONObject()
                    .put("type", "enabled")
                    .put("budget_tokens", 4096));

            String system = systemPrompt(messages);
            if (system.length() > 0) {
                body.put("system", system);
            }

            HashMap<String, String> headers = new HashMap<>();
            headers.put("x-api-key", config.getApiKey());
            headers.put("anthropic-version", "2023-06-01");

            StringBuilder text = new StringBuilder();
            StringBuilder reasoning = new StringBuilder();

            postJsonSse(endpoint(config.getBaseUrl(), "/v1/messages"), body, headers, cancellationToken, (eventType, data) -> {
                if ("[DONE]".equals(data.trim())) {
                    return;
                }
                JSONObject event = new JSONObject(data);
                if (event.has("error")) {
                    throw new ModelCompletionException("Anthropic 流式错误: " + event.opt("error"));
                }
                String type = event.optString("type");

                if ("content_block_start".equals(type)) {
                    JSONObject block = event.optJSONObject("content_block");
                    if (block != null && "redacted_thinking".equals(block.optString("type"))) {
                        appendDelta(reasoning, "[redacted thinking]", true, callback);
                    }
                    return;
                }

                if (!"content_block_delta".equals(type)) {
                    return;
                }

                JSONObject delta = event.optJSONObject("delta");
                if (delta == null) {
                    return;
                }
                String deltaType = delta.optString("type");
                if ("thinking_delta".equals(deltaType)) {
                    appendDelta(reasoning, delta.optString("thinking"), true, callback);
                } else if ("text_delta".equals(deltaType)) {
                    appendDelta(text, delta.optString("text"), false, callback);
                }
            });

            return new ModelCompletionResponse(text.toString(), reasoning.toString());
        } catch (ModelCompletionException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCompletionException("Anthropic Messages 协议流式解析失败: " + e.getMessage(), e);
        }
    }

    private JSONArray messagesJson(List<ModelMessage> messages) throws Exception {
        JSONArray array = new JSONArray();
        for (ModelMessage message : messages) {
            if ("system".equals(message.getRole())) {
                continue;
            }
            JSONObject object = new JSONObject();
            object.put("role", message.getRole());
            object.put("content", message.getContent());
            array.put(object);
        }
        return array;
    }

    private String systemPrompt(List<ModelMessage> messages) {
        StringBuilder builder = new StringBuilder();
        for (ModelMessage message : messages) {
            if ("system".equals(message.getRole())) {
                if (builder.length() > 0) {
                    builder.append('\n');
                }
                builder.append(message.getContent());
            }
        }
        return builder.toString();
    }

    private String extractContent(JSONArray content) {
        if (content == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            JSONObject block = content.optJSONObject(i);
            if (block != null && "text".equals(block.optString("type"))) {
                builder.append(block.optString("text"));
            }
        }
        return builder.toString();
    }

    private void appendDelta(
            StringBuilder target,
            String delta,
            boolean thinking,
            ModelStreamCallback callback
    ) {
        if (delta == null || delta.length() == 0) {
            return;
        }
        target.append(delta);
        if (callback == null) {
            return;
        }
        if (thinking) {
            callback.onReasoningDelta(delta);
        } else {
            callback.onTextDelta(delta);
        }
    }
}
