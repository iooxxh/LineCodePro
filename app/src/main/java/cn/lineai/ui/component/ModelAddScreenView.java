package cn.lineai.ui.component;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.ModelProtocolType;
import cn.lineai.model.ModelProviderPreset;
import cn.lineai.ui.theme.LineTheme;

public final class ModelAddScreenView extends LinearLayout {
    public interface Listener {
        void onBack();

        void onSave(ModelConfig model);
    }

    private static final String[] PROVIDER_LABELS = new String[] {"OpenAI", "Codex", "Anthropic", "本地"};

    private final TextView saveAction;
    private final TextView providerLabelView;
    private final EditText nameInput;
    private final ModelProviderPreset preset;
    private LinearLayout queryButton;
    private TextView queryLabel;
    private TextView queryText;
    private IconButtonView queryIcon;
    private TextView baseUrlHintView;
    private LinearLayout modelInputHost;
    private Switch customIdSwitch;
    private EditText baseUrlInput;
    private EditText apiKeyInput;
    private EditText modelIdInput;
    private final String[] selectedModelId = new String[] {""};
    private final boolean local;
    private final boolean lockedPreset;
    private final String providerLabel;
    private final ModelProtocolType[] protocolType = new ModelProtocolType[1];
    private boolean saveEnabled;

