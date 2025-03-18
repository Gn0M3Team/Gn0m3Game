package com.gnome.gnome.switcher.switcherPage;

import com.gnome.gnome.switcher.switcherPage.component.SceneSwitch;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * The SwitchPage class implements the PageSwitcherInterface interface and provides
 * functionality for switching pages in the application.
 *
 * Uses the SceneSwitch class to replace the current AnchorPane content
 * with another one according to the passed FXML file.
 */
public class SwitchPage implements PageSwitcherInterface {

    @Override
    public void goLogin(AnchorPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/login.fxml");
    }

    @Override
    public void goRegistration(AnchorPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/registration.fxml");
    }

    @Override
    public void goAccount(AnchorPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/account.fxml");
    }

    @Override
    public void goSwitch(AnchorPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/switcher-page.fxml");
    }
}
