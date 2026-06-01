package cn.lineai.mvp;

import cn.lineai.model.ChatUiState;
import cn.lineai.model.ModelConfig;
import cn.lineai.model.SheetOption;
import java.util.List;

public interface MainContract {
    interface View {
        void render(ChatUiState state);

        void showDrawer();

        void showSheet(String title, List<SheetOption> options);

        void hideOverlays();

        void showScreen(String screenId);

        void showChatScreen();
    }

    interface Presenter {
        void attachView(View view);

        void detachView();

        void onMenuClick();

        void onProjectClick();

        void onPermissionClick();

        void onNewConversation();

        void onMoreClick();

        void onSendMessage(String text);

        void onStopGeneration();

        void onSheetOptionSelected(String id);

        void onScreenBack();

        void onSettingsItemSelected(String id);

        List<ModelConfig> getModels();

        String getSelectedModelId();

        void onModelSelected(String id);

        void onModelSaved(ModelConfig model);
    }
}
