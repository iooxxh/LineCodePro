package cn.lineai.tool.builtin;

import android.content.Context;
import cn.lineai.ai.ImageInputPayload;
import cn.lineai.ai.ModelClient;
import cn.lineai.ai.ModelCompletionResponse;
import cn.lineai.ai.message.ModelMessage;
import cn.lineai.ai.message.SystemModelMessage;
import cn.lineai.ai.message.UserModelMessage;
import cn.lineai.data.repository.ToolSettingsRepository;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.ModelProtocolType;
import cn.lineai.model.ModelRepository;
import cn.lineai.tool.BaseTool;
import cn.lineai.tool.ToolCategory;
import cn.lineai.tool.ToolContext;
import cn.lineai.tool.ToolResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import org.json.JSONObject;

public final class ImageUnderstandingTool extends BaseTool {
    private static final long MAX_IMAGE_BYTES = 10L * 1024L * 1024L;

    private final ToolSettingsRepository settingsRepository;
    private final ModelRepository modelRepository;
    private final ModelClient modelClient;

    public ImageUnderstandingTool() {
        this(null);
    }

    public ImageUnderstandingTool(Context context) {
        Context appContext = context == null ? null : context.getApplicationContext();
        settingsRepository = appContext == null ? null : new ToolSettingsRepository(appContext);
        modelRepository = appContext == null ? null : new ModelRepository(appContext);
        modelClient = new ModelClient();
    }

    @Override
    public String getName() {
        return "image_understanding";
    }

    @Override
    public String getDescription() {
        return "读取本地图片文件并调用工具设置里选择的视觉模型理解图片内容。支持 OpenAI 兼容、Codex Responses 和 Anthropic Messages 协议。";
    }

    @Override
    public ToolCategory getCategory() {
        return ToolCategory.READ;
    }

    @Override
    public JSONObject getParameters() throws org.json.JSONException {
        return new JSONObject()
                .put("type", "object")
                .put("properties", new JSONObject()
                        .put("path", new JSONObject()
                                .put("type", "string")
                                .put("description", "图片路径，相对当前工作区或已授权目录，也可以是工作区内绝对路径"))
                        .put("prompt", new JSONObject()
                                .put("type", "string")
                                .put("description", "希望视觉模型回答的问题或分析要求")))
                .put("required", new org.json.JSONArray().put("path").put("prompt"));
    }

    @Override
    public ToolResult execute(JSONObject input, ToolContext context) {
        if (settingsRepository == null || modelRepository == null) {
            return error("图片理解工具未接入应用上下文。");
        }
        String path = first(input, "path", "image_path", "file_path");
        if (path.length() == 0) {
            return error("图片路径不能为空。");
        }
        String prompt = input.optString("prompt").trim();
        if (prompt.length() == 0) {
            prompt = "请描述这张图片的内容。";
        }
        ModelConfig model = selectedModel();
        if (model == null) {
            return error("图片理解未选择模型。请在 设置 -> 工具设置 -> 图片操作 中选择视觉模型。");
        }
        if (model.getProtocolType() == ModelProtocolType.LOCAL_GGUF) {
            return error("本地 GGUF 协议暂不支持图片理解工具。请选择 OpenAI、Codex 或 Anthropic 协议模型。");
        }
        try {
            File file = FileToolPathPolicy.resolve(context, path);
            if (!file.exists()) {
                return error("图片文件不存在: " + path);
            }
            if (file.isDirectory()) {
                return error("路径是目录，无法作为图片理解: " + path);
            }
            if (file.length() > MAX_IMAGE_BYTES) {
                return error("图片过大，当前上限为 10 MB: " + path);
            }
            String mimeType = mimeType(file);
            if (!ImageInputPayload.isSupportedMimeType(mimeType)) {
                return error("不支持的图片格式: " + file.getName() + "。支持 PNG、JPEG、WebP 和 GIF。");
            }
            String rawInput = ImageInputPayload.rawInputJson(prompt, mimeType, android.util.Base64.encodeToString(readBytes(file), android.util.Base64.NO_WRAP));
            ArrayList<ModelMessage> messages = new ArrayList<>();
            messages.add(new SystemModelMessage("你是 LineCode 的图片理解工具。根据用户提示分析图片，只返回与图片和提示相关的内容。不要提及工具调用、base64 或文件路径；无法确定时说明不确定。"));
            messages.add(new UserModelMessage(prompt, rawInput));
            ModelCompletionResponse response = modelClient.complete(model, messages);
            String text = response.getText().trim();
            return ok(text.length() == 0 ? "视觉模型没有返回内容。" : text);
        } catch (Exception e) {
            return error("图片理解失败: " + e.getMessage());
        }
    }

    private ModelConfig selectedModel() {
        String modelId = settingsRepository.getImageUnderstandingModelId();
        return modelId.length() == 0 ? null : modelRepository.getModel(modelId);
    }

    private byte[] readBytes(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
            return output.toByteArray();
        } finally {
            input.close();
        }
    }

    private String mimeType(File file) {
        String name = file == null ? "" : file.getName().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".png")) {
            return "image/png";
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (name.endsWith(".webp")) {
            return "image/webp";
        }
        if (name.endsWith(".gif")) {
            return "image/gif";
        }
        return "";
    }

    private String first(JSONObject input, String... keys) {
        if (input == null) {
            return "";
        }
        for (String key : keys) {
            String value = input.optString(key).trim();
            if (value.length() > 0) {
                return value;
            }
        }
        return "";
    }

    private ToolResult ok(String content) {
        return new ToolResult("", getName(), content, false);
    }

    private ToolResult error(String content) {
        return new ToolResult("", getName(), content, true);
    }
}
