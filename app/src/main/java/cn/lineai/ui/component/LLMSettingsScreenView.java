package cn.lineai.ui.component;

import android.content.Context;
import android.widget.LinearLayout;

public final class LLMSettingsScreenView extends ScreenScaffoldView {
    public interface Listener {
        void onBack();
    }

    public LLMSettingsScreenView(Context context, Listener listener) {
        super(context, "AI 行为", listener::onBack, null);
        LinearLayout content = getContent();

        SettingsSectionView reasoning = new SettingsSectionView(context, "思考深度");
        String[][] efforts = new String[][] {
                {"关闭", "不使用 reasoning，最快响应"},
                {"低", "轻量推理，适合普通问答"},
                {"中", "默认强度，兼顾速度和质量"},
                {"高", "复杂代码任务使用更深思考"},
                {"最大", "尽可能充分推理，耗时更长"},
        };
        for (int i = 0; i < efforts.length; i++) {
            reasoning.addRow(new OptionRowView(context, IconButtonView.SPARKLES, efforts[i][0], efforts[i][1], i == 2, null), i < efforts.length - 1);
        }
        content.addView(reasoning, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView learning = new SettingsSectionView(context, "学习与记忆");
        learning.addRow(new SwitchRowView(context, IconButtonView.BRAIN, "学习模式", "启用自动 Skills、长期记忆、项目记忆、短期记忆和聊天记录检索", false), false);
        content.addView(learning, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView tone = new SettingsSectionView(context, "交流语气");
        tone.addRow(new OptionRowView(context, IconButtonView.ZAP, "编程模式", "严谨专业，代码优先，不使用 emoji", true, null), true);
        tone.addRow(new OptionRowView(context, IconButtonView.SMILE, "聊天模式", "亲切温柔，像朋友聊天，可以使用 emoji", false, null), false);
        content.addView(tone, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        SettingsSectionView thinking = new SettingsSectionView(context, "思考过程");
        thinking.addRow(new SwitchRowView(context, IconButtonView.SCROLL_TEXT, "滚动显示", "关闭后直接完全展开显示", true), true);
        thinking.addRow(new SwitchRowView(context, IconButtonView.EXPAND, "自动展开", "收到思考内容时自动展开", false), true);
        thinking.addRow(new SwitchRowView(context, IconButtonView.BRAIN, "保留完整 reasoning", "将历史思考发回兼容模型，适合多轮工具调用", false), false);
        content.addView(thinking, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
}
