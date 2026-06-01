package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class ExtensionDetailScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();

        void onAddAgent();

        void onAddMcp();
    }

    public ExtensionDetailScreenView(Context context, String kind, Listener listener) {
        super(context, titleFor(kind), listener::onBack, addButton(context, kind, listener));
        LinearLayout content = getContent();
        LineTheme.padding(content, 0, 0, 0, 100);

        SettingsSectionView add = new SettingsSectionView(context, isSkills(kind) ? "安装" : "添加");
        add.addRow(new ActionRowView(context, iconFor(kind), inlineTitle(kind), inlineDesc(kind), false, true, () -> {
            if ("agent".equals(kind)) {
                listener.onAddAgent();
            } else if ("mcp".equals(kind)) {
                listener.onAddMcp();
            }
        }), false);
        content.addView(add, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView installed = new SettingsSectionView(context, "已安装");
        if ("agent".equals(kind)) {
            installed.addRow(item(context, IconButtonView.BRAIN, "测试修复 Agent", "按需启停，关闭后不会参与提示词和工具调用。", true), true);
            installed.addRow(item(context, IconButtonView.BRAIN, "代码审查 Agent", "检查边界条件、测试缺口和行为回归。", false), false);
        } else if ("mcp".equals(kind)) {
            installed.addRow(item(context, IconButtonView.SERVER, "公司 MCP 服务", "3/5 tools · https://example.com/mcp", true), false);
        } else if ("skills".equals(kind)) {
            installed.addRow(item(context, IconButtonView.ARCHIVE, "frontend-design", "本地 ~/.linecode/skills/frontend-design", true), true);
            installed.addRow(item(context, IconButtonView.ARCHIVE, "openai-docs", "本地 ~/.linecode/skills/openai-docs", true), false);
        } else {
            installed.addRow(item(context, IconButtonView.PACKAGE, "sample-plugin.lip", "导入到应用扩展目录，长按可删除。", true), false);
        }
        content.addView(installed, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private static IconButtonView addButton(Context context, String kind, Listener listener) {
        IconButtonView button = new IconButtonView(context, IconButtonView.PLUS);
        button.setIconColor(LineTheme.ACCENT);
        button.setIconSizeDp(36, 19);
        button.setBackground(LineTheme.rounded(context, LineTheme.ACCENT_MUTED, 18));
        button.setOnClickListener(v -> {
            if ("agent".equals(kind)) {
                listener.onAddAgent();
            } else if ("mcp".equals(kind)) {
                listener.onAddMcp();
            }
        });
        return button;
    }

    private static boolean isSkills(String kind) {
        return "skills".equals(kind);
    }

    private static String titleFor(String kind) {
        if ("agent".equals(kind)) return "Agent 扩展";
        if ("mcp".equals(kind)) return "MCP 扩展";
        if ("skills".equals(kind)) return "Skills 扩展";
        return "LineCode 扩展";
    }

    private static int iconFor(String kind) {
        if ("agent".equals(kind)) return IconButtonView.BRAIN;
        if ("mcp".equals(kind)) return IconButtonView.CPU;
        if ("skills".equals(kind)) return IconButtonView.ARCHIVE;
        return IconButtonView.PACKAGE;
    }

    private static String inlineTitle(String kind) {
        if ("skills".equals(kind)) return "选择 ZIP 安装";
        if ("linecode".equals(kind)) return "导入 LIP 扩展";
        if ("agent".equals(kind)) return "添加 Agent";
        return "添加 HTTP/S MCP";
    }

    private static String inlineDesc(String kind) {
        if ("skills".equals(kind)) return "选择技能包后再选择安装位置，SSH 模式会推送到远端 ~/.linecode。";
        if ("linecode".equals(kind)) return "选择 .lip 文件导入到应用扩展目录，长按已导入扩展可删除。";
        if ("agent".equals(kind)) return "自定义 Agent 可按需启停，关闭后不会参与提示词和工具调用。";
        return "自定义 MCP 可按需启停，关闭后不会作为工具暴露给 AI。";
    }

    private LinearLayout item(Context context, int iconType, String title, String desc, boolean enabled) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(LineTheme.dp(context, 68));
        LineTheme.padding(row, LineTheme.LG, LineTheme.MD, LineTheme.LG, LineTheme.MD);

        IconButtonView icon = new IconButtonView(context, iconType);
        icon.setIconColor(enabled ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY);
        icon.setIconSizeDp(36, 20);
        icon.setClickable(false);
        icon.setBackground(LineTheme.rounded(context, LineTheme.ACCENT_MUTED, 18));
        row.addView(icon, new LinearLayout.LayoutParams(LineTheme.dp(context, 36), LineTheme.dp(context, 36)));

        LinearLayout labels = new LinearLayout(context);
        labels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
        row.addView(labels, labelParams);
        labels.addView(LineTheme.textMedium(context, title, LineTheme.FONT_MD, enabled ? LineTheme.TEXT : LineTheme.TEXT_TERTIARY));
        TextView descView = LineTheme.text(context, desc, LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        descView.setLineSpacing(LineTheme.dp(context, 3), 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, 2);
        labels.addView(descView, descParams);

        TextView state = LineTheme.text(context, enabled ? "已启用" : "已关闭", LineTheme.FONT_XS, enabled ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY, Typeface.BOLD);
        state.setGravity(Gravity.CENTER);
        state.setBackground(LineTheme.rounded(context, enabled ? LineTheme.ACCENT_MUTED : LineTheme.SURFACE_LIGHT, 999));
        LineTheme.padding(state, LineTheme.SM, 3, LineTheme.SM, 3);
        row.addView(state, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        return row;
    }
}