    public ModelAddScreenView(Context context, ModelProviderPreset preset, boolean local, Listener listener) {
        super(context);
        this.local = local;
        this.preset = preset;
        this.lockedPreset = preset != null;
        this.protocolType[0] = local ? ModelProtocolType.LOCAL_GGUF : preset == null ? ModelProtocolType.OPENAI_COMPATIBLE : preset.getProtocolType();
        this.providerLabel = local ? "本地" : preset == null ? null : preset.getLabel();
        setOrientation(VERTICAL);
        setBackgroundColor(LineTheme.BG);

        saveAction = LineTheme.textMedium(context, "保存", LineTheme.FONT_MD, LineTheme.TEXT_TERTIARY);
        saveAction.setGravity(Gravity.CENTER);
        LineTheme.padding(saveAction, LineTheme.MD, LineTheme.SM, LineTheme.MD, LineTheme.SM);
        addView(new ScreenHeaderView(context, "添加模型", listener::onBack, saveAction), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        LineTheme.padding(content, LineTheme.LG, LineTheme.LG, LineTheme.LG, LineTheme.LG);
        scrollView.addView(content, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(scrollView, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        providerLabelView = label(context, preset == null ? "提供商" : "提供商：" + preset.getLabel());
        content.addView(providerLabelView, labelParams(context, LineTheme.LG, LineTheme.SM));
        LinearLayout providerRow = new LinearLayout(context);
        providerRow.setOrientation(HORIZONTAL);
        for (int i = 0; i < PROVIDER_LABELS.length; i++) {
            final int index = i;
            boolean enabled = !lockedPreset || isActiveProviderIndex(index);
            addToggle(providerRow, PROVIDER_LABELS[i], isActiveProviderIndex(index), enabled, () -> {
                if (index == 3) {
                    Toast.makeText(context, "请从“加载本地模型”进入本地模型表单。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (local) {
                    Toast.makeText(context, "请返回后进入自定义模型表单。", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!lockedPreset) {
                    protocolType[0] = protocolForIndex(index);
                    updateProviderToggles(providerRow);
                    updateBaseUrlHint();
                    updateQueryState();
                    updateSaveState();
                }
            });
        }
        content.addView(providerRow, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        content.addView(label(context, "名称"), labelParams(context, LineTheme.LG, LineTheme.SM));
        nameInput = input(context, "", local ? "如 Qwen2.5 7B 本地" : preset == null ? "如 GPT-4o、Claude Sonnet" : "可留空，默认使用模型 ID", false, false);
        content.addView(nameInput, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        if (local) {
            addLocalUi(context, content);
            baseUrlInput = null;
            apiKeyInput = null;
            customIdSwitch = null;
            queryButton = null;
            queryLabel = null;
            queryText = null;
            queryIcon = null;
            baseUrlHintView = null;
            modelInputHost = null;
            modelIdInput = null;
        } else {
            content.addView(label(context, "Base URL"), labelParams(context, LineTheme.LG, LineTheme.SM));
            baseUrlInput = input(context, preset == null ? "" : preset.getBaseUrl(), preset == null ? "https://api.example.com/v1" : preset.getPlaceholder(), false, false);
            content.addView(baseUrlInput, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            baseUrlHintView = LineTheme.text(context, hintFor(preset), LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
            baseUrlHintView.setLineSpacing(LineTheme.dp(context, 3), 1f);
            LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            hintParams.topMargin = LineTheme.dp(context, LineTheme.SM);
            content.addView(baseUrlHintView, hintParams);

            content.addView(label(context, "API Key"), labelParams(context, LineTheme.LG, LineTheme.SM));
            apiKeyInput = input(context, "", "sk-...", false, true);
            content.addView(apiKeyInput, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            LinearLayout modelIdHeader = new LinearLayout(context);
            modelIdHeader.setOrientation(HORIZONTAL);
            modelIdHeader.setGravity(Gravity.CENTER_VERTICAL);
            TextView modelIdLabel = label(context, "模型 ID");
            modelIdHeader.addView(modelIdLabel, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            LinearLayout switchWrap = new LinearLayout(context);
            switchWrap.setOrientation(HORIZONTAL);
            switchWrap.setGravity(Gravity.CENTER_VERTICAL);
            TextView customText = LineTheme.textMedium(context, "自定义", LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY);
            switchWrap.addView(customText, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            customIdSwitch = new Switch(context);
            tintSwitch(customIdSwitch);
            LinearLayout.LayoutParams switchParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            switchParams.leftMargin = LineTheme.dp(context, LineTheme.SM);
            switchWrap.addView(customIdSwitch, switchParams);
            modelIdHeader.addView(switchWrap, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            content.addView(modelIdHeader, labelParams(context, LineTheme.LG, LineTheme.SM));

            modelInputHost = new LinearLayout(context);
            modelInputHost.setOrientation(VERTICAL);
            content.addView(modelInputHost, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            modelIdInput = input(context, "", "输入模型 ID", false, false);
            queryLabel = LineTheme.text(context, "请先查询并选择模型", LineTheme.FONT_MD, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
            queryLabel.setSingleLine(true);
            queryLabel.setEllipsize(TextUtils.TruncateAt.END);
            queryButton = createQueryButton(context);
            renderModelIdInput(false);

            customIdSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                renderModelIdInput(isChecked);
                updateSaveState();
            });
            queryButton.setOnClickListener(v -> {
                if (!canQuery()) {
                    return;
                }
                Toast.makeText(context, "模型查询稍后接入；先打开“自定义”输入模型 ID。", Toast.LENGTH_SHORT).show();
                customIdSwitch.setChecked(true);
            });
        }

        addLearningCard(context, content);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveState();
                updateQueryState();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        nameInput.addTextChangedListener(watcher);
        if (!local) {
            baseUrlInput.addTextChangedListener(watcher);
            apiKeyInput.addTextChangedListener(watcher);
            modelIdInput.addTextChangedListener(watcher);
        }

        saveAction.setOnClickListener(v -> {
            if (!saveEnabled) {
                return;
            }
            ModelConfig model = buildModelConfig(context);
            if (model != null) {
                listener.onSave(model);
            }
        });
        updateBaseUrlHint();
        updateQueryState();
        updateSaveState();
    }

    private void addLocalUi(Context context, LinearLayout content) {
        content.addView(label(context, "模型文件"), labelParams(context, LineTheme.LG, LineTheme.SM));
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setMinimumHeight(LineTheme.dp(context, 74));
        card.setBackground(LineTheme.roundedStroke(context, LineTheme.SURFACE_LIGHT, 12, LineTheme.BORDER_LIGHT));
        LineTheme.padding(card, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        FrameLayout iconWrap = new FrameLayout(context);
        iconWrap.setBackground(LineTheme.rounded(context, LineTheme.ACCENT_MUTED, 8));
        IconButtonView fileIcon = new IconButtonView(context, IconButtonView.FILE_UP);
        fileIcon.setIconColor(LineTheme.ACCENT);
        fileIcon.setIconSizeDp(38, 20);
        fileIcon.setClickable(false);
        iconWrap.addView(fileIcon, new FrameLayout.LayoutParams(LineTheme.dp(context, 38), LineTheme.dp(context, 38), Gravity.CENTER));
        card.addView(iconWrap, new LinearLayout.LayoutParams(LineTheme.dp(context, 38), LineTheme.dp(context, 38)));

        LinearLayout fileText = new LinearLayout(context);
        fileText.setOrientation(VERTICAL);
        LinearLayout.LayoutParams fileTextParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        fileTextParams.leftMargin = LineTheme.dp(context, LineTheme.MD);
        fileTextParams.rightMargin = LineTheme.dp(context, LineTheme.MD);
        card.addView(fileText, fileTextParams);
        fileText.addView(LineTheme.text(context, "选择 GGUF 模型文件", LineTheme.FONT_MD, LineTheme.TEXT_TERTIARY, Typeface.BOLD));
        TextView desc = LineTheme.text(context, "通过 SAF 选择文件，应用会导入本地副本", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, 3);
        fileText.addView(desc, descParams);
        IconButtonView down = new IconButtonView(context, IconButtonView.CHEVRON_DOWN);
        down.setIconColor(LineTheme.TEXT_TERTIARY);
        down.setIconSizeDp(16, 14);
        down.setClickable(false);
        card.addView(down, new LinearLayout.LayoutParams(LineTheme.dp(context, 16), LineTheme.dp(context, 16)));
        card.setOnClickListener(v -> Toast.makeText(context, "本地文件选择器稍后接入。", Toast.LENGTH_SHORT).show());
        content.addView(card, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        content.addView(label(context, "上下文长度"), labelParams(context, LineTheme.LG, LineTheme.SM));
        EditText ctx = input(context, "4096", "4096", false, false);
        ctx.setInputType(InputType.TYPE_CLASS_NUMBER);
        content.addView(ctx, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView hint = LineTheme.text(context, "手机端建议从 4096 开始；保存后模型 ID 会自动带上上下文标记。", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        hintParams.topMargin = LineTheme.dp(context, LineTheme.SM);
        content.addView(hint, hintParams);

        content.addView(label(context, "加速"), labelParams(context, LineTheme.LG, LineTheme.SM));
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(HORIZONTAL);
        addToggle(row, "自动", true, true, null);
        addToggle(row, "CPU", false, true, null);
        addToggle(row, "NPU", false, true, null);
        content.addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        TextView accelHint = LineTheme.text(context, "自动模式会优先尝试 Android Hexagon NPU，设备或模型不支持时回退 CPU。", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        accelHint.setLineSpacing(LineTheme.dp(context, 3), 1f);
        LinearLayout.LayoutParams accelHintParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        accelHintParams.topMargin = LineTheme.dp(context, LineTheme.SM);
        content.addView(accelHint, accelHintParams);
    }

    private void renderModelIdInput(boolean custom) {
        modelInputHost.removeAllViews();
        if (custom) {
            modelInputHost.addView(modelIdInput, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            return;
        }
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout selector = new LinearLayout(getContext());
        selector.setOrientation(HORIZONTAL);
        selector.setGravity(Gravity.CENTER_VERTICAL);
        selector.setBackground(LineTheme.roundedStroke(getContext(), LineTheme.SURFACE_LIGHT, 12, LineTheme.BORDER_LIGHT));
        LineTheme.padding(selector, LineTheme.LG, LineTheme.MD, LineTheme.LG, LineTheme.MD);
        queryLabel.setText(selectedModelId[0].length() == 0 ? "请先查询并选择模型" : selectedModelId[0]);
        selector.addView(queryLabel, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        IconButtonView down = new IconButtonView(getContext(), IconButtonView.CHEVRON_DOWN);
        down.setIconColor(LineTheme.TEXT_TERTIARY);
        down.setIconSizeDp(16, 14);
        down.setClickable(false);
        selector.addView(down, new LinearLayout.LayoutParams(LineTheme.dp(getContext(), 16), LineTheme.dp(getContext(), 16)));
        selector.setOnClickListener(v -> queryButton.performClick());
        row.addView(selector, new LinearLayout.LayoutParams(0, LineTheme.dp(getContext(), 48), 1f));
        LinearLayout.LayoutParams queryParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LineTheme.dp(getContext(), 48));
        queryParams.leftMargin = LineTheme.dp(getContext(), LineTheme.SM);
        row.addView(queryButton, queryParams);
        modelInputHost.addView(row, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private LinearLayout createQueryButton(Context context) {
        LinearLayout button = new LinearLayout(context);
        button.setOrientation(HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setMinimumWidth(LineTheme.dp(context, 76));
        LineTheme.padding(button, LineTheme.LG, 0, LineTheme.LG, 0);

        queryIcon = new IconButtonView(context, IconButtonView.SEARCH);
        queryIcon.setClickable(false);
        queryIcon.setIconSizeDp(16, 16);
        button.addView(queryIcon, new LinearLayout.LayoutParams(LineTheme.dp(context, 16), LineTheme.dp(context, 16)));

        queryText = LineTheme.text(context, "查询", LineTheme.FONT_MD, LineTheme.TEXT_TERTIARY, Typeface.BOLD);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textParams.leftMargin = LineTheme.dp(context, LineTheme.XS);
        button.addView(queryText, textParams);
        return button;
    }

    private ModelConfig buildModelConfig(Context context) {
        if (local) {
            Toast.makeText(context, "请先选择 GGUF 文件。本地推理稍后接入。", Toast.LENGTH_SHORT).show();
            return null;
        }
        String baseUrl = effectiveBaseUrl();
        String apiKey = value(apiKeyInput);
        String modelId = customIdSwitch.isChecked() ? value(modelIdInput) : selectedModelId[0];
        String name = value(nameInput);
        if (name.length() == 0) {
            name = modelId;
        }
        if (modelId.length() == 0 || name.length() == 0) {
            Toast.makeText(context, "请填写名称和模型 ID", Toast.LENGTH_SHORT).show();
            return null;
        }
        if (apiKey.length() == 0) {
            Toast.makeText(context, "请填写 API Key", Toast.LENGTH_SHORT).show();
            return null;
        }
        String label = providerLabel != null ? providerLabel : protocolType[0].getLabel();
        return new ModelConfig("", name, protocolType[0], label, baseUrl, apiKey, modelId);
    }

    private void updateProviderToggles(LinearLayout providerRow) {
        for (int i = 0; i < providerRow.getChildCount(); i++) {
            TextView button = (TextView) providerRow.getChildAt(i);
            boolean active = isActiveProviderIndex(i);
            button.setTextColor(active ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_SECONDARY);
            button.setBackground(LineTheme.rounded(getContext(), active ? LineTheme.ACCENT : LineTheme.SURFACE_LIGHT, 12));
            button.setAlpha(lockedPreset && !active ? 0.45f : 1f);
        }
    }

    private void updateBaseUrlHint() {
        if (local || baseUrlHintView == null) {
            return;
        }
        baseUrlHintView.setText(hintFor(lockedPreset ? preset : null));
        if (!lockedPreset && baseUrlInput != null) {
            baseUrlInput.setHint(placeholderFor(protocolType[0]));
        }
        if (providerLabelView != null && providerLabel == null) {
            providerLabelView.setText("提供商");
        }
    }

    private void updateQueryState() {
        if (local || queryButton == null) {
            return;
        }
        boolean canQuery = canQuery();
        if (queryText != null) {
            queryText.setTextColor(canQuery ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_TERTIARY);
        }
        if (queryIcon != null) {
            queryIcon.setIconColor(canQuery ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_TERTIARY);
        }
        queryButton.setBackground(LineTheme.rounded(getContext(), canQuery ? LineTheme.ACCENT : LineTheme.SURFACE_LIGHT, 12));
        queryButton.setEnabled(canQuery);
    }

    private void updateSaveState() {
        boolean canSave;
        if (local) {
            canSave = false;
        } else {
            String id = customIdSwitch.isChecked() ? value(modelIdInput) : selectedModelId[0];
            String name = value(nameInput);
            canSave = (name.length() > 0 || id.length() > 0)
                    && id.length() > 0
                    && value(apiKeyInput).length() > 0;
        }
        saveEnabled = canSave;
        saveAction.setTextColor(canSave ? LineTheme.ACCENT : LineTheme.TEXT_TERTIARY);
        saveAction.setAlpha(canSave ? 1f : 0.45f);
    }

    private void addLearningCard(Context context, LinearLayout content) {
        LinearLayout learning = new LinearLayout(context);
        learning.setOrientation(VERTICAL);
        learning.setBackground(LineTheme.roundedStroke(context, LineTheme.SURFACE_LIGHT, 12, LineTheme.BORDER_LIGHT));
        LineTheme.padding(learning, LineTheme.MD, LineTheme.MD, LineTheme.MD, LineTheme.MD);
        learning.addView(LineTheme.text(context, "学习模式已移至 AI 行为", LineTheme.FONT_MD, LineTheme.TEXT, Typeface.BOLD));
        TextView desc = LineTheme.text(context, "请在设置 → AI 行为中统一开启或关闭学习模式，避免每个模型重复配置。", LineTheme.FONT_XS, LineTheme.TEXT_TERTIARY, Typeface.NORMAL);
        desc.setLineSpacing(LineTheme.dp(context, 3), 1f);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        descParams.topMargin = LineTheme.dp(context, LineTheme.XS);
        learning.addView(desc, descParams);
        LinearLayout.LayoutParams learningParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        learningParams.topMargin = LineTheme.dp(context, LineTheme.XL);
        content.addView(learning, learningParams);
    }

    private void addToggle(LinearLayout row, String label, boolean active, boolean enabled, Runnable onClick) {
        Context context = row.getContext();
        TextView button = LineTheme.text(context, label, LineTheme.FONT_MD, active ? LineTheme.TEXT_ON_COLOR : LineTheme.TEXT_SECONDARY, Typeface.BOLD);
        button.setGravity(Gravity.CENTER);
        button.setBackground(LineTheme.rounded(context, active ? LineTheme.ACCENT : LineTheme.SURFACE_LIGHT, 12));
        button.setAlpha(enabled || active ? 1f : 0.45f);
        if (enabled && onClick != null) {
            button.setOnClickListener(v -> onClick.run());
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LineTheme.dp(context, 46), 1f);
        if (row.getChildCount() > 0) {
            params.leftMargin = LineTheme.dp(context, LineTheme.SM);
        }
        row.addView(button, params);
    }

    private boolean isActiveProviderIndex(int index) {
        if (index == 3) {
            return local;
        }
        return !local && protocolForIndex(index) == protocolType[0];
    }

    private ModelProtocolType protocolForIndex(int index) {
        if (index == 1) {
            return ModelProtocolType.CODEX_RESPONSES;
        }
        if (index == 2) {
            return ModelProtocolType.ANTHROPIC_MESSAGES;
        }
        return ModelProtocolType.OPENAI_COMPATIBLE;
    }

    private boolean canQuery() {
        return !local && effectiveBaseUrl().length() > 0 && value(apiKeyInput).length() > 0;
    }

    private String effectiveBaseUrl() {
        String baseUrl = value(baseUrlInput);
        if (baseUrl.length() > 0) {
            return baseUrl;
        }
        if (preset != null && preset.getBaseUrl() != null && preset.getBaseUrl().length() > 0) {
            return preset.getBaseUrl();
        }
        return defaultBaseUrlFor(protocolType[0]);
    }

    private String defaultBaseUrlFor(ModelProtocolType type) {
        if (type == ModelProtocolType.ANTHROPIC_MESSAGES) {
            return "https://api.anthropic.com";
        }
        return "https://api.openai.com/v1";
    }

    private String placeholderFor(ModelProtocolType type) {
        if (type == ModelProtocolType.ANTHROPIC_MESSAGES) {
            return "https://api.example.com/anthropic";
        }
        return "https://api.example.com/v1";
    }

    private String hintFor(ModelProviderPreset preset) {
        if (preset != null) {
            return preset.getHint();
        }
        if (protocolType[0] == ModelProtocolType.CODEX_RESPONSES) {
            return "Codex 使用 Responses API，也必须填到 /v1 结尾，例如 https://api.example.com/v1；不要加 /responses。";
        }
        if (protocolType[0] == ModelProtocolType.ANTHROPIC_MESSAGES) {
            return "Anthropic 协议必须填到 /anthropic 结尾，例如 https://api.example.com/anthropic；不要加 /v1/messages。";
        }
        return "OpenAI 兼容协议必须填到 /v1 结尾，例如 https://api.example.com/v1；不要只填域名，也不要加 /chat/completions。";
    }

    private TextView label(Context context, String text) {
        return LineTheme.textMedium(context, text, LineTheme.FONT_SM, LineTheme.TEXT_SECONDARY);
    }

    private LinearLayout.LayoutParams labelParams(Context context, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.topMargin = LineTheme.dp(context, top);
        params.bottomMargin = LineTheme.dp(context, bottom);
        return params;
    }

    private EditText input(Context context, String value, String placeholder, boolean multiline, boolean secure) {
        EditText input = new EditText(context);
        input.setText(value == null ? "" : value);
        input.setHint(placeholder);
        input.setHintTextColor(LineTheme.TEXT_TERTIARY);
        input.setTextColor(LineTheme.TEXT);
        input.setTextSize(LineTheme.FONT_MD);
        input.setSingleLine(!multiline);
        input.setMinHeight(LineTheme.dp(context, multiline ? 120 : 48));
        input.setIncludeFontPadding(false);
        input.setBackground(LineTheme.roundedStroke(context, LineTheme.SURFACE_LIGHT, 12, LineTheme.BORDER_LIGHT));
        input.setPadding(LineTheme.dp(context, LineTheme.LG), LineTheme.dp(context, LineTheme.MD), LineTheme.dp(context, LineTheme.LG), LineTheme.dp(context, LineTheme.MD));
        input.setInputType(secure
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : multiline ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        return input;
    }

    private void tintSwitch(Switch toggle) {
        int[][] states = new int[][] {
                new int[] {android.R.attr.state_checked},
                new int[] {-android.R.attr.state_checked}
        };
        toggle.setThumbTintList(new ColorStateList(states, new int[] {LineTheme.ACCENT, LineTheme.TEXT_TERTIARY}));
        toggle.setTrackTintList(new ColorStateList(states, new int[] {LineTheme.ACCENT_DIM, LineTheme.SURFACE_LIGHT}));
    }

    private String value(EditText input) {
        return input == null || input.getText() == null ? "" : input.getText().toString().trim();
    }
}
