package com.Sepehr.HallowKnight.model.event;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.HashSet;

public class AchievementManager implements GameEventListener{
    //todo:check the next line with the name in the achievement screen
    private static final String SAVE_NAME = "HollowKnightSaveData";
    private final Preferences prefs;

    private final HashSet<String> unlockedIds = new HashSet<>();
    private final HashSet<String> defeatedEnemyTypes = new HashSet<>();

    private float playtimeCounter = 0f;
    private boolean falseKnightDefeatedFlag = false;
    private boolean gameCompletedFlag = false;

    private static AchievementManager instance;
    private AchievementPopupNotify currentNotificationListener;

    public interface AchievementPopupNotify {
        void triggerPopup(String titleKey, String descKey);
    }

    public static AchievementManager getInstance() {
        if (instance == null) instance = new AchievementManager();
        return instance;
    }

    private AchievementManager() {
        prefs = Gdx.app.getPreferences(SAVE_NAME);
        loadAchievements();

        // Subscribe to game lifecycle events
        EventDispatcher.register(GameEvent.ENEMY_KILLED, this);
        EventDispatcher.register(GameEvent.BOSS_DEFEATED_FALSE_KNIGHT, this);
        EventDispatcher.register(GameEvent.GAME_COMPLETED, this);
    }

    public void setNotificationListener(AchievementPopupNotify listener) {
        this.currentNotificationListener = listener;
    }

    @Override
    public void onEvent(GameEvent event, Object data) {
        switch (event) {
            case ENEMY_KILLED:
                if (data instanceof String) {
                    defeatedEnemyTypes.add((String) data);
                }
                break;
            case BOSS_DEFEATED_FALSE_KNIGHT:
                falseKnightDefeatedFlag = true;
                break;
            case GAME_COMPLETED:
                gameCompletedFlag = true;
                break;
        }
    }

    /**
     * Call this method inside your main GameplayScreen render loop every single frame.
     */
    public void updateFrameChecks(float delta, com.Sepehr.HallowKnight.model.entities.Player player) {
        playtimeCounter += delta;

        // 1. ach_false_knight
        if (falseKnightDefeatedFlag) {
            unlock("ach_false_knight", "lbl_ach_false_knight_title", "lbl_ach_false_knight_desc");
        }

        // 2. ach_true_hunter
        if (defeatedEnemyTypes.size() >= 3) {
            unlock("ach_true_hunter", "lbl_ach_hunter_title", "lbl_ach_hunter_desc");
        }

        // 3. ach_custom_slot (Example condition: Player has unlocked/equipped 3 or more charms)
        if (player != null && player.getEquippedCharms() != null && player.getEquippedCharms().size() >= 3) {
            unlock("ach_custom_slot", "lbl_ach_custom_title", "lbl_ach_custom_desc");
        }

        // 4. ach_completion
        if (gameCompletedFlag) {
            unlock("ach_completion", "lbl_ach_completion_title", "lbl_ach_completion_desc");
        }

        if (gameCompletedFlag && playtimeCounter <= 2700f) {
            unlock("ach_speedrun", "lbl_ach_speedrun_title", "lbl_ach_speedrun_desc");
        }
    }

    public void unlock(String saveKey, String titleKey, String descKey) {
        if (!unlockedIds.contains(saveKey)) {
            unlockedIds.add(saveKey);
            prefs.putBoolean(saveKey, true);
            prefs.flush();

            if (currentNotificationListener != null) {
                currentNotificationListener.triggerPopup(titleKey, descKey);
            }
        }
    }

    private void loadAchievements() {
        String[] ids = {"ach_completion", "ach_speedrun", "ach_true_hunter", "ach_false_knight", "ach_custom_slot"};
        for (String id : ids) {
            if (prefs.getBoolean(id, false)) {
                unlockedIds.add(id);
            }
        }
    }
}
