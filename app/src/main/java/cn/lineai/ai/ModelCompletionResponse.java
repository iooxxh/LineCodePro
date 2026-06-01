package cn.lineai.ai;

public final class ModelCompletionResponse {
    private final String text;
    private final String reasoningContent;

    public ModelCompletionResponse(String text) {
        this(text, "");
    }

    public ModelCompletionResponse(String text, String reasoningContent) {
        this.text = text == null ? "" : text;
        this.reasoningContent = reasoningContent == null ? "" : reasoningContent;
    }

    public String getText() {
        return text;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }
}
