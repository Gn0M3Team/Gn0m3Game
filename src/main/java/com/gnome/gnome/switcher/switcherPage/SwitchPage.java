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
    public void goLogin(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/login.fxml");
    }

    @Override
    public void goRegistration(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/registration.fxml");
    }

    @Override
    public void goAccount(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/account.fxml");
    }

    @Override
    public void goSwitch(BorderPane anchorPane)  {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/switchingPage/switcher-page.fxml");
    }

    @Override
    public void goHello(BorderPane anchorPane) {
        new SceneSwitch(anchorPane, "/com/gnome/gnome/pages/hello-view.fxml");
    }


}
