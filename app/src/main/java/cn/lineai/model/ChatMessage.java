package cn.lineai.model;

public final class ChatMessage {
    public enum Role {
        SYSTEM("system"),
        USER("user"),
        ASSISTANT("assistant"),
        TOOL("tool");

        private final String protocolName;

        Role(String protocolName) {
            this.protocolName = protocolName;
        }

        public String getProtocolName() {
            return protocolName;
        }
    }

    private final String id;
    private final Role role;
    private final String content;
    private final String reasoningContent;
    private final boolean streaming;
    private final boolean hidden;
    private final boolean excludeFromContext;

    public ChatMessage(String id, Role role, String content, boolean streaming) {
        this(id, role, content, "", streaming, false, false);
    }

    public ChatMessage(String id, Role role, String content, String reasoningContent, boolean streaming) {
        this(id, role, content, reasoningContent, streaming, false, false);
    }

    public ChatMessage(
            String id,
            Role role,
            String content,
            String reasoningContent,
            boolean streaming,
            boolean hidden,
            boolean excludeFromContext
    ) {
        this.id = id;
        this.role = role == null ? Role.USER : role;
        this.content = content == null ? "" : content;
        this.reasoningContent = reasoningContent == null ? "" : reasoningContent;
        this.streaming = streaming;
        this.hidden = hidden;
        this.excludeFromContext = excludeFromContext;
    }

    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public String getReasoningContent() {
        return reasoningContent;
    }

    public boolean isStreaming() {
        return streaming;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isExcludeFromContext() {
        return excludeFromContext;
    }

    public String getProtocolRole() {
        return role.getProtocolName();
    }

    public ChatMessage withContent(String nextContent, String nextReasoningContent, boolean nextStreaming) {
        return new ChatMessage(id, role, nextContent, nextReasoningContent, nextStreaming, hidden, excludeFromContext);
    }
}
