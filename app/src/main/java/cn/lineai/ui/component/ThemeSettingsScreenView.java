package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class ThemeSettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    private static final int[] SWATCHES = new int[] {
            Color.parseColor("#F4EFE6"), Color.parseColor("#FBF7EF"), Color.parseColor("#EEE5D8"), Color.parseColor("#E7DCCA"),
            Color.parseColor("#2B2118"), Color.parseColor("#6C5A49"), Color.parseColor("#9B8976"), Color.parseColor("#D97757"),
            Color.parseColor("#0A0A0A"), Color.parseColor("#1C1C1E"), Color.parseColor("#FFFFFF"), Color.parseColor("#0A84FF"),
            Color.parseColor("#1E1E1E"), Color.parseColor("#252526"), Color.parseColor("#007ACC"), Color.parseColor("#D4D4D4"),
            Color.parseColor("#0D1117"), Color.parseColor("#161B22"), Color.parseColor("#2F81F7"), Color.parseColor("#E6EDF3"),
            Color.parseColor("#282828"), Color.parseColor("#FABD2F"), Color.parseColor("#EBDBB2"), Color.parseColor("#458588"),
            Color.parseColor("#64D2FF"), Color.parseColor("#FFD60A"), Color.parseColor("#30D158"), Color.parseColor("#FF453A"),
    };

    public ThemeSettingsScreenView(Context context, Listener listener) {
        super(context, "主题设置", listener::onBack, null);
        LinearLayout content = getContent();

        SettingsSectionView themes = new SettingsSectionView(context, "主题");
        String[][] modes = new String[][] {
                {"跟随系统", "自动匹配系统外观", String.valueOf(IconButtonView.MONITOR)},
                {"亮色模式", "浅色主题，适合白天", String.valueOf(IconButtonView.SUN)},
                {"暗色模式", "深色主题，适合夜间", String.valueOf(IconButtonView.MOON)},
                {"咖啡纸", "类似 Claude 的纸张和咖啡色调", String.valueOf(IconButtonView.COFFEE)},
                {"VS Code", "熟悉的编辑器深色蓝调", String.valueOf(IconButtonView.CODE)},
                {"GitHub Dark", "接近 GitHub 的暗色代码界面", String.valueOf(IconButtonView.GIT_BRANCH)},
                {"Gruvbox", "复古暖色终端风格", String.valueOf(IconButtonView.CODE)},
                {"高对比", "黑底高亮，提升辨识度", String.valueOf(IconButtonView.CONTRAST)},
                {"自定义", "编辑并保存自己的颜色主题", String.valueOf(IconButtonView.PAINTBRUSH)},
        };
        for (int i = 0; i < modes.length; i++) {
            themes.addRow(new OptionRowView(context, Integer.parseInt(modes[i][2]), modes[i][0], modes[i][1], i == 2, null), i < modes.length - 1);
        }
        content.addView(themes, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        addCustomHeader(content);
        addStarterPanel(content);
        addPreview(content);
        addSwatches(content);
        addColorEditor(content);
    }

    private void addCustomHeader(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout header = new LinearLayout(context);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        SectionHeaderView title = new SectionHeaderView(context, "自定义颜色");
        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        IconButtonView reset = new IconButtonView(context, IconButtonView.ROTATE_CCW);
        reset.setIconColor(LineTheme.TEXT_SECONDARY);
        reset.setIconSizeDp(34, 15);
        reset.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 17));
        header.addView(reset, new LinearLayout.LayoutParams(LineTheme.dp(context, 34), LineTheme.dp(context, 34)));

        LinearLayout save = new LinearLayout(context);
        save.setOrientation(HORIZONTAL);
        save.setGravity(Gravity.CENTER);
        save.setBackground(LineTheme.rounded(context, LineTheme.ACCENT, 17));
        LineTheme.padding(save, LineTheme.MD, 0, LineTheme.MD, 0);
        IconButtonView saveIcon = new IconButtonView(context, IconButtonView.SAVE);
        saveIcon.setIconColor(LineTheme.TEXT_ON_COLOR);
        saveIcon.setIconSizeDp(15, 15);
        saveIcon.setClickable(false);
        save.addView(saveIcon, new LinearLayout.LayoutParams(LineTheme.dp(context, 15), LineTheme.dp(context, 15)));
        TextView saveText = LineTheme.text(context, "保存", LineTheme.FONT_SM, LineTheme.TEXT_ON_COLOR, Typeface.BOLD);
        LinearLayout.LayoutParams saveTextParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        saveTextParams.leftMargin = LineTheme.dp(context, LineTheme.XS);
        save.addView(saveText, saveTextParams);
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LineTheme.dp(context, 34));
        saveParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        header.addView(save, saveParams);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = LineTheme.dp(context, LineTheme.XL);
        params.bottomMargin = LineTheme.dp(context, LineTheme.SM);
        content.addView(header, params);
    }

    private void addStarterPanel(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout panel = panel(context);
        panel.addView(LineTheme.textMedium(context, "创作起点", LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(3);
        String[] labels = new String[] {"默认", "亮色", "暗色", "咖啡纸", "VS Code", "GitHub", "Gruvbox", "高对比"};
        int[][] triples = new int[][] {
                {LineTheme.BG, LineTheme.AI_BUBBLE, LineTheme.ACCENT},
                {Color.parseColor("#FBFBFD"), Color.parseColor("#FFFFFF"), Color.parseColor("#0A84FF")},
                {LineTheme.BG, LineTheme.AI_BUBBLE, LineTheme.ACCENT},
                {Color.parseColor("#F4EFE6"), Color.parseColor("#EFE4D4"), Color.parseColor("#B86F50")},
                {Color.parseColor("#1E1E1E"), Color.parseColor("#252526"), Color.parseColor("#007ACC")},
                {Color.parseColor("#0D1117"), Color.parseColor("#161B22"), Color.parseColor("#2F81F7")},
                {Color.parseColor("#282828"), Color.parseColor("#3C3836"), Color.parseColor("#FABD2F")},
                {Color.BLACK, Color.parseColor("#111111"), Color.parseColor("#64D2FF")},
        };
        for (int i = 0; i < labels.length; i++) {
            grid.addView(starterButton(context, labels[i], triples[i], i == 0));
        }
        LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        gridParams.topMargin = LineTheme.dp(context, LineTheme.SM);
        panel.addView(grid, gridParams);
        addPanel(content, panel);
    }

    private View starterButton(Context context, String label, int[] colors, boolean active) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setBackground(LineTheme.roundedStroke(context, active ? LineTheme.ACCENT_MUTED : LineTheme.SURFACE, 8, active ? LineTheme.ACCENT : LineTheme.BORDER_LIGHT));
        LineTheme.padding(button, LineTheme.SM, LineTheme.SM, LineTheme.SM, LineTheme.SM);
        LinearLayout chips = new LinearLayout(context);
        chips.setOrientation(HORIZONTAL);
        for (int i = 0; i < colors.length; i++) {
            View chip = new View(context);
            chip.setBackground(LineTheme.roundedStroke(context, colors[i], 9, Color.argb(32, 0, 0, 0)));
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(LineTheme.dp(context, 18), LineTheme.dp(context, 18));
            if (i > 0) chipParams.leftMargin = LineTheme.dp(context, -4);
            chips.addView(chip, chipParams);
        }
        button.addView(chips, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        TextView text = LineTheme.text(context, label, LineTheme.FONT_XS, active ? LineTheme.ACCENT : LineTheme.TEXT_SECONDARY, Typeface.BOLD);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.topMargin = LineTheme.dp(context, 6);
        button.addView(text, textParams);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(LineTheme.dp(context, 0), LineTheme.dp(context, LineTheme.SM), LineTheme.dp(context, LineTheme.SM), 0);
        button.setLayoutParams(params);
        return button;
    }

    private void addPreview(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout preview = panel(context);
        preview.setBackground(LineTheme.roundedStroke(context, LineTheme.BG, 12, LineTheme.BORDER));
        LinearLayout bubble = new LinearLayout(context);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setBackground(LineTheme.rounded(context, LineTheme.AI_BUBBLE, 8));
        LineTheme.padding(bubble, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        bubble.addView(LineTheme.text(context, "主题预览", LineTheme.FONT_MD, LineTheme.TEXT, Typeface.BOLD));
        TextView sub = LineTheme.text(context, "保存后会应用到自定义主题。", LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY, Typeface.NORMAL);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subParams.topMargin = LineTheme.dp(context, 4);
        bubble.addView(sub, subParams);
        preview.addView(bubble, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView pill = LineTheme.text(context, "Accent", LineTheme.FONT_XS, LineTheme.TEXT_ON_COLOR, Typeface.BOLD);
        pill.setGravity(Gravity.CENTER);
        pill.setBackground(LineTheme.rounded(context, LineTheme.ACCENT, 999));
        LineTheme.padding(pill, LineTheme.MD, LineTheme.XS, LineTheme.MD, LineTheme.XS);
        LinearLayout.LayoutParams pillParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pillParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        preview.addView(pill, pillParams);
        addPanel(content, preview);
    }

    private void addSwatches(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout panel = panel(context);
        panel.addView(LineTheme.textMedium(context, "当前编辑：强调色", LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(7);
        for (int i = 0; i < SWATCHES.length; i++) {
            FrameLayout swatch = new FrameLayout(context);
            swatch.setBackground(LineTheme.roundedStroke(context, SWATCHES[i], 17, i == 26 ? LineTheme.ACCENT : LineTheme.BORDER_LIGHT));
            if (i == 26) {
                IconButtonView check = new IconButtonView(context, IconButtonView.CHECK);
                check.setIconColor(LineTheme.TEXT_ON_COLOR);
                check.setIconSizeDp(14, 14);
                check.setClickable(false);
                swatch.addView(check, new FrameLayout.LayoutParams(LineTheme.dp(context, 14), LineTheme.dp(context, 14), Gravity.CENTER));
            }
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = LineTheme.dp(context, 34);
            params.height = LineTheme.dp(context, 34);
            params.setMargins(0, LineTheme.dp(context, LineTheme.SM), LineTheme.dp(context, LineTheme.SM), 0);
            grid.addView(swatch, params);
        }
        panel.addView(grid, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addPanel(content, panel);
    }

    private void addColorEditor(LinearLayout content) {
        Context context = content.getContext();
        LinearLayout group = new LinearLayout(context);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 12));
        String[][] fields = new String[][] {
                {"背景", "页面底色", "#000000"},
                {"面板", "卡片和弹层背景", "#141414"},
                {"浅面板", "按钮和次级区域", "#1C1C1E"},
                {"输入框", "输入栏背景", "#1C1C1E"},
                {"正文", "主要文字", "#FFFFFF"},
                {"次级文字", "说明文字", "#8E8E93"},
                {"弱文字", "占位和辅助文字", "#636366"},
                {"强调色", "选中、按钮、链接", "#30D158"},
        };
        for (int i = 0; i < fields.length; i++) {
            group.addView(colorRow(context, fields[i][0], fields[i][1], fields[i][2], i == 7), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = LineTheme.dp(context, LineTheme.LG);
        params.rightMargin = LineTheme.dp(context, LineTheme.LG);
        params.topMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(group, params);
    }

    private View colorRow(Context context, String label, String desc, String value, boolean active) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(LineTheme.dp(context, 66));
        if (active) row.setBackgroundColor(LineTheme.ACCENT_MUTED);
        LineTheme.padding(row, LineTheme.MD, 0, LineTheme.MD, 0);
        View preview = new View(context);
        preview.setBackground(LineTheme.roundedStroke(context, Color.parseColor(value), 15, LineTheme.BORDER_LIGHT));
        row.addView(preview, new LinearLayout.LayoutParams(LineTheme.dp(context, 30), LineTheme.dp(context, 30)));
        LinearLayout meta = new LinearLayout(context);
        meta.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        metaParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
        row.addView(meta, metaParams);
        meta.addView(LineTheme.textMedium(context, label, LineTheme.FONT_MD, LineTheme.TEXT));
        TextView descView = LineTheme.text(context, desc, LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, 2);
        meta.addView(descView, descParams);
        EditText input = new EditText(context);
        input.setText(value);
        input.setTextColor(LineTheme.TEXT);
        input.setTextSize(LineTheme.FONT_SM);
        input.setSingleLine(true);
        input.setTypeface(Typeface.MONOSPACE);
        input.setBackground(LineTheme.roundedStroke(context, LineTheme.SURFACE_LIGHT, 8, LineTheme.BORDER_LIGHT));
        input.setPadding(LineTheme.dp(context, LineTheme.SM), 0, LineTheme.dp(context, LineTheme.SM), 0);
        row.addView(input, new LinearLayout.LayoutParams(LineTheme.dp(context, 92), LineTheme.dp(context, 38)));
        return row;
    }

    private LinearLayout panel(Context context) {
        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 12));
        LineTheme.padding(panel, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        return panel;
    }

    private void addPanel(LinearLayout content, LinearLayout panel) {
        Context context = content.getContext();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.leftMargin = LineTheme.dp(context, LineTheme.LG);
        params.rightMargin = LineTheme.dp(context, LineTheme.LG);
        params.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(panel, params);
    }
}
