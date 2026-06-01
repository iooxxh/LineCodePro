package cn.lineai.ui.component;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.lineai.ui.theme.LineTheme;

public final class DrawerView extends FrameLayout {
    public interface Listener {
        void onCloseDrawer();

        void onNewConversation();
    }

    private static final int TAB_CONVERSATIONS = 0;
    private static final int TAB_FILES = 1;

    private final View backdrop;
    private final LinearLayout sidebar;
    private final TextView headerTitle;
    private final LinearLayout headerActions;
    private final LinearLayout body;
    private final LinearLayout tabs;
    private Listener listener;
    private int activeTab = TAB_CONVERSATIONS;

    public DrawerView(Context context) {
        super(context);
        setVisibility(GONE);
        setClickable(true);

        backdrop = new View(context);
        backdrop.setBackgroundColor(LineTheme.OVERLAY);
        backdrop.setOnClickListener(v -> close());
        addView(backdrop, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        sidebar = new LinearLayout(context);
        sidebar.setOrientation(LinearLayout.VERTICAL);
        sidebar.setBackgroundColor(LineTheme.SURFACE_ELEVATED);
        FrameLayout.LayoutParams sidebarParams = new FrameLayout.LayoutParams(LineTheme.dp(context, 300), LayoutParams.MATCH_PARENT);
        sidebarParams.gravity = Gravity.START;
        addView(sidebar, sidebarParams);

        LinearLayout header = new LinearLayout(context);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LineTheme.padding(header, LineTheme.LG, 50, LineTheme.LG, LineTheme.MD);
        sidebar.addView(header, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        headerTitle = LineTheme.text(context, "对话历史", LineTheme.FONT_LG, LineTheme.TEXT, Typeface.BOLD);
        header.addView(headerTitle, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        headerActions = new LinearLayout(context);
        headerActions.setOrientation(LinearLayout.HORIZONTAL);
        headerActions.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(headerActions, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        tabs = new LinearLayout(context);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 8));
        LineTheme.padding(tabs, 2, 2, 2, 2);
        LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        tabParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        tabParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        tabParams.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        sidebar.addView(tabs, tabParams);

        body = new LinearLayout(context);
        body.setOrientation(LinearLayout.VERTICAL);
        sidebar.addView(body, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        renderChrome();
        renderBody();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void open() {
        if (getVisibility() == VISIBLE) {
            return;
        }
        setVisibility(VISIBLE);
        bringToFront();
        sidebar.setTranslationX(-LineTheme.dp(getContext(), 300));
        backdrop.setAlpha(0f);
        sidebar.animate().translationX(0).setDuration(180).start();
        backdrop.animate().alpha(1f).setDuration(200).start();
    }

    public void close() {
        if (getVisibility() != VISIBLE) {
            return;
        }
        sidebar.animate().translationX(-LineTheme.dp(getContext(), 300)).setDuration(180).start();
        backdrop.animate().alpha(0f).setDuration(150).withEndAction(() -> {
            setVisibility(GONE);
            if (listener != null) {
                listener.onCloseDrawer();
            }
        }).start();
    }

    private void setActiveTab(int tab) {
        if (activeTab == tab) {
            return;
        }
        activeTab = tab;
        renderChrome();
        renderBody();
    }

    private void renderChrome() {
        Context context = getContext();
        headerTitle.setText(activeTab == TAB_CONVERSATIONS ? "对话历史" : "文件管理器");
        headerActions.removeAllViews();

        if (activeTab == TAB_FILES) {
            headerActions.addView(headerIcon(context, IconButtonView.FOLDER_OPEN, LineTheme.ACCENT, 16));
            headerActions.addView(headerIcon(context, IconButtonView.FOLDER_PLUS, LineTheme.ACCENT, 16));
            headerActions.addView(headerIcon(context, IconButtonView.ARCHIVE, LineTheme.ACCENT, 16));
        }
        IconButtonView close = headerIcon(context, IconButtonView.CLOSE, LineTheme.TEXT, 20);
        close.setOnClickListener(v -> close());
        headerActions.addView(close);

        tabs.removeAllViews();
        tabs.addView(tabButton(context, IconButtonView.MESSAGE_SQUARE, "对话", activeTab == TAB_CONVERSATIONS, () -> setActiveTab(TAB_CONVERSATIONS)),
                new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        tabs.addView(tabButton(context, IconButtonView.FOLDER_OPEN, "文件", activeTab == TAB_FILES, () -> setActiveTab(TAB_FILES)),
                new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
    }

    private void renderBody() {
        body.removeAllViews();
        if (activeTab == TAB_CONVERSATIONS) {
            renderConversations();
        } else {
            renderFiles();
        }
    }

    private void renderConversations() {
        Context context = getContext();
        LinearLayout newButton = new LinearLayout(context);
        newButton.setOrientation(LinearLayout.HORIZONTAL);
        newButton.setGravity(Gravity.CENTER);
        newButton.setBackground(LineTheme.rounded(context, LineTheme.ACCENT, 12));
        newButton.setClickable(true);
        newButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNewConversation();
            }
            close();
        });
        IconButtonView plus = inlineIcon(context, IconButtonView.PLUS, android.graphics.Color.BLACK, 18);
        newButton.addView(plus, new LinearLayout.LayoutParams(LineTheme.dp(context, 18), LineTheme.dp(context, 18)));
        TextView label = LineTheme.text(context, "新建对话", LineTheme.FONT_MD, android.graphics.Color.BLACK, Typeface.BOLD);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        newButton.addView(label, labelParams);
        LinearLayout.LayoutParams newParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LineTheme.dp(context, 45));
        newParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        newParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        newParams.bottomMargin = LineTheme.dp(context, LineTheme.MD);
        body.addView(newButton, newParams);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        LineTheme.padding(list, LineTheme.SM, 0, LineTheme.SM, 0);
        scrollView.addView(list, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        body.addView(scrollView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        addConversationItem(list, "Java 原生 UI 重写", "6/1 18:28", true);
        addConversationItem(list, "LineAI 结构分析", "6/1 17:54", false);
        addConversationItem(list, "模型与权限设置", "5/31 23:10", false);
        addConversationItem(list, "本地文件工具", "5/31 19:42", false);
    }

    private void renderFiles() {
        Context context = getContext();
        LinearLayout strip = new LinearLayout(context);
        strip.setOrientation(LinearLayout.VERTICAL);
        strip.setBackground(LineTheme.roundedStroke(context, LineTheme.SURFACE_LIGHT, 8, LineTheme.BORDER_LIGHT));
        LineTheme.padding(strip, LineTheme.SM, LineTheme.SM, LineTheme.SM, LineTheme.SM);

        TextView project = LineTheme.text(context, "LineCode", LineTheme.FONT_SM, LineTheme.TEXT, Typeface.BOLD);
        strip.addView(project, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView path = LineTheme.text(context, "/home/LangLang/AndroidStudioProjects/LineCode", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        path.setSingleLine(true);
        LinearLayout.LayoutParams pathParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pathParams.topMargin = LineTheme.dp(context, 2);
        strip.addView(path, pathParams);

        LinearLayout.LayoutParams stripParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        stripParams.leftMargin = LineTheme.dp(context, LineTheme.LG);
        stripParams.rightMargin = LineTheme.dp(context, LineTheme.LG);
        stripParams.bottomMargin = LineTheme.dp(context, LineTheme.SM);
        body.addView(strip, stripParams);

        ScrollView scrollView = new ScrollView(context);
        LinearLayout tree = new LinearLayout(context);
        tree.setOrientation(LinearLayout.VERTICAL);
        LineTheme.padding(tree, LineTheme.SM, LineTheme.SM, LineTheme.SM, 0);
        scrollView.addView(tree, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        body.addView(scrollView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        addFileRow(tree, IconButtonView.FOLDER_OPEN, "LineCode", 0, true, LineTheme.ACCENT, 16);
        addFileRow(tree, IconButtonView.FOLDER, "app", 1, false, LineTheme.TEXT_SECONDARY, 16);
        addFileRow(tree, IconButtonView.FOLDER, "src", 2, false, LineTheme.TEXT_SECONDARY, 16);
        addFileRow(tree, IconButtonView.FILE, "MainActivity.java", 3, false, LineTheme.TEXT_TERTIARY, 14);
        addFileRow(tree, IconButtonView.FILE, "MainChatView.java", 3, false, LineTheme.TEXT_TERTIARY, 14);
        addFileRow(tree, IconButtonView.FOLDER, "res", 2, false, LineTheme.TEXT_SECONDARY, 16);
        addFileRow(tree, IconButtonView.FILE, "styles.xml", 3, false, LineTheme.TEXT_TERTIARY, 14);
    }

    private IconButtonView headerIcon(Context context, int type, int color, int iconSizeDp) {
        IconButtonView icon = new IconButtonView(context, type);
        icon.setIconColor(color);
        icon.setIconSizeDp(32, iconSizeDp);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LineTheme.dp(context, 32), LineTheme.dp(context, 32));
        params.leftMargin = LineTheme.dp(context, LineTheme.SM);
        icon.setLayoutParams(params);
        return icon;
    }

    private View tabButton(Context context, int iconType, String text, boolean active, Runnable onClick) {
        LinearLayout tab = new LinearLayout(context);
        tab.setOrientation(LinearLayout.HORIZONTAL);
        tab.setGravity(Gravity.CENTER);
        tab.setBackground(active ? LineTheme.rounded(context, LineTheme.SURFACE_ELEVATED, 6) : null);
        tab.setClickable(true);
        tab.setOnClickListener(v -> onClick.run());
        LineTheme.padding(tab, 0, LineTheme.SM, 0, LineTheme.SM);

        int color = active ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY;
        IconButtonView icon = inlineIcon(context, iconType, color, 14);
        tab.addView(icon, new LinearLayout.LayoutParams(LineTheme.dp(context, 14), LineTheme.dp(context, 14)));

        TextView label = active
                ? LineTheme.textMedium(context, text, LineTheme.FONT_SM, color)
                : LineTheme.text(context, text, LineTheme.FONT_SM, color, Typeface.NORMAL);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        labelParams.leftMargin = LineTheme.dp(context, 4);
        tab.addView(label, labelParams);
        return tab;
    }

    private IconButtonView inlineIcon(Context context, int type, int color, int size) {
        IconButtonView icon = new IconButtonView(context, type);
        icon.setIconColor(color);
        icon.setClickable(false);
        icon.setFocusable(false);
        icon.setPadding(0, 0, 0, 0);
        icon.setMinimumWidth(0);
        icon.setMinimumHeight(0);
        icon.setMaxWidth(LineTheme.dp(context, size));
        icon.setMaxHeight(LineTheme.dp(context, size));
        return icon;
    }

    private void addConversationItem(LinearLayout list, String title, String time, boolean active) {
        Context context = list.getContext();
        LinearLayout item = new LinearLayout(context);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setBackground(active ? LineTheme.rounded(context, LineTheme.ACCENT_MUTED, 8) : null);
        LineTheme.padding(item, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        list.addView(item, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        FrameLayout iconBox = new FrameLayout(context);
        iconBox.setBackground(LineTheme.rounded(context, LineTheme.SURFACE_LIGHT, 14));
        IconButtonView messageIcon = inlineIcon(context, IconButtonView.MESSAGE_SQUARE, active ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY, 16);
        FrameLayout.LayoutParams messageParams = new FrameLayout.LayoutParams(LineTheme.dp(context, 16), LineTheme.dp(context, 16), Gravity.CENTER);
        iconBox.addView(messageIcon, messageParams);
        item.addView(iconBox, new LinearLayout.LayoutParams(LineTheme.dp(context, 28), LineTheme.dp(context, 28)));

        LinearLayout texts = new LinearLayout(context);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textsParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        textsParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        textsParams.rightMargin = LineTheme.dp(context, LineTheme.SM);
        item.addView(texts, textsParams);

        TextView titleView = active
                ? LineTheme.textMedium(context, title, LineTheme.FONT_SM, LineTheme.ACCENT)
                : LineTheme.text(context, title, LineTheme.FONT_SM, LineTheme.TEXT, Typeface.NORMAL);
        titleView.setSingleLine(true);
        texts.addView(titleView, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView timeView = LineTheme.text(context, time, LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams timeParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        timeParams.topMargin = LineTheme.dp(context, 2);
        texts.addView(timeView, timeParams);

        IconButtonView trash = sizedIcon(context, IconButtonView.TRASH_2, LineTheme.TEXT_TERTIARY, 22, 14);
        item.addView(trash, new LinearLayout.LayoutParams(LineTheme.dp(context, 22), LineTheme.dp(context, 22)));
    }

    private void addFileRow(LinearLayout tree, int iconType, String name, int depth, boolean root, int iconColor, int iconSize) {
        Context context = tree.getContext();
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int left = LineTheme.SM + depth * 16;
        LineTheme.padding(row, left, 4, LineTheme.SM, 4);
        tree.addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        IconButtonView icon = inlineIcon(context, iconType, iconColor, iconSize);
        row.addView(icon, new LinearLayout.LayoutParams(LineTheme.dp(context, iconSize), LineTheme.dp(context, iconSize)));

        TextView label = LineTheme.text(context, name, LineTheme.FONT_SM, LineTheme.TEXT, Typeface.NORMAL);
        label.setSingleLine(true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        labelParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
        row.addView(label, labelParams);

        if (root) {
            IconButtonView plus = sizedIcon(context, IconButtonView.PLUS, LineTheme.TEXT_TERTIARY, 22, 14);
            row.addView(plus, new LinearLayout.LayoutParams(LineTheme.dp(context, 22), LineTheme.dp(context, 22)));
        }
    }

    private IconButtonView sizedIcon(Context context, int type, int color, int containerSize, int iconSize) {
        IconButtonView icon = inlineIcon(context, type, color, iconSize);
        icon.setIconSizeDp(containerSize, iconSize);
        return icon;
    }
}
