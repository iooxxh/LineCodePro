package cn.lineai.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChatUiState {
    private final String projectLabel;
    private final String modelLabel;
    private final String contextLabel;
    private final int contextPercent;
    private final boolean streaming;
    private final boolean hasConfiguredModel;
    private final List<ChatMessage> messages;

    public ChatUiState(
            String projectLabel,
            String modelLabel,
            String contextLabel,
            int contextPercent,
            boolean streaming,
            boolean hasConfiguredModel,
            List<ChatMessage> messages
    ) {
        this.projectLabel = projectLabel;
        this.modelLabel = modelLabel;
        this.contextLabel = contextLabel;
        this.contextPercent = contextPercent;
        this.streaming = streaming;
        this.hasConfiguredModel = hasConfiguredModel;
        this.messages = Collections.unmodifiableList(new ArrayList<>(messages));
    }

    public String getProjectLabel() {
        return projectLabel;
    }

    public String getModelLabel() {
        return modelLabel;
    }

    public String getContextLabel() {
        return contextLabel;
    }

    public int getContextPercent() {
        return contextPercent;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public boolean hasConfiguredModel() {
        return hasConfiguredModel;
    }

    public List<ChatMessage> getMessages() {
        return messages;
    }
}
