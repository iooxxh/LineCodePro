package cn.lineai.model;

public enum ModelProtocolType {
    OPENAI_COMPATIBLE("OpenAI"),
    CODEX_RESPONSES("Codex"),
    ANTHROPIC_MESSAGES("Anthropic"),
    LOCAL_GGUF("本地");

    private final String label;

    ModelProtocolType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static ModelProtocolType fromStorage(String value) {
        if (value == null) {
            return OPENAI_COMPATIBLE;
        }
        for (ModelProtocolType type : values()) {
            if (type.name().equals(value)) {
                return type;
            }
        }
        return OPENAI_COMPATIBLE;
    }
}
