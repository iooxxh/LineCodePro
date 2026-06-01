package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class MCPSettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public MCPSettingsScreenView(Context context, Listener listener) {
        super(context, "MCP 工具", listener::onBack, null);
        LinearLayout content = getContent();
        LineTheme.padding(content, LineTheme.LG, LineTheme.LG, LineTheme.LG, 100);

        addExecutionTarget(content);
        addWebSearch(content);
        addToolCard(content, IconButtonView.FILE_CODE, "文件操作", "读取、写入、列目录、搜索和补丁应用", true,
                new String[] {"read_file", "write_file", "list_dir", "apply_patch"});
        addToolCard(content, IconButtonView.SERVER, "HTTP 服务器", "启动临时网页服务并转发端口", true,
                new String[] {"start_server", "stop_server", "open_url"});
        addToolCard(content, IconButtonView.TERMINAL, "Shell", "通过本机或 SSH Shell 执行命令", false,
                new String[] {"exec", "stream", "kill"});
        addToolCard(content, IconButtonView.SEARCH, "网页搜索", "应用侧搜索 API 配置，Local 与 SSH 模式共用", true,
                new String[] {"search", "open", "summarize"});
    }

    private void addExecutionTarget(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout card = card(context);
        card.addView(title(context, "执行目标"));
        LinearLayout segment = new LinearLayout(context);
        segment.setOrientation(HORIZONTAL);
        segment.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 8));
        LineTheme.padding(segment, 3, 3, 3, 3);
        addModeButton(segment, "本地工作区", true);
        addModeButton(segment, "SSH Shell", false);
        LinearLayout.LayoutParams segmentParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LineTheme.dp(context, 42));
        segmentParams.topMargin = LineTheme.dp(context, LineTheme.SM);
        card.addView(segment, segmentParams);
        TextView desc = desc(context, "SSH Shell 模式会禁用本地文件读写、文件搜索、Agent 和 HTTP 服务器；网页搜索仍由应用侧网络配置执行。");
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, LineTheme.SM);
        card.addView(desc, descParams);
        addCard(content, card);
    }

    private void addWebSearch(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout card = card(context);
        card.addView(title(context, "网页搜索配置"));
        TextView desc = desc(context, "需要先打开“网页搜索”工具，并填写你自己的搜索 API、模型/搜索源和密钥。本地与 SSH 模式共用这组配置。");
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, 2);
        card.addView(desc, descParams);

        GridLayout providers = new GridLayout(context);
        providers.setColumnCount(3);
        String[] names = new String[] {"Tavily", "SerpAPI", "Bing", "Exa", "Perplexity", "自定义"};
        for (int i = 0; i < names.length; i++) {
            TextView button = LineTheme.text(context, names[i], LineTheme.FONT_XS, i == 0 ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_SECONDARY, Typeface.BOLD);
            button.setGravity(Gravity.CENTER);
            button.setBackground(LineTheme.roundedStroke(context, i == 0 ? LineTheme.ACCENT : LineTheme.SURFACE_LIGHT, 8, i == 0 ? LineTheme.ACCENT : LineTheme.BORDER_LIGHT));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = LineTheme.dp(context, 34);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(0, LineTheme.dp(context, LineTheme.SM), LineTheme.dp(context, LineTheme.SM), 0);
            providers.addView(button, params);
        }
        LinearLayout.LayoutParams providerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        providerParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        card.addView(providers, providerParams);

        card.addView(new FormTextFieldView(context, "Search API URL", "", "https://api.example.com/search", null, false, false), formParams(context));
        card.addView(new FormTextFieldView(context, "API Key", "", "搜索服务密钥", null, false, true), formParams(context));
        card.addView(new FormTextFieldView(context, "模型 / 搜索源", "", "如 basic、advanced、google，可留空", null, false, false), formParams(context));
        addCard(content, card);
    }

    private void addToolCard(LinearLayout content, int iconType, String name, String desc, boolean enabled, String[] tools) {
        Context context = content.getContext();
        LinearLayout card = card(context);
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        IconButtonView icon = new IconButtonView(context, iconType);
        icon.setIconColor(enabled ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY);
        icon.setIconSizeDp(36, 19);
        icon.setClickable(false);
        icon.setBackground(LineTheme.rounded(context, LineTheme.ACCENT_MUTED, 18));
        header.addView(icon, new LinearLayout.LayoutParams(LineTheme.dp(context, 36), LineTheme.dp(context, 36)));

        LinearLayout labels = new LinearLayout(context);
        labels.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
        labelParams.rightMargin = LineTheme.dp(context, LineTheme.MD);
        header.addView(labels, labelParams);
        labels.addView(LineTheme.textMedium(context, name, LineTheme.FONT_MD, LineTheme.TEXT));
        TextView descView = desc(context, desc);
        LinearLayout.LayoutParams descViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descViewParams.topMargin = LineTheme.dp(context, 2);
        labels.addView(descView, descViewParams);

        SwitchRowView switchRow = new SwitchRowView(context, IconButtonView.CHECK, "", "", enabled);
        switchRow.setPadding(0, 0, 0, 0);
        header.addView(switchRow, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        card.addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        View divider = new View(context);
        divider.setBackgroundColor(LineTheme.BORDER_LIGHT);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
        dividerParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        card.addView(divider, dividerParams);

        LinearLayout toolWrap = new LinearLayout(context);
        toolWrap.setOrientation(HORIZONTAL);
        toolWrap.setGravity(Gravity.LEFT);
        toolWrap.setBaselineAligned(false);
        for (String tool : tools) {
            TextView badge = LineTheme.text(context, tool, LineTheme.FONT_XS, LineTheme.TEXT_SECONDARY, Typeface.NORMAL);
            badge.setTypeface(Typeface.MONOSPACE);
            badge.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 4));
            LineTheme.padding(badge, LineTheme.SM, 2, LineTheme.SM, 2);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            badgeParams.rightMargin = LineTheme.dp(context, LineTheme.SM);
            badgeParams.topMargin = LineTheme.dp(context, LineTheme.SM);
            toolWrap.addView(badge, badgeParams);
        }
        card.addView(toolWrap, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addCard(content, card);
    }

    private void addModeButton(LinearLayout segment, String label, boolean active) {
        Context context = segment.getContext();
        TextView button = LineTheme.text(context, label, LineTheme.FONT_SM, active ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_SECONDARY, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(LineTheme.rounded(context, active ? LineTheme.ACCENT : android.graphics.Color.TRANSPARENT, 8));
        segment.addView(button, new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f));
    }

    private LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 12));
        LineTheme.padding(card, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
        return card;
    }

    private TextView title(Context context, String text) {
        return LineTheme.text(context, text, LineTheme.FONT_MD, LineTheme.TEXT, Typeface.BOLD);
    }

    private TextView desc(Context context, String text) {
        TextView view = LineTheme.text(context, text, LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        view.setLineSpacing(LineTheme.dp(context, 3), 1f);
        return view;
    }

    private LinearLayout.LayoutParams formParams(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = LineTheme.dp(context, LineTheme.MD);
        return params;
    }

    private void addCard(LinearLayout content, LinearLayout card) {
        Context context = content.getContext();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(card, params);
    }
}
