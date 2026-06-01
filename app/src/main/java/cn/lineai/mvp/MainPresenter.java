package cn.lineai.mvp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import cn.lineai.ai.ModelCancellationToken;
import cn.lineai.ai.ModelClient;
import cn.lineai.ai.ModelCompletionException;
import cn.lineai.ai.ModelCompletionResponse;
import cn.lineai.ai.ModelStreamCallback;
import cn.lineai.ai.message.AssistantModelMessage;
import cn.lineai.ai.message.ModelMessage;
import cn.lineai.ai.message.SystemModelMessage;
import cn.lineai.ai.message.ToolModelMessage;
import cn.lineai.ai.message.UserModelMessage;
import cn.lineai.ai.prompt.SystemPromptProvider;
import cn.lineai.model.ChatMessage;
import cn.lineai.model.ChatUiState;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.ModelContextInfo;
import cn.lineai.model.ModelContextParser;
import cn.lineai.model.ModelRepository;
import cn.lineai.model.SheetOption;
import java.util.ArrayList;
import java.util.List;

public final class MainPresenter implements MainContract.Presenter {
    private final ArrayList<ChatMessage> messages = new ArrayList<>();
    private final ArrayList<String> screenStack = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ModelRepository modelRepository;
    private final ModelClient modelClient = new ModelClient();
    private final SystemPromptProvider systemPromptProvider;
    private MainContract.View view;
    private ModelCancellationToken currentCancellationToken;
    private int messageSequence = 1;
    private int generationSequence = 1;
    private boolean streaming;
    private String projectLabel = "LineCode";
    private String projectPath = "/home/LangLang/AndroidStudioProjects/LineCode";
    private String permissionMode = "ask";

    public MainPresenter(Context context) {
        modelRepository = new ModelRepository(context);
        systemPromptProvider = new SystemPromptProvider(context);
    }

    @Override
    public void attachView(MainContract.View view) {
        this.view = view;
        render();
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void onMenuClick() {
        if (view != null) {
            view.showDrawer();
        }
    }

    @Override
    public void onProjectClick() {
        if (view == null) {
            return;
        }
        ArrayList<SheetOption> options = new ArrayList<>();
        options.add(new SheetOption("default", "LineCode", "/home/LangLang/AndroidStudioProjects/LineCode", "LineCode".equals(projectLabel)));
        options.add(new SheetOption("lineai", "LineAI 参考项目", "/home/LangLang/code/ts/LineAI", "LineAI 参考项目".equals(projectLabel)));
        options.add(new SheetOption("open", "打开项目", "选择外部目录并使用真实路径", false));
        options.add(new SheetOption("create", "创建项目", "在 .linecode/project 下创建目录", false));
        view.showSheet("项目", options);
    }

    @Override
    public void onPermissionClick() {
        if (view == null) {
            return;
        }
        ArrayList<SheetOption> options = new ArrayList<>();
        options.add(new SheetOption("ask", "每次询问", "工具执行前保持确认", "ask".equals(permissionMode)));
        options.add(new SheetOption("workspace", "工作区自动", "工作区内低风险操作自动执行", "workspace".equals(permissionMode)));
        options.add(new SheetOption("manual", "只读模式", "禁用写入、安装和 shell 自动执行", "manual".equals(permissionMode)));
        view.showSheet("权限设置", options);
    }

    @Override
    public void onNewConversation() {
        cancelActiveGeneration();
        streaming = false;
        messages.clear();
        render();
    }

    @Override
    public void onMoreClick() {
        if (view == null) {
            return;
        }
        ArrayList<SheetOption> options = new ArrayList<>();
        options.add(new SheetOption("tutorial", "教程", "打开初学者教程", false));
        options.add(new SheetOption("settings", "设置", "模型、主题、数据与实验功能", false));
        options.add(new SheetOption("compact", "压缩上下文", "将早期上下文总结为隐藏摘要", false));
        options.add(new SheetOption("clear", "清空对话", "清空当前对话消息", false));
        view.showSheet("更多", options);
    }

    @Override
    public void onSendMessage(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty() || streaming) {
            return;
        }
        messages.add(new ChatMessage(nextId(), ChatMessage.Role.USER, trimmed, false));
        ModelConfig selectedModel = modelRepository.getSelectedModel();
        if (selectedModel == null) {
            messages.add(new ChatMessage(nextId(), ChatMessage.Role.ASSISTANT,
                    "还没有可用模型。请进入 设置 → 模型管理 → 添加模型，保存后再发送消息。",
                    false));
            render();
            return;
        }

        int generationId = generationSequence++;
        ArrayList<ModelMessage> requestMessages = buildModelMessages();
        String assistantId = nextId();
        ModelCancellationToken cancellationToken = new ModelCancellationToken();
        currentCancellationToken = cancellationToken;
        streaming = true;
        messages.add(new ChatMessage(assistantId, ChatMessage.Role.ASSISTANT, "", true));
        render();

        new Thread(() -> {
            try {
                ModelCompletionResponse response = modelClient.stream(selectedModel, requestMessages, new ModelStreamCallback() {
                    @Override
                    public void onTextDelta(String delta) {
                        appendAssistantDelta(generationId, assistantId, delta, "");
                    }

                    @Override
                    public void onReasoningDelta(String delta) {
                        appendAssistantDelta(generationId, assistantId, "", delta);
                    }
                }, cancellationToken);
                finishGeneration(generationId, assistantId, response);
            } catch (ModelCompletionException e) {
                failGeneration(generationId, assistantId, "模型通信失败：\n" + e.getMessage());
            }
        }, "linecode-model-stream").start();
    }

