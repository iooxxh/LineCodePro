package cn.lineai.ai.message;

public final class UserModelMessage extends ModelMessage {
    public UserModelMessage(String content) {
        super(content);
    }

    @Override
    public String getRole() {
        return "user";
    }
}
