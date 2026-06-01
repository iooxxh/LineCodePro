package cn.lineai.ui.component;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class McpExtensionEditScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public McpExtensionEditScreenView(Context context, Listener listener) {
        super(context, "添加 MCP", listener::onBack, saveAction(context));
        LinearLayout content = getContent();
        addForm(content, "连接信息",
                new FormTextFieldView(context, "名称", "", "例如：公司 MCP 服务", null, false, false),
                new FormTextFieldView(context, "HTTP/S 地址", "", "https://example.com/mcp", "查询会请求 tools/list 并展示 MCP 工具列表。", false, false));

        SettingsSectionView headers = new SettingsSectionView(context, "自定义请求头");
        headers.addRow(new ActionRowView(context, IconButtonView.PLUS, "添加请求头", "查询和调用 MCP tools 时会附带这些请求头。", false, false, null), false);
        content.addView(headers, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView query = new SettingsSectionView(context, "查询");
        query.addRow(new ActionRowView(context, IconButtonView.SEARCH, "查询 MCP 列表", "填写地址后查询服务暴露的 tools。", false, true, null), false);
        content.addView(query, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView tools = new SettingsSectionView(context, "TOOLS 列表 · 已启用 0/0");
        TextView empty = LineTheme.text(context, "查询后会在这里显示 tools 列表，可单独开启或关闭。", LineTheme.FONT_SM, LineTheme.TEXT_TERTIARY, android.graphics.Typeface.NORMAL);
        empty.setGravity(android.view.Gravity.CENTER);
        LineTheme.padding(empty, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
        tools.addRow(empty, false);
        content.addView(tools, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private static android.view.View saveAction(Context context) {
        android.widget.TextView save = LineTheme.textMedium(context, "保存", LineTheme.FONT_MD, LineTheme.TEXT_TERTIARY);
        save.setGravity(android.view.Gravity.CENTER);
        return save;
    }

    private void addForm(LinearLayout content, String title, android.view.View first, android.view.View second) {
        Context context = content.getContext();
        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 12));
        LineTheme.padding(group, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
        group.addView(first, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams secondParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        secondParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        group.addView(second, secondParams);

        SectionHeaderView header = new SectionHeaderView(context, title);
        LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        headerParams.topMargin = LineTheme.dp(context, LineTheme.LG);
        headerParams.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(header, headerParams);
        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        groupParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        groupParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        content.addView(group, groupParams);
    }
}