    @Override
    public void onStopGeneration() {
        cancelActiveGeneration();
        streaming = false;
        generationSequence++;
        markStreamingMessagesStopped();
        render();
    }

    @Override
    public void onSheetOptionSelected(String id) {
        if ("default".equals(id)) {
            projectLabel = "LineCode";
            projectPath = "/home/LangLang/AndroidStudioProjects/LineCode";
        } else if ("lineai".equals(id)) {
            projectLabel = "LineAI 参考项目";
            projectPath = "/home/LangLang/code/ts/LineAI";
        } else if ("ask".equals(id) || "workspace".equals(id) || "manual".equals(id)) {
            permissionMode = id;
        } else if ("settings".equals(id)) {
            showScreen("settings");
        } else if ("tutorial".equals(id)) {
            showScreen("tutorial");
        } else if ("clear".equals(id)) {
            messages.clear();
        }
        if (view != null && !"settings".equals(id) && !"tutorial".equals(id)) {
            view.hideOverlays();
        }
        render();
    }

    @Override
    public void onScreenBack() {
        if (screenStack.isEmpty()) {
            return;
        }
        screenStack.remove(screenStack.size() - 1);
        if (view == null) {
            return;
        }
        if (screenStack.isEmpty()) {
            view.showChatScreen();
        } else {
            view.showScreen(screenStack.get(screenStack.size() - 1));
        }
    }

    @Override
    public void onSettingsItemSelected(String id) {
        if (id == null || id.length() == 0) {
            return;
        }
        showScreen(id);
    }

    @Override
    public List<ModelConfig> getModels() {
        return modelRepository.getModels();
    }

    @Override
    public String getSelectedModelId() {
        return modelRepository.getSelectedModelId();
    }

    @Override
    public void onModelSelected(String id) {
        modelRepository.setSelectedModelId(id);
        if (view != null) {
            view.showScreen("models");
        }
        render();
    }

    @Override
    public void onModelSaved(ModelConfig model) {
        ModelConfig saved = modelRepository.save(model);
        modelRepository.setSelectedModelId(saved.getId());
        while (!screenStack.isEmpty() && !"models".equals(screenStack.get(screenStack.size() - 1))) {
            screenStack.remove(screenStack.size() - 1);
        }
        if (screenStack.isEmpty()) {
            screenStack.add("models");
        }
        if (view != null) {
            view.showScreen("models");
        }
        render();
    }

    private void showScreen(String screenId) {
        screenStack.add(screenId);
        if (view != null) {
            view.hideOverlays();
            view.showScreen(screenId);
        }
    }

