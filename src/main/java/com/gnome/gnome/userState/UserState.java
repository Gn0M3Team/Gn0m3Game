package com.gnome.gnome.userState;

import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.models.user.UserGameState;
import lombok.Getter;

@Getter
public class UserState {
    private static UserState instance;

    private String username;
    private PlayerRole role;
    @Getter
    private float balance;
    private float health;
    private int score;
    private int deathCounter;
    private int mapLevel;
    private Integer weaponId;
    private Integer potionId;
    private Integer armorId;

    private final UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
    private final AuthUserDAO authUserDAO = new AuthUserDAO();

    private UserState() {
    }

    public static UserState getInstance() {
        if (instance == null) {
            instance = new UserState();
        }
        return instance;
    }

    public static void init(AuthUser authUser, UserGameState userGameState) {
        UserState userState = getInstance();
        userState.username = authUser.getUsername();
        userState.role = authUser.getRole();
        userState.balance = userGameState.getBalance();
        userState.health = userGameState.getHealth();
        userState.score = userGameState.getScore();
        userState.deathCounter = userGameState.getDeathCounter();
        userState.mapLevel = userGameState.getMapLevel();
        userState.weaponId = userGameState.getWeaponId();
        userState.potionId = userGameState.getPotionId();
        userState.armorId = userGameState.getArmorId();
    }

    // Setters with immediate database update
//    public void setRole(String role) {
//        this.role = role;
//        AuthUser authUser = new AuthUser(username, null, role);
//        authUserDAO.updateUserRole(authUser);
//    }

    public void setBalance(float balance) {
        this.balance = balance;
        updateUserGameState();
    }

    public void setHealth(float health) {
        this.health = health;
        updateUserGameState();
    }

    public void setScore(int score) {
        this.score = score;
        updateUserGameState();
    }

    public void setDeathCounter(int deathCounter) {
        this.deathCounter = deathCounter;
        updateUserGameState();
    }

    public void setMapLevel(int mapLevel) {
        this.mapLevel = mapLevel;
        updateUserGameState();
    }

    public void setWeaponId(Integer weaponId) {
        this.weaponId = weaponId;
        updateUserGameState();
    }

    public void setPotionId(Integer potionId) {
        this.potionId = potionId;
        updateUserGameState();
    }

    public void setArmorId(Integer armorId) {
        this.armorId = armorId;
        updateUserGameState();
    }

    private void updateUserGameState() {
        UserGameState userGameState = new UserGameState(
                username,
                balance,
                health,
                score,
                deathCounter,
                mapLevel,
                weaponId,
                potionId,
                armorId
        );
        userGameStateDAO.updateUserGameState(userGameState);
    }
}
