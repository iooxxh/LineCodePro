package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.Gravity;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class InAppBrowserScreenView extends LinearLayout {
    public interface Listener {
        void onBack();
    }

    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public InAppBrowserScreenView(Context context, String url, Listener listener) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(LineTheme.BG);

        LinearLayout header = new LinearLayout(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                borderPaint.setColor(LineTheme.BORDER);
                borderPaint.setStrokeWidth(1f);
                canvas.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1, borderPaint);
            }
        };
        header.setWillNotDraw(false);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(LineTheme.SURFACE_ELEVATED);
        LineTheme.padding(header, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);

        LinearLayout back = new LinearLayout(context);
        back.setOrientation(HORIZONTAL);
        back.setGravity(Gravity.CENTER_VERTICAL);
        back.setOnClickListener(v -> listener.onBack());
        IconButtonView chevron = new IconButtonView(context, IconButtonView.CHEVRON_LEFT);
        chevron.setIconColor(LineTheme.TEXT);
        chevron.setIconSizeDp(22, 22);
        chevron.setClickable(false);
        back.addView(chevron, new LinearLayout.LayoutParams(LineTheme.dp(context, 22), LineTheme.dp(context, 22)));
        back.addView(LineTheme.text(context, "退出", LineTheme.FONT_MD, LineTheme.TEXT, Typeface.NORMAL));
        header.addView(back, new LinearLayout.LayoutParams(LineTheme.dp(context, 56), LayoutParams.WRAP_CONTENT));

        TextView title = LineTheme.textMedium(context, url == null ? "网页" : url, LineTheme.FONT_MD, LineTheme.TEXT);
        title.setGravity(Gravity.CENTER);
        title.setSingleLine(true);
        header.addView(title, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        header.addView(new LinearLayout(context), new LinearLayout.LayoutParams(LineTheme.dp(context, 56), 1));
        addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        WebView webView = new WebView(context);
        webView.setBackgroundColor(LineTheme.BG);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        if (url != null && url.length() > 0) {
            webView.loadUrl(url);
        }
        addView(webView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
    }
}
