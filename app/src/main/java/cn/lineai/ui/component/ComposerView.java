package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.model.ChatUiState;
import cn.lineai.ui.theme.LineTheme;

public final class ComposerView extends LinearLayout {
    public interface Listener {
        void onSend(String text);

        void onStop();
    }

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextView modelText;
    private final TextView contextText;
    private final EditText input;
    private final IconButtonView sendButton;
    private boolean streaming;
    private Listener listener;

    public ComposerView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(LineTheme.BG);
        setWillNotDraw(false);
        LineTheme.padding(this, LineTheme.LG, LineTheme.SM, LineTheme.LG, LineTheme.LG);

        LinearLayout panel = new LinearLayout(context);
        panel.setOrientation(VERTICAL);
        panel.setMinimumHeight(LineTheme.dp(context, 96));
        panel.setBackground(LineTheme.roundedStroke(context, LineTheme.INPUT_BG, 22, LineTheme.BORDER));
        addView(panel, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout metaRow = new LinearLayout(context);
        metaRow.setOrientation(HORIZONTAL);
        metaRow.setGravity(Gravity.CENTER_VERTICAL);
        LineTheme.padding(metaRow, LineTheme.LG, 0, LineTheme.LG, 0);
        panel.addView(metaRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LineTheme.dp(context, 34)));

        modelText = LineTheme.textMedium(context, "", LineTheme.FONT_XS, LineTheme.TEXT_SECONDARY);
        modelText.setSingleLine(true);
        metaRow.addView(modelText, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        contextText = LineTheme.text(context, "", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.BOLD);
        LinearLayout.LayoutParams contextParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        contextParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
        metaRow.addView(contextText, contextParams);

        android.view.View divider = new android.view.View(context);
        divider.setBackgroundColor(LineTheme.BORDER_LIGHT);
        panel.addView(divider, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));

        LinearLayout inputRow = new LinearLayout(context);
        inputRow.setOrientation(HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        LineTheme.padding(inputRow, LineTheme.SM, LineTheme.SM, LineTheme.SM, LineTheme.SM);
        panel.addView(inputRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        IconButtonView attach = new IconButtonView(context, IconButtonView.PLUS);
        attach.setIconColor(LineTheme.TEXT_SECONDARY);
        attach.setIconSizeDp(40, 22);
        attach.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 20));
        inputRow.addView(attach, new LinearLayout.LayoutParams(LineTheme.dp(context, 40), LineTheme.dp(context, 40)));

        input = new EditText(context);
        input.setTextColor(LineTheme.TEXT);
        input.setHintTextColor(LineTheme.TEXT_TERTIARY);
        input.setHint("输入消息...");
        input.setTextSize(LineTheme.FONT_MD);
        input.setSingleLine(false);
        input.setMinLines(1);
        input.setMaxLines(4);
        input.setMinHeight(LineTheme.dp(context, 42));
        input.setMaxHeight(LineTheme.dp(context, 100));
        input.setGravity(Gravity.CENTER_VERTICAL);
        input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        input.setIncludeFontPadding(false);
        input.setPadding(LineTheme.dp(context, LineTheme.SM), 0, LineTheme.dp(context, LineTheme.SM), 0);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        inputParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        inputParams.rightMargin = LineTheme.dp(context, LineTheme.SM);
        inputRow.addView(input, inputParams);

        sendButton = new IconButtonView(context, IconButtonView.ARROW_UP);
        sendButton.setIconSizeDp(40, 22);
        sendButton.setOnClickListener(v -> {
            if (streaming) {
                if (listener != null) {
                    listener.onStop();
                }
                return;
            }
            String value = input.getText().toString();
            if (value.trim().isEmpty()) {
                return;
            }
            if (listener != null) {
                listener.onSend(value);
            }
            input.setText("");
        });
        inputRow.addView(sendButton, new LinearLayout.LayoutParams(LineTheme.dp(context, 40), LineTheme.dp(context, 40)));

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        updateSendButton();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void render(ChatUiState state) {
        streaming = state.isStreaming();
        modelText.setText(state.getModelLabel());
        contextText.setText(state.getContextLabel());
        contextText.setTextColor(state.getContextPercent() >= 80 ? LineTheme.WARNING : LineTheme.TEXT_TERTIARY);
        input.setEnabled(!streaming);
        input.setHint(state.hasConfiguredModel() ? "输入消息..." : "请先到设置 → 模型管理配置模型");
        updateSendButton();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        borderPaint.setColor(LineTheme.BORDER);
        borderPaint.setStrokeWidth(1f);
        canvas.drawLine(0, 0, getWidth(), 0, borderPaint);
    }

    private void updateSendButton() {
        boolean hasText = input.getText().toString().trim().length() > 0;
        if (streaming) {
            sendButton.setIconType(IconButtonView.STOP);
            sendButton.setIconColor(LineTheme.TEXT_ON_COLOR);
            sendButton.setIconSizeDp(40, 18);
            sendButton.setBackground(LineTheme.rounded(getContext(), LineTheme.DANGER, 20));
        } else {
            sendButton.setIconType(IconButtonView.ARROW_UP);
            sendButton.setIconColor(hasText ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_TERTIARY);
            sendButton.setIconSizeDp(40, 22);
            sendButton.setBackground(LineTheme.rounded(getContext(), hasText ? LineTheme.ACCENT : LineTheme.SURFACE_LIGHT, 20));
        }
        sendButton.setEnabled(streaming || hasText);
        sendButton.setAlpha(sendButton.isEnabled() ? 1f : 0.72f);
    }
}
