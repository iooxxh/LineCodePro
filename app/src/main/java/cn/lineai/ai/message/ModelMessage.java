package cn.lineai.ai.message;

public abstract class ModelMessage {
    private final String content;

    protected ModelMessage(String content) {
        this.content = content == null ? "" : content;
    }

    public final String getContent() {
        return content;
    }

    public abstract String getRole();
}
