package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.lineai.model.ChatMessage;
import cn.lineai.model.ChatUiState;
import cn.lineai.ui.theme.LineTheme;
import java.util.List;

public final class ChatMessageListView extends ScrollView {
    private final LinearLayout stack;

    public ChatMessageListView(Context context) {
        super(context);
        setFillViewport(true);
        setClipToPadding(false);
        setBackgroundColor(LineTheme.BG);

        stack = new LinearLayout(context);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setGravity(Gravity.TOP);
        LineTheme.padding(stack, 0, LineTheme.SM, 0, LineTheme.SM);
        addView(stack, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void render(ChatUiState state) {
        stack.removeAllViews();
        List<ChatMessage> messages = state.getMessages();
        if (messages.isEmpty()) {
            if (!state.hasConfiguredModel()) {
                stack.addView(createConfigureState(getContext()), new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
            }
        } else {
            for (ChatMessage message : messages) {
                if (message.isHidden()
                        || message.getRole() == ChatMessage.Role.SYSTEM
                        || message.getRole() == ChatMessage.Role.TOOL) {
                    continue;
                }
                View row;
                if (message.getRole() == ChatMessage.Role.USER) {
                    UserMessageView user = new UserMessageView(getContext());
                    user.bind(message);
                    row = user;
                } else {
                    AssistantMessageView assistant = new AssistantMessageView(getContext());
                    assistant.bind(message);
                    row = assistant;
                }
                stack.addView(row, new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
            }
        }
        post(() -> fullScroll(FOCUS_DOWN));
    }

    private View createConfigureState(Context context) {
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        LineTheme.padding(box, LineTheme.XL, 80, LineTheme.XL, 80);

        TextView title = LineTheme.text(context, "请先配置模型", LineTheme.FONT_XL, LineTheme.TEXT, Typeface.BOLD);
        box.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        TextView desc = LineTheme.text(context,
                "进入 设置 → 模型管理 → 添加模型，保存后再发送消息。",
                LineTheme.FONT_MD,
                LineTheme.TEXT_SECONDARY,
                Typeface.NORMAL);
        desc.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = LineTheme.dp(context, LineTheme.MD);
        box.addView(desc, descParams);
        return box;
    }
}
