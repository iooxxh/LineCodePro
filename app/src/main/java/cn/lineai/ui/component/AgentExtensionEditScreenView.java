package cn.lineai.ui.component;

import android.content.Context;
import android.widget.LinearLayout;
import cn.lineai.ui.theme.LineTheme;

public final class AgentExtensionEditScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public AgentExtensionEditScreenView(Context context, Listener listener) {
        super(context, "添加 Agent", listener::onBack, saveAction(context));
        LinearLayout content = getContent();

        SettingsSectionView quick = new SettingsSectionView(context, "快速创建");
        quick.addRow(new ActionRowView(context, IconButtonView.SPARKLES, "让 AI 写", "直接描述所需 Agent，自动填写名称、提示词、触发条件和权限。", false, true, null), false);
        content.addView(quick, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addForm(content, "基础信息", new FormTextFieldView(context, "名字", "", "例如：测试修复 Agent", null, false, false),
                new FormTextFieldView(context, "英文标识", "", "test-fixer", "用于触发和识别自定义 Agent。", false, false));
        addForm(content, "行为定义", new FormTextFieldView(context, "提示词", "", "描述这个 Agent 的角色、边界、输出格式和验收方式", null, true, false),
                new FormTextFieldView(context, "触发条件", "", "例如：用户要求修复测试、分析性能、重构组件时触发", null, true, false));

        SettingsSectionView tools = new SettingsSectionView(context, "可使用的工具 · 已选 2");
        tools.addRow(new OptionRowView(context, IconButtonView.SETTINGS, "file_read", "files · 读取项目文件内容", true, null), true);
        tools.addRow(new OptionRowView(context, IconButtonView.SEARCH, "glob", "search · 按模式搜索文件", true, null), true);
        tools.addRow(new OptionRowView(context, IconButtonView.TERMINAL, "shell", "command · 执行 shell 命令", false, null), false);
        content.addView(tools, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView mcps = new SettingsSectionView(context, "可使用的 MCP · 已选 0");
        mcps.addRow(new OptionRowView(context, IconButtonView.CPU, "文件操作", "read_file, write_file, list_dir", false, null), true);
        mcps.addRow(new OptionRowView(context, IconButtonView.SEARCH, "网页搜索", "search, open, summarize", false, null), false);
        content.addView(mcps, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
