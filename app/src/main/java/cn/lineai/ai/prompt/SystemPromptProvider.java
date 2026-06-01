package cn.lineai.ai.prompt;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public final class SystemPromptProvider {
    private static final String TEMPLATE_PATH = "prompts/system-prompt-template.txt";
    private static final String WORK_DIRECTORY_TEMPLATE_PATH = "prompts/work-directory-template.txt";

    private final Context context;
    private StringTemplate cachedTemplate;
    private StringTemplate cachedWorkDirectoryTemplate;

    public SystemPromptProvider(Context context) {
        this.context = context.getApplicationContext();
    }

    public String build(String homePath) {
        HashMap<String, String> values = new HashMap<>();
        values.put("WORK_DIRECTORY_CONTEXT", workDirectoryContext(homePath));
        return template().render(values);
    }

    private StringTemplate template() {
        if (cachedTemplate == null) {
            cachedTemplate = new StringTemplate(readAsset(TEMPLATE_PATH));
        }
        return cachedTemplate;
    }

    private String workDirectoryContext(String homePath) {
        if (homePath == null || homePath.trim().length() == 0) {
            return "";
        }
        HashMap<String, String> values = new HashMap<>();
        values.put("HOME_PATH", homePath.trim());
        if (cachedWorkDirectoryTemplate == null) {
            cachedWorkDirectoryTemplate = new StringTemplate(readAsset(WORK_DIRECTORY_TEMPLATE_PATH));
        }
        return cachedWorkDirectoryTemplate.render(values);
    }

    private String readAsset(String path) {
        try {
            InputStream input = context.getAssets().open(path);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            input.close();
            return output.toString(StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalStateException("无法读取系统提示词模板: " + path, e);
        }
    }
}
