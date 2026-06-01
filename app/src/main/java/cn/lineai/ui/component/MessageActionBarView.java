package cn.lineai.ui.component;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import cn.lineai.ui.theme.LineTheme;

public final class MessageActionBarView extends LinearLayout {
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;

    public MessageActionBarView(Context context, int align, boolean recallEnabled) {
        super(context);
        setOrientation(HORIZONTAL);
        setGravity(align == ALIGN_RIGHT ? Gravity.RIGHT : Gravity.LEFT);
        setMinimumHeight(LineTheme.dp(context, 22));

        IconButtonView copy = icon(context, IconButtonView.COPY);
        addView(copy, iconParams(context));

        if (recallEnabled) {
            IconButtonView recall = icon(context, IconButtonView.ROTATE_CCW);
            addView(recall, iconParams(context));
        }
    }

    private IconButtonView icon(Context context, int type) {
        IconButtonView icon = new IconButtonView(context, type);
        icon.setIconColor(LineTheme.TEXT_TERTIARY);
        icon.setIconPaddingDp(4, 3, 5, 4);
        icon.setClickable(true);
        return icon;
    }

    private LinearLayout.LayoutParams iconParams(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LineTheme.dp(context, 24), LineTheme.dp(context, 22));
        params.rightMargin = LineTheme.dp(context, LineTheme.XS);
        return params;
    }
}
