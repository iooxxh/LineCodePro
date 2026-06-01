package cn.lineai;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import cn.lineai.mvp.MainContract;
import cn.lineai.mvp.MainPresenter;
import cn.lineai.ui.MainChatView;
import cn.lineai.ui.theme.LineTheme;

@SuppressWarnings("deprecation")
public final class MainActivity extends Activity {
    private MainContract.Presenter presenter;
    private MainChatView mainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configureWindow();

        presenter = new MainPresenter(this);
        mainView = new MainChatView(this, presenter);
        setContentView(mainView);
        presenter.attachView(mainView);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mainView != null && mainView.handleBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private void configureWindow() {
        Window window = getWindow();
        window.setStatusBarColor(LineTheme.BG);
        window.setNavigationBarColor(LineTheme.BG);
    }
}
