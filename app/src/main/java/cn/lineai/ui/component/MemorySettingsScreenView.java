package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class MemorySettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public MemorySettingsScreenView(Context context, Listener listener) {
        super(context, "记忆", listener::onBack, addButton(context));
        LinearLayout content = getContent();

        TextView hint = LineTheme.text(context, "当前项目: /home/LangLang/AndroidStudioProjects/LineCode", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        hintParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        hintParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        hintParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(hint, hintParams);

        addMemorySection(content, "长期记忆（2）", new String[][] {
                {"偏好 Java 原生 View", "用户要求不使用 AndroidX，组件独立继承 View"},
                {"UI 对齐 LineAI", "图标直接来自 lucide-react-native node_modules"},
        });
        addMemorySection(content, "项目记忆（1）", new String[][] {
                {"LineCode MVP 架构", "MainPresenter 管理原生页面栈和弹层事件"},
        });
        addMemorySection(content, "环境记忆（0）", new String[0][0]);
        addMemorySection(content, "短期记忆（0）", new String[0][0]);
        addMemorySection(content, "聊天索引（1）", new String[][] {
                {"继续写 UI", "设置、模型、扩展和系统页正在迁移为 Java 原生页面"},
        });
    }

    private static IconButtonView addButton(Context context) {
        IconButtonView button = new IconButtonView(context, IconButtonView.PLUS);
        button.setIconColor(LineTheme.ACCENT);
        button.setIconSizeDp(36, 20);
        return button;
    }

    private void addMemorySection(LinearLayout content, String title, String[][] rows) {
        Context context = content.getContext();
        SettingsSectionView section = new SettingsSectionView(context, title);
        if (rows.length == 0) {
            TextView empty = LineTheme.text(context, "暂无内容", LineTheme.FONT_SM, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
            LineTheme.padding(empty, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
            section.addRow(empty, false);
        } else {
            for (int i = 0; i < rows.length; i++) {
                section.addRow(new ActionRowView(context, IconButtonView.BOOK_OPEN, rows[i][0], rows[i][1], false, true, null), i < rows.length - 1);
            }
        }
        content.addView(section, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
}
