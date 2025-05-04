package com.gnome.gnome.switcher.switcherPage;

import javafx.scene.layout.BorderPane;

/**
 * Interface for switching between different pages (or scenes) in the application.
 *
 * This interface defines methods for navigating to various parts of the application
 * using a common layout container (usually a {@link BorderPane}).
 *
 * Implementations of this interface should handle the actual logic for scene transitions.
 */
public interface PageSwitcherInterface {

    /**
     * Navigates to the Hello (main menu or welcome) page.
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goMainMenu(BorderPane anchorPane);

    /**
     * Navigates to the Continue Game page.
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goContinueGame(BorderPane anchorPane);

    /**
     * Navigates to the Editor page (e.g., level or content editor).
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goEditor(BorderPane anchorPane);

    /**
     * Navigates to the Login or Registration page.
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goLogin(BorderPane anchorPane);

    /**
     * Navigates to the Profile page for a specific user.
     *
     * @param anchorPane the current root pane from which the scene will switch
     * @param selected the identifier or username of the selected profile
     */
    void goProfile(BorderPane anchorPane, String selected);

    /**
     * Navigates to a New Game screen.
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goNewGame(BorderPane anchorPane);

    /**
     * Navigates to the Settings page.
     *
     * @param anchorPane the current root pane from which the scene will switch
     */
    void goSetting(BorderPane anchorPane);

    /**
     * Displays the Shop pop-up window.
     *
     * @param anchorPane the current root pane from which the scene will switch or where the pop-up will appear
     */
    void goShop(BorderPane anchorPane);

    void goStoryMaps(BorderPane anchorPane);

    /**
     * Navigate to the login page, if internet lost
     */
    void goToBeginning();
}
