package com.gnome.gnome.setting;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import java.util.logging.Logger;

/**
 * Controller class for the Settings page in the application.
 *
 * This class handles user interactions within the settings view,
 * such as toggling sound, adjusting sound volume, selecting language, and navigating back to the game or main menu.
 * It uses a {@link PageSwitcherInterface} implementation to manage page transitions.
 */
public class SettingController {

    private static final Logger logger = Logger.getLogger(SettingController.class.getName());

    @FXML private ToggleButton soundToggleButton;
    @FXML private Slider soundVolumeSlider;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private Button backGameButton;
    @FXML private Button mainMenuButton;
    @FXML private BorderPane settingPage;
    @FXML private ComboBox<String> trackSelector;

    private PageSwitcherInterface pageSwitch;
    private boolean isSoundOn = true; // Default sound state
    private double soundVolume = 50.0; // Default volume (0 to 100)
    private char selectedLanguage; // Default language

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It initializes the {@link PageSwitcherInterface} and sets up the UI components.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();

        selectedLanguage = MainApplication.lang;

        // Initialize sound toggle button
        soundToggleButton.setSelected(isSoundOn);
        soundToggleButton.setText(isSoundOn ? "Sound: ON" : "Sound: OFF");

        // Initialize sound volume slider
        soundVolumeSlider.setValue(soundVolume);
        soundVolumeSlider.setDisable(!isSoundOn); // Disable slider if sound is off

        // Initialize language combo box
        languageComboBox.getItems().addAll("English (eng)", "Slovak (sk)");
        languageComboBox.setValue("English (eng)");

        trackSelector.getItems().addAll("1.wav", "2.wav", "3.wav");
    }

    /**
     * Handles the sound toggle button click.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void handleSoundToggle(ActionEvent e) {
        isSoundOn = !isSoundOn;
        soundToggleButton.setText(isSoundOn ? "Sound: ON" : "Sound: OFF");
        logger.info("Sound toggled to: " + (isSoundOn ? "ON" : "OFF"));

        if (isSoundOn) {
            MusicWizard.start_ambient();
        } else {
            soundVolumeSlider.setDisable(true);
            MusicWizard.stop_ambient();
        }
    }

    /**
     * Handles the sound volume change.
     */
    @FXML
    public void handleSoundVolumeChange() {
        soundVolume = soundVolumeSlider.getValue();
        logger.info("Sound volume set to: " + soundVolume);
        MusicWizard.setGlobalVolume(soundVolume);
    }

    /**
     * Handles the language selection change.
     *
     * @param e the action event triggered by the combo box selection
     */
    @FXML
    public void handleLanguageChange(ActionEvent e) {
        String selected = languageComboBox.getValue();
        selectedLanguage = selected.equals("English (eng)") ? 'E' : 'S';
        MainApplication.lang = selectedLanguage;
        logger.info("Language set to: " + selectedLanguage);
    }

    @FXML
    public void handleTrackSelection(ActionEvent e) {
        String track = trackSelector.getValue();
        if (track != null && !track.isEmpty()) {
            MusicWizard.playSingleTrack("src/main/java/com/gnome/gnome/music/" + track);
        }
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
}