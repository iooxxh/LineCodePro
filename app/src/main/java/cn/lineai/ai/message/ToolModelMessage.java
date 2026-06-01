package cn.lineai.ai.message;

public final class ToolModelMessage extends ModelMessage {
    public ToolModelMessage(String content) {
        super(content);
    }

    @Override
    public String getRole() {
        return "tool";
    }
}
