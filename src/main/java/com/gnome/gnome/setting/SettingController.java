package com.gnome.gnome.setting;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.util.ResourceBundle;
import java.util.logging.Logger;

public class SettingController {

    private static final Logger logger = Logger.getLogger(SettingController.class.getName());

    @FXML private ToggleButton musicToggleButton;
    @FXML private ToggleButton ambientToggleButton;
    @FXML private Slider soundVolumeSlider;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> trackSelector;
//    @FXML private Button backGameButton;
    @FXML private Button mainMenuButton;
    @FXML private BorderPane settingPage;

    private PageSwitcherInterface pageSwitch;
    private double soundVolume = (MusicWizard.getMAXvolume())*200;
    private char selectedLanguage;

    private ResourceBundle bundle;

    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
        selectedLanguage = MainApplication.getLang();

        this.bundle = MainApplication.getLangBundle();

        // Language
        languageComboBox.getItems().addAll("English (eng)", "Slovak (sk)");
        languageComboBox.setValue(selectedLanguage == 'E' ? "English (eng)" : "Slovak (sk)");

        // Volume
        soundVolumeSlider.setValue(soundVolume);
        soundVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> handleSoundVolumeChange());

        // Tracks
//        trackSelector.getItems().addAll("1.wav", "2.wav", "3.wav");
//        trackSelector.setOnAction(this::handleTrackSelection);
//        trackSelector.setValue("1.wav");

        // Ambient
        ambientToggleButton.setSelected(MainApplication.ambientEnabled);
        ambientToggleButton.setText(MainApplication.ambientEnabled ? bundle.getString("settings.ambient.on") : bundle.getString("settings.ambient.off"));
        if (MainApplication.ambientEnabled && !MusicWizard.ambientRunning) {
            MusicWizard.start_ambient();
        }

        // Music
        musicToggleButton.setSelected(MainApplication.musicEnabled);
        musicToggleButton.setText(MainApplication.musicEnabled ? bundle.getString("settings.music.on")  : bundle.getString("settings.music.off"));
        if (MainApplication.musicEnabled && !MusicWizard.musicRunning) {
            MusicWizard.start_music_loop();
        }

        MusicWizard.setGlobalVolume(soundVolume);
    }

    @FXML
    public void handleMusicToggle(ActionEvent e) {
        MainApplication.musicEnabled = musicToggleButton.isSelected();
        musicToggleButton.setText(MainApplication.musicEnabled ? bundle.getString("settings.music.on") : bundle.getString("settings.music.off") );

        if (MainApplication.musicEnabled) {
            if (!MusicWizard.musicRunning) {
                System.out.println("Run Music");
                MusicWizard.start_music_loop();
            }
        } else {
            MusicWizard.stop_music();
        }
    }


    @FXML
    public void handleAmbientToggle(ActionEvent e) {
        MainApplication.ambientEnabled = ambientToggleButton.isSelected();
        ambientToggleButton.setText(MainApplication.ambientEnabled ? bundle.getString("settings.ambient.on") : bundle.getString("settings.ambient.off"));

        if (MainApplication.ambientEnabled) {
            if (!MusicWizard.ambientRunning) {
                MusicWizard.start_ambient();
            }
        } else {
            MusicWizard.stop_ambient();
        }
    }

    @FXML
    public void handleSoundVolumeChange() {
        soundVolume = soundVolumeSlider.getValue();
        logger.info("Volume set to: " + soundVolume);
        MusicWizard.setGlobalVolume(soundVolume);
    }

//    @FXML
//    public void handleTrackSelection(ActionEvent e) {
//        String selectedTrack = trackSelector.getValue();
//        if (selectedTrack != null && !selectedTrack.isEmpty()) {
//            String fullPath = "src/main/java/com/gnome/gnome/music/" + selectedTrack;
//            logger.info("Selected track: " + fullPath);
//            MusicWizard.playSingleTrack(fullPath);
//        }
//    }

    @FXML
    public void handleLanguageChange(ActionEvent e) {
        String selected = languageComboBox.getValue();
        if (selected != null) {
            selectedLanguage = selected.equals("English (eng)") ? 'E' : 'S';
            MainApplication.flipLanguage();

            logger.info("Language set to: " + selectedLanguage);
        }
    }

//    @FXML
//    public void backToGameButtonClick(ActionEvent e) {
//        pageSwitch.goNewGame(settingPage);
//    }

    @FXML
    public void mainMenuButtonClick(ActionEvent e) {
        pageSwitch.goMainMenu(settingPage);
    }
}
