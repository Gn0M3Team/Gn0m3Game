package com.gnome.gnome.setting;

import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Controller class for the Settings page in the application.
 *
 * This class handles user interactions within the settings view,
 * such as navigating back to the game or to the main menu.
 * It uses a {@link PageSwitcherInterface} implementation to manage page transitions.
 */
public class SettingController {


    @FXML
    private Button backGameButton;
    @FXML
    private Button mainMenuButton;
    @FXML
    private BorderPane settingPage;
    @FXML
    private ChoiceBox<String> musicChoiceBox;

    @FXML private Label languageLabel;
    @FXML private Label selectMusicLabel;
    @FXML private Button applyMusicButton;
    @FXML private Label titleLabel;


    private PageSwitcherInterface pageSwitch;
    private final Locale[] supportedLocales = {new Locale("en"), new Locale("sk")};
    private int currentLangIndex = 0;

    private ResourceBundle bundle;


    /**
     * Called automatically after the FXML is loaded.
     * Initializes page switcher, resource bundle, UI language, and music options.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();

        this.bundle = ResourceBundle.getBundle("com.gnome.gnome.lang.lang", supportedLocales[currentLangIndex]);
        applyLanguage();

        musicChoiceBox.getItems().addAll("Echoes of Eldoria", "Blades and Whispers", "Twilight of the Ancients");
        setSelectedTrack();
    }
    /**
     * Called automatically after the FXML is loaded.
     * Initializes page switcher, resource bundle, UI language, and music options.
     */
    @FXML
    private void onLanguageLeftClick() {
        currentLangIndex = (currentLangIndex - 1 + supportedLocales.length) % supportedLocales.length;
        bundle = ResourceBundle.getBundle("com.gnome.gnome.lang.lang", supportedLocales[currentLangIndex]);
        applyLanguage();
    }
    /**
     * Handles right arrow click to switch to the next language in the list.
     * Wraps around if the end of the list is reached.
     */
    @FXML
    private void onLanguageRightClick() {
        currentLangIndex = (currentLangIndex + 1) % supportedLocales.length;
        bundle = ResourceBundle.getBundle("com.gnome.gnome.lang.lang", supportedLocales[currentLangIndex]);
        applyLanguage();
    }


    /**
     * Applies localized text to UI elements based on the current resource bundle.
     */
    private void applyLanguage() {
        titleLabel.setText(bundle.getString("setting.title"));
        backGameButton.setText(bundle.getString("back.to.game"));
        mainMenuButton.setText(bundle.getString("main.menu"));
        selectMusicLabel.setText(bundle.getString("select.music"));
        applyMusicButton.setText(bundle.getString("apply.music"));
        languageLabel.setText(bundle.getString("language"));
    }

    /**
     * Handles the action of the "Back to Game" button.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void backToGameButtonClick(ActionEvent e) {
        pageSwitch.goNewGame(settingPage);
    }

    /**
     * Handles the action of the "Main Menu" button.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void mainMenuButtonClick(ActionEvent e) {
        pageSwitch.goMainMenu(settingPage);
    }

    /**
     * Sets the current music track in the ChoiceBox based on the playlist.
     * Selects the first matching track based on file name.
     */
    private void setSelectedTrack() {
        if (MusicWizard.playlist != null && !MusicWizard.playlist.isEmpty()) {
            String firstTrack = MusicWizard.playlist.get(0);

            if (firstTrack.contains("1.wav")) {
                musicChoiceBox.setValue("Echoes of Eldoria");
            } else if (firstTrack.contains("2.wav")) {
                musicChoiceBox.setValue("Blades and Whispers");
            } else if (firstTrack.contains("3.wav")) {
                musicChoiceBox.setValue("Twilight of the Ancients");
            }
        } else {
            musicChoiceBox.getSelectionModel().selectFirst();
        }
    }


    /**
     * Applies the selected music from the ChoiceBox.
     * Stops current music, builds a new playlist with the selected track first,
     * and avoids duplicating other tracks.
     */
    @FXML
    public void onApplyMusicClick() {
        String selectedTrack = musicChoiceBox.getValue();
        String selectedPath = "";

        switch (selectedTrack) {
            case "Echoes of Eldoria":
                selectedPath = "src/main/java/com/gnome/gnome/music/1.wav";
                break;
            case "Blades and Whispers":
                selectedPath = "src/main/java/com/gnome/gnome/music/2.wav";
                break;
            case "Twilight of the Ancients":
                selectedPath = "src/main/java/com/gnome/gnome/music/3.wav";
                break;
        }

        MusicWizard.Music_stop();

        List<String> newPlaylist = new ArrayList<>();
        newPlaylist.add(selectedPath);

        if (!selectedPath.contains("1.wav")) newPlaylist.add("src/main/java/com/gnome/gnome/music/1.wav");
        if (!selectedPath.contains("2.wav")) newPlaylist.add("src/main/java/com/gnome/gnome/music/2.wav");
        if (!selectedPath.contains("3.wav")) newPlaylist.add("src/main/java/com/gnome/gnome/music/3.wav");

        MusicWizard.change_loop(newPlaylist);// Set new playlist in loop
    }
}
