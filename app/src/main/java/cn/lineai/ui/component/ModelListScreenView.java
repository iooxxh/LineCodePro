package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.lineai.model.ModelConfig;
import cn.lineai.ui.theme.LineTheme;
import java.util.List;

public final class ModelListScreenView extends LinearLayout {
    public interface Listener {
        void onBack();

        void onAddModel();

        void onSelectModel(String id);
    }

    public ModelListScreenView(Context context, List<ModelConfig> models, String selectedModelId, Listener listener) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(LineTheme.BG);

        IconButtonView add = new IconButtonView(context, IconButtonView.PLUS);
        add.setIconColor(LineTheme.TEXT);
        add.setIconSizeDp(36, 20);
        add.setOnClickListener(v -> listener.onAddModel());
        addView(new ScreenHeaderView(context, "模型", listener::onBack, add), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(VERTICAL);
        LineTheme.padding(list, LineTheme.LG, LineTheme.LG, LineTheme.LG, 100);
        scrollView.addView(list, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        if (models == null || models.isEmpty()) {
            TextView empty = LineTheme.text(context, "还没有模型。点击右上角 + 添加 OpenAI 兼容、Codex 或 Anthropic 模型。", LineTheme.FONT_SM, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
            empty.setLineSpacing(LineTheme.dp(context, 3), 1f);
            list.addView(empty, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return;
        }

        for (ModelConfig model : models) {
            addModel(list, model, selectedModelId != null && selectedModelId.equals(model.getId()), listener);
        }
    }

    private void addModel(LinearLayout list, ModelConfig model, boolean selected, Listener listener) {
        Context context = list.getContext();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setClickable(true);
        card.setOnClickListener(v -> listener.onSelectModel(model.getId()));
        card.setBackground(LineTheme.roundedStroke(context, LineTheme.BG, 12, selected ? LineTheme.ACCENT : Color.TRANSPARENT));
        LineTheme.padding(card, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = LineTheme.dp(context, LineTheme.SM);
        list.addView(card, cardParams);

        String provider = model.getProtocolType().getLabel();
        TextView badge = LineTheme.text(context, provider, LineTheme.FONT_XS, LineTheme.TEXT_ON_COLOR, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(LineTheme.rounded(context, badgeColor(model), 8));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        badgeParams.rightMargin = LineTheme.dp(context, LineTheme.MD);
        LineTheme.padding(badge, LineTheme.SM, 4, LineTheme.SM, 4);
        card.addView(badge, badgeParams);

        LinearLayout info = new LinearLayout(context);
        info.setOrientation(VERTICAL);
        card.addView(info, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView title = LineTheme.textMedium(context, model.getName(), LineTheme.FONT_MD, LineTheme.TEXT);
        title.setSingleLine(true);
        info.addView(title, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView sub = LineTheme.text(context, model.getModelId() + " · " + model.getBaseUrl(), LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        sub.setSingleLine(true);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        subParams.topMargin = LineTheme.dp(context, 2);
        info.addView(sub, subParams);

        if (selected) {
            View dot = new View(context);
            dot.setBackground(LineTheme.rounded(context, LineTheme.ACCENT, 4));
            LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(LineTheme.dp(context, 8), LineTheme.dp(context, 8));
            dotParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
            card.addView(dot, dotParams);
        }
    }

    private int badgeColor(ModelConfig model) {
        switch (model.getProtocolType()) {
            case CODEX_RESPONSES:
                return Color.parseColor("#4B8BFF");
            case ANTHROPIC_MESSAGES:
                return Color.parseColor("#B86F50");
            case LOCAL_GGUF:
                return Color.parseColor("#2E7D62");
            case OPENAI_COMPATIBLE:
            default:
                return Color.parseColor("#10A37F");
        }
    }
}
