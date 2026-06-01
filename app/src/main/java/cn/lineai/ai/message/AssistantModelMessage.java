package cn.lineai.ai.message;

public final class AssistantModelMessage extends ModelMessage {
    public AssistantModelMessage(String content) {
        super(content);
    }

    @Override
    public String getRole() {
        return "assistant";
    }
}
