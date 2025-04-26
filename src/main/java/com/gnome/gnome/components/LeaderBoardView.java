package com.gnome.gnome.components;

import com.gnome.gnome.HelloController;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.profile.ProfileController;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class LeaderBoardView extends VBox {
    private static final Logger logger = Logger.getLogger(LeaderBoardView.class.getName());


    private Label titleLabel;
    private TextField searchField;
    private RadioButton allRadioButton;
    private RadioButton onlyMyRadioButton;
    private ToggleGroup toggleGroup;
    private ListView<String> listView;
    private List<AuthUser> allUsers;
    private List<AuthUser> filteredUsers;

    private AuthUser currentUser;

    // Pagination state for the list items
    private int currentPage = 1;
    private final int pageSize = 17;
    private boolean loading = false;
    private final HelloController parentController;
    private PageSwitcherInterface pageSwitch;
    private final AuthUserDAO userDAO = new AuthUserDAO();
    /**
     * Constructs a LeaderBoardView with a parent controller and a close action.
     *
     * @param parentController the controller of the parent page.
     * @param onCloseAction a Runnable to execute when the close button is pressed.
     */
    public LeaderBoardView(HelloController parentController,Runnable onCloseAction) {

        this.parentController=parentController;
        pageSwitch=new SwitchPage();

        // Set style and load the external css
        this.getStyleClass().add("leaderboard-view");
        this.getStylesheets().add(
                getClass().getResource("/com/gnome/gnome/pages/css/leaderboard.css").toExternalForm()
        );
        this.currentUser = UserSession.getInstance().getCurrentUser();

        logger.info("Initializing LeaderBoardView...");

        initComponents();
        setupPagination();
        buildLayout(onCloseAction);
        setupToggleListener();
    }

    /**
     * Initializes UI components.
     */
    private void initComponents() {
        titleLabel = new Label("RESULT");
        titleLabel.getStyleClass().add("title-label");

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");

        allRadioButton = new RadioButton("all");
        onlyMyRadioButton = new RadioButton("only my");
        toggleGroup = new ToggleGroup();
        allRadioButton.setToggleGroup(toggleGroup);
        onlyMyRadioButton.setToggleGroup(toggleGroup);
        allRadioButton.setSelected(true);
        allRadioButton.getStyleClass().add("radio-button");
        onlyMyRadioButton.getStyleClass().add("radio-button");

        listView = new ListView<>();
        listView.getStyleClass().add("list-view");

        // Load the initial set of items into the ListView.
        loadMoreItems();
        searchField.setOnKeyReleased(event -> filterList(searchField.getText()));
    }

    /**
     * Sets up pagination by adding a listener to the ListView's vertical scrollbar.
     */
    private void setupPagination() {
        listView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) listView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= scrollBar.getMax() && !loading) {
                            logger.fine("User scrolled to bottom, loading more items...");
                            loadMoreItems();
                        }
                    });
                }
            }
        });
    }

    /**
     * Builds the layout by assembling UI components into rows.
     *
     * @param onCloseAction the action to execute when the close button is clicked.
     */
    private void buildLayout(Runnable onCloseAction) {
        // Row for the title
        HBox titleRow = new HBox(titleLabel);
        titleRow.getStyleClass().add("top-row");
        titleRow.setAlignment(Pos.CENTER);

        // Row for the search field
        HBox searchRow = new HBox(searchField);
        searchRow.getStyleClass().add("search-row");
        searchRow.setAlignment(Pos.CENTER);

        // Row for the radio buttons
        HBox radioRow = new HBox(10, allRadioButton, onlyMyRadioButton);
        radioRow.getStyleClass().add("radio-row");
        radioRow.setAlignment(Pos.CENTER);

        // Close button with its row
        Button closeBtn = new Button("X");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> onCloseAction.run());
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(10, 0, 0, 0));

        // Add an extra style class for the container, if needed.
        this.getStyleClass().add("leaderboard-container");

        // Assemble the rows into the main VBox.
        this.getChildren().addAll(titleRow, searchRow, radioRow, listView, closeRow);

        // Set the double-click event to handle redirection.
        listView.setOnMouseClicked(this::handleListClick);
    }

    /**
     * Sets up listeners for the radio buttons to filter between all users and only the current user's data.
     */
    private void setupToggleListener() {
        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == onlyMyRadioButton) {
                logger.info("Filter: only my");
                listView.getItems().clear();
                filterOnlyMyUser();
            } else {
                logger.info("Filter: all");
                listView.getItems().clear();
                allUsers = null;
                currentPage = 1;
                loadMoreItems();
            }
        });
    }

    /**
     * Loads additional users from the database and appends them to the ListView.
     * This method supports paginated loading.
     */
    private void loadMoreItems() {
        loading = true;
        logger.fine("Loading more leaderboard items...");

        int offset = (currentPage - 1) * pageSize;
        List<AuthUser> users = userDAO.getUsersByPage(offset, pageSize);

        if (allUsers == null) {
            allUsers = users;
            filteredUsers = users;
        } else {
            allUsers.addAll(users);
            filteredUsers = allUsers;
        }

        updateListView(filteredUsers);

        if (!users.isEmpty()) {
            currentPage++;
        }

        loading = false;
    }

    /**
     * Filters the list based on the text input from the search field.
     *
     * @param query the text to filter usernames by.
     */
    private void filterList(String query) {
        filteredUsers = allUsers.stream()
                .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        updateListView(filteredUsers);
    }
    /**
     * Filters the list to show only the current user's record.
     */
    private void filterOnlyMyUser() {
        filteredUsers = allUsers.stream()
                .filter(user -> user.getUsername().equals(currentUser.getUsername()))
                .collect(Collectors.toList());

        updateListView(filteredUsers);
    }
    /**
     * Updates the ListView with a new set of AuthUser objects.
     *
     * @param users the list of users to display.
     */
    private void updateListView(List<AuthUser> users) {
        listView.getItems().clear();
        for (AuthUser user : users) {
            String display = user.getUsername() + " - [role: " + user.getRole() + "]";
            listView.getItems().add(display);
        }
    }

    /**
     * Handles mouse clicks on the ListView. When a record is double-clicked,
     * it loads the profile page and passes the selected player's data.
     *
     * @param event the MouseEvent triggered on the ListView.
     */
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                logger.info("Opening profile for: " + selected);
                BorderPane helloPage = parentController.getHelloPage();
                String username = selected.split(" - ")[0];
                logger.info(username);
                pageSwitch.goProfile(helloPage, username);
            }
        }
    }

}