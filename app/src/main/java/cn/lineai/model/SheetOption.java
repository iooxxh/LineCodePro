package cn.lineai.model;

public final class SheetOption {
    private final String id;
    private final String label;
    private final String description;
    private final boolean selected;

    public SheetOption(String id, String label, String description, boolean selected) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSelected() {
        return selected;
    }
}
