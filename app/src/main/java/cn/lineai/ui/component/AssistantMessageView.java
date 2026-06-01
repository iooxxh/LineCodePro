package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.model.ChatMessage;
import cn.lineai.ui.theme.LineTheme;

public final class AssistantMessageView extends LinearLayout {
    private final ThinkingBlockView thinkingBlockView;
    private final TextView contentText;
    private final MessageActionBarView actionBar;

    public AssistantMessageView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.LEFT);
        LineTheme.padding(this, LineTheme.LG, 0, LineTheme.LG, LineTheme.MD);

        thinkingBlockView = new ThinkingBlockView(context);
        LinearLayout.LayoutParams thinkingParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        thinkingParams.bottomMargin = LineTheme.dp(context, LineTheme.SM);
        addView(thinkingBlockView, thinkingParams);

        contentText = LineTheme.text(context, "", 16, LineTheme.TEXT, Typeface.NORMAL);
        contentText.setLineSpacing(LineTheme.dp(context, 4), 1.0f);
        addView(contentText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        actionBar = new MessageActionBarView(context, MessageActionBarView.ALIGN_LEFT, false);
        LinearLayout.LayoutParams actionParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LineTheme.dp(context, 22));
        actionParams.topMargin = LineTheme.dp(context, 3);
        addView(actionBar, actionParams);
    }

    public void bind(ChatMessage message) {
        String reasoning = message.getReasoningContent();
        if (reasoning != null && reasoning.trim().length() > 0) {
            thinkingBlockView.setVisibility(VISIBLE);
            thinkingBlockView.bind(message.getId(), reasoning, message.isStreaming());
        } else {
            thinkingBlockView.setVisibility(GONE);
        }
        contentText.setText(message.isStreaming() && message.getContent().length() == 0 && thinkingBlockView.getVisibility() == GONE ? "..." : message.getContent());
        actionBar.setVisibility(message.isStreaming() || message.getContent().trim().isEmpty() ? GONE : VISIBLE);
    }
}
