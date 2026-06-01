package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class DebugSettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public DebugSettingsScreenView(Context context, Listener listener) {
        super(context, "调试模式", listener::onBack, null);
        LinearLayout content = getContent();

        SettingsSectionView actions = new SettingsSectionView(context, "错误处理器测试");
        actions.addRow(new ActionRowView(context, IconButtonView.BUG, "直接上报 Error", "调用 ErrorReporter.report，验证报告创建、持久化和错误报告页。", false, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.FLASK_CONICAL, "React 渲染期抛错", "组件 render 阶段 throw，验证 AppErrorBoundary 是否接管。", true, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.ZAP, "事件回调抛 TypeError", "在按钮确认回调里直接 throw，验证全局 JS 异常处理。", true, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.CLOCK_3, "异步 setTimeout 抛错", "在定时器回调中 throw RangeError，验证异步全局异常处理。", true, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.FILE_TEXT, "未处理 Promise 拒绝", "触发 Promise.reject，验证 promise rejection tracker。", true, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.TERMINAL, "调用 ErrorUtils 全局处理器", "直接调用当前全局 handler，验证 fatal 标记和原始 handler 委托链。", true, false, null), true);
        actions.addRow(new ActionRowView(context, IconButtonView.TERMINAL, "console.error 来源", "生产环境走 console.error 捕获；开发环境会额外模拟 console 来源报告。", false, false, null), false);
        content.addView(actions, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        SectionHeaderView title = new SectionHeaderView(context, "最近错误记录");
        titleRow.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        IconButtonView refresh = new IconButtonView(context, IconButtonView.REFRESH_CW);
        refresh.setIconColor(LineTheme.TEXT_SECONDARY);
        refresh.setIconSizeDp(34, 18);
        titleRow.addView(refresh, new LinearLayout.LayoutParams(LineTheme.dp(context, 34), LineTheme.dp(context, 34)));
        IconButtonView trash = new IconButtonView(context, IconButtonView.TRASH_2);
        trash.setIconColor(LineTheme.DANGER);
        trash.setIconSizeDp(34, 18);
        titleRow.addView(trash, new LinearLayout.LayoutParams(LineTheme.dp(context, 34), LineTheme.dp(context, 34)));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = LineTheme.dp(context, LineTheme.XL);
        titleParams.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        content.addView(titleRow, titleParams);

        LinearLayout empty = new LinearLayout(context);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 12));
        LineTheme.padding(empty, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
        empty.addView(LineTheme.textMedium(context, "暂无错误记录", LineTheme.FONT_MD, LineTheme.TEXT));
        TextView desc = LineTheme.text(context, "触发测试后，错误报告会保存在这里。", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, LineTheme.XS);
        empty.addView(desc, descParams);
        LinearLayout.LayoutParams emptyParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        emptyParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        emptyParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        content.addView(empty, emptyParams);
    }
}
