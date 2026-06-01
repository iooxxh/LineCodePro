package cn.lineai.ai.protocol;

import cn.lineai.ai.ModelCompletionException;
import cn.lineai.ai.ModelCompletionResponse;
import cn.lineai.ai.ModelCancellationToken;
import cn.lineai.ai.ModelStreamCallback;
import cn.lineai.ai.message.ModelMessage;
import cn.lineai.ai.stream.ThinkTagParser;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.ModelContextParser;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public final class OpenAiCompatibleProtocol extends AbstractHttpModelProtocol {
    @Override
    public ModelCompletionResponse complete(ModelConfig config, List<ModelMessage> messages) throws ModelCompletionException {
        try {
            JSONObject body = new JSONObject();
            body.put("model", ModelContextParser.apiModelId(config.getModelId()));
            body.put("messages", messagesJson(messages));
            body.put("temperature", 0.2);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getApiKey());
            String raw = postJson(endpoint(config.getBaseUrl(), "/chat/completions"), body, headers);
            JSONObject response = new JSONObject(raw);
            String text = response
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content");
            return new ModelCompletionResponse(text);
        } catch (ModelCompletionException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCompletionException("OpenAI 兼容协议解析失败: " + e.getMessage(), e);
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
            body.put("messages", messagesJson(messages));
            body.put("temperature", 0.2);
            body.put("stream", true);
            applyReasoningRequest(config, body);

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + config.getApiKey());

            StringBuilder text = new StringBuilder();
            StringBuilder reasoning = new StringBuilder();
            ThinkTagParser thinkTagParser = new ThinkTagParser();

            postJsonSse(endpoint(config.getBaseUrl(), "/chat/completions"), body, headers, cancellationToken, (eventType, data) -> {
                if ("[DONE]".equals(data.trim())) {
                    return;
                }
                JSONObject event = new JSONObject(data);
                if (event.has("error")) {
                    throw new ModelCompletionException("OpenAI 流式错误: " + event.opt("error"));
                }
                JSONArray choices = event.optJSONArray("choices");
                if (choices == null || choices.length() == 0) {
                    return;
                }
                JSONObject choice = choices.optJSONObject(0);
                if (choice == null) {
                    return;
                }
                if ("content_filter".equals(choice.optString("finish_reason"))) {
                    throw new ModelCompletionException("OpenAI 流式错误: 输出被内容安全策略拦截");
                }
                JSONObject delta = choice.optJSONObject("delta");
                if (delta == null) {
                    return;
                }

                String reasoningDelta = extractReasoningDelta(delta);
                if (reasoningDelta.length() > 0) {
                    reasoning.append(reasoningDelta);
                    if (callback != null) {
                        callback.onReasoningDelta(reasoningDelta);
                    }
                }

                if (delta.has("content") && !delta.isNull("content")) {
                    ThinkTagParser.Result parsed = thinkTagParser.append(delta.optString("content"));
                    appendParsedDelta(text, reasoning, parsed, callback);
                }
            });

            appendParsedDelta(text, reasoning, thinkTagParser.flush(), callback);
            return new ModelCompletionResponse(text.toString(), reasoning.toString());
        } catch (ModelCompletionException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelCompletionException("OpenAI 兼容协议流式解析失败: " + e.getMessage(), e);
        }
    }

    private JSONArray messagesJson(List<ModelMessage> messages) throws Exception {
        JSONArray array = new JSONArray();
        for (ModelMessage message : messages) {
            JSONObject object = new JSONObject();
            object.put("role", message.getRole());
            object.put("content", message.getContent());
            array.put(object);
        }
        return array;
    }

    private void appendParsedDelta(
            StringBuilder text,
            StringBuilder reasoning,
            ThinkTagParser.Result parsed,
            ModelStreamCallback callback
    ) {
        if (parsed.getThinking().length() > 0) {
            reasoning.append(parsed.getThinking());
            if (callback != null) {
                callback.onReasoningDelta(parsed.getThinking());
            }
        }
        if (parsed.getText().length() > 0) {
            text.append(parsed.getText());
            if (callback != null) {
                callback.onTextDelta(parsed.getText());
            }
        }
    }

    private String extractReasoningDelta(JSONObject delta) {
        if (delta.has("reasoning_content") && !delta.isNull("reasoning_content")) {
            return delta.optString("reasoning_content");
        }
        Object reasoning = delta.opt("reasoning");
        if (reasoning instanceof String) {
            return (String) reasoning;
        }
        if (reasoning instanceof JSONObject) {
            JSONObject object = (JSONObject) reasoning;
            if (object.has("content")) {
                return object.optString("content");
            }
            if (object.has("text")) {
                return object.optString("text");
            }
        }
        Object details = delta.opt("reasoning_details");
        if (details instanceof JSONObject) {
            JSONObject object = (JSONObject) details;
            if (object.has("content")) {
                return object.optString("content");
            }
            if (object.has("text")) {
                return object.optString("text");
            }
            if (object.has("reasoning_content")) {
                return object.optString("reasoning_content");
            }
        }
        if (details instanceof JSONArray) {
            JSONArray array = (JSONArray) details;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.length(); i++) {
                Object item = array.opt(i);
                if (item instanceof String) {
                    builder.append((String) item);
                } else if (item instanceof JSONObject) {
                    JSONObject object = (JSONObject) item;
                    if (object.has("content")) {
                        builder.append(object.optString("content"));
                    } else if (object.has("text")) {
                        builder.append(object.optString("text"));
                    } else if (object.has("reasoning_content")) {
                        builder.append(object.optString("reasoning_content"));
                    }
                }
            }
            return builder.toString();
        }
        return "";
    }

    private void applyReasoningRequest(ModelConfig config, JSONObject body) throws Exception {
        String base = config.getBaseUrl().toLowerCase();
        String model = ModelContextParser.apiModelId(config.getModelId()).toLowerCase();
        if (base.contains("dashscope") || base.contains("aliyuncs") || model.contains("qwen")) {
            body.put("enable_thinking", true);
            body.put("thinking_budget", 4096);
            return;
        }
        if (base.contains("minimax") || model.contains("minimax") || model.contains("abab") || model.contains("m2")) {
            body.put("reasoning_split", true);
            return;
        }
        if (base.contains("deepseek") || model.contains("deepseek")) {
            body.put("thinking", new JSONObject().put("type", "enabled"));
            body.put("reasoning_effort", "high");
            return;
        }
        if (base.contains("moonshot") || base.contains("kimi") || model.contains("kimi")
                || base.contains("bigmodel") || base.contains("zhipu") || model.contains("glm")
                || base.contains("mimo") || base.contains("xiaomi") || model.contains("mimo")) {
            body.put("thinking", new JSONObject().put("type", "enabled"));
            return;
        }
        body.put("reasoning", new JSONObject().put("effort", "medium"));
    }
}
