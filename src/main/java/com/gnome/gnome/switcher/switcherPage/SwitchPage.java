package com.gnome.gnome.switcher.switcherPage;

import com.gnome.gnome.switcher.switcherPage.component.SceneSwitch;
import javafx.scene.layout.BorderPane;


/**
 * The SwitchPage class implements the PageSwitcherInterface interface and provides
 * functionality for switching pages in the application.
 *
 * Uses the SceneSwitch class to replace the current AnchorPane content
 * with another one according to the passed FXML file.
 */
public class SwitchPage implements PageSwitcherInterface {


    @Override
    public void goMainMenu(BorderPane anchorPane) {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/main-menu.fxml");
    }

    @Override
    public void goContinueGame(BorderPane anchorPane) {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/game.fxml");
    }

    @Override
    public void goEditor(BorderPane anchorPane) {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/editor-view.fxml");
    }

    @Override
    public void goLogin(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/login-registration.fxml");
    }
    @Override
    public void goProfile(BorderPane anchorPane,String selected)  {
        SceneSwitch.setGlobalData(selected);
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/profile-page.fxml");
    }
    @Override
    public void goNewGame(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/main-new-game-page.fxml");
    }
    @Override
    public void goSetting(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/setting.fxml");
    }

    @Override
    public void goShop(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/shop.fxml");
    }

    @Override
    public void goStoryMaps(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/story-mode-page.fxml");
    }



}