    private void render() {
        if (view == null) {
            return;
        }
        ModelConfig selectedModel = modelRepository.getSelectedModel();
        boolean hasConfiguredModel = selectedModel != null;
        ModelContextInfo contextInfo = selectedModel == null
                ? ModelContextParser.parse("")
                : ModelContextParser.parse(selectedModel.getModelId());
        String modelLabel = selectedModel == null
                ? "未选择模型"
                : contextInfo.getApiModelId();
        view.render(new ChatUiState(
                projectLabel,
                modelLabel,
                "0% / " + contextInfo.getContextLabel(),
                0,
                streaming,
                hasConfiguredModel,
                messages
        ));
    }

    private ArrayList<ModelMessage> buildModelMessages() {
        ArrayList<ModelMessage> modelMessages = new ArrayList<>();
        modelMessages.add(new SystemModelMessage(systemPromptProvider.build(projectPath)));
        int start = Math.max(0, messages.size() - 20);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            if (message.isExcludeFromContext() || message.getContent().trim().length() == 0) {
                continue;
            }
            modelMessages.add(toModelMessage(message));
        }
        return modelMessages;
    }

    private ModelMessage toModelMessage(ChatMessage message) {
        if (message.getRole() == ChatMessage.Role.SYSTEM) {
            return new SystemModelMessage(message.getContent());
        }
        if (message.getRole() == ChatMessage.Role.TOOL) {
            return new ToolModelMessage(message.getContent());
        }
        if (message.getRole() == ChatMessage.Role.USER) {
            return new UserModelMessage(message.getContent());
        }
        return new AssistantModelMessage(message.getContent());
    }

    private void appendAssistantDelta(int generationId, String assistantId, String textDelta, String reasoningDelta) {
        mainHandler.post(() -> {
            if (generationId != generationSequence - 1 || !streaming) {
                return;
            }
            int index = findMessageIndex(assistantId);
            if (index < 0) {
                return;
            }
            ChatMessage message = messages.get(index);
            String nextText = message.getContent() + (textDelta == null ? "" : textDelta);
            String nextReasoning = message.getReasoningContent() + (reasoningDelta == null ? "" : reasoningDelta);
            messages.set(index, message.withContent(nextText, nextReasoning, true));
            render();
        });
    }

    private void finishGeneration(int generationId, String assistantId, ModelCompletionResponse response) {
        mainHandler.post(() -> {
            if (generationId != generationSequence - 1 || !streaming) {
                return;
            }
            int index = findMessageIndex(assistantId);
            if (index < 0) {
                return;
            }
            ChatMessage message = messages.get(index);
            String finalText = response.getText().trim().length() == 0 ? message.getContent() : response.getText();
            String finalReasoning = response.getReasoningContent().trim().length() == 0 ? message.getReasoningContent() : response.getReasoningContent();
            if (finalText.trim().length() == 0 && finalReasoning.trim().length() == 0) {
                finalText = "模型没有返回文本。";
            }
            messages.set(index, message.withContent(finalText, finalReasoning, false));
            streaming = false;
            currentCancellationToken = null;
            render();
        });
    }

    private void failGeneration(int generationId, String assistantId, String text) {
        mainHandler.post(() -> {
            if (generationId != generationSequence - 1 || !streaming) {
                return;
            }
            int index = findMessageIndex(assistantId);
            if (index >= 0) {
                ChatMessage message = messages.get(index);
                messages.set(index, message.withContent(text, message.getReasoningContent(), false));
            } else {
                messages.add(new ChatMessage(nextId(), ChatMessage.Role.ASSISTANT, text, false));
            }
            streaming = false;
            currentCancellationToken = null;
            render();
        });
    }

    private void cancelActiveGeneration() {
        if (currentCancellationToken != null) {
            currentCancellationToken.cancel();
            currentCancellationToken = null;
        }
    }

    private void markStreamingMessagesStopped() {
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            if (message.isStreaming()) {
                messages.set(i, message.withContent(message.getContent(), message.getReasoningContent(), false));
            }
        }
    }

    private int findMessageIndex(String id) {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private String nextId() {
        return "m" + messageSequence++;
    }
}
