package cn.lineai.ui.component;

import android.content.Context;
import android.widget.LinearLayout;

public final class OutputSettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public OutputSettingsScreenView(Context context, Listener listener) {
        super(context, "输出与浏览", listener::onBack, null);
        LinearLayout content = getContent();

        SettingsSectionView display = new SettingsSectionView(context, "回复布局");
        display.addRow(new OptionRowView(context, IconButtonView.MONITOR, "全屏模式", "AI 回复占满宽度，适合阅读代码", true, null), true);
        display.addRow(new OptionRowView(context, IconButtonView.MESSAGE_CIRCLE, "气泡模式", "传统聊天气泡样式", false, null), false);
        content.addView(display, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView code = new SettingsSectionView(context, "代码显示");
        code.addRow(new SwitchRowView(context, IconButtonView.SCROLL_TEXT, "代码自动换行", "关闭时代码可水平滚动", false), false);
        content.addView(code, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView browser = new SettingsSectionView(context, "网页打开方式");
        browser.addRow(new OptionRowView(context, IconButtonView.GLOBE, "内置浏览器", "在应用内打开网页", true, null), true);
        browser.addRow(new OptionRowView(context, IconButtonView.EXTERNAL_LINK, "外部浏览器", "使用系统浏览器打开", false, null), false);
        content.addView(browser, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
}
