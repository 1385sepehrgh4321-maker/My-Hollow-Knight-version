package com.Sepehr.HallowKnight.model.entities;

import com.Sepehr.HallowKnight.view.rendering.ZoteDialogueOverlay;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Zote extends Entity {
    public enum State {
        IDLE, TALK, ATTACK, FALL
    }

    private State currentState = State.IDLE;
    private float stateTime = 0f;
    private boolean isGrounded = false;

    private final float ZOTE_GRAVITY = -1300.0f;
    private final float ZOTE_CHARGE_SPEED = 220.0f;
    private final float ANGER_DURATION = 2.0f;

    private boolean hasTalkedBefore = false;
    private boolean inConversation = false;
    private int currentDialogueIndex = 0;
    private int currentPreceptIndex = 0;
    private int preceptsShownInCurrentSession = 0; // Prevents endless dialogue loops

    private final List<String> dialogues = new ArrayList<>();
    private final List<String> precepts = new ArrayList<>();
    private final ZoteDialogueOverlay dialogueOverlay;

    private final MiniAudio miniAudio;
    private final List<String> gruntAudioPaths = new ArrayList<>();
    private final Random random = new Random();

    private final TextureAtlas atlas;
    private final HashMap<State, Animation<TextureRegion>> animations = new HashMap<>();

    public Zote(float x, float y, String atlasPath, MiniAudio miniAudio) {
        super(x, y, 96f, 144f);
        this.health = 99999;
        this.miniAudio = miniAudio;
        this.dialogueOverlay = new ZoteDialogueOverlay();
        this.atlas = new TextureAtlas(Gdx.files.internal(atlasPath));

        initializeDialogueData();
        initializeAnimationTracks();
        initializeAudioPaths();
    }

    private void initializeDialogueData() {
        dialogues.add("Leave me be, tiny creature! I am Zote the Mighty!");
        dialogues.add("I do not need the assistance of someone as insignificant as you.");
        dialogues.add("My weapon, Lifeaker, has slain thousands!");

        precepts.add("Precept One: Always Win Your Battles.");
        precepts.add("Precept Two: Never Let Them Laugh Choice.");
        precepts.add("Precept Three: Always Be Rested.");
        precepts.add("Precept Four: Forget Your Past.");
        precepts.add("Precept Five: Strength Beats Strength.");
    }

    private void initializeAnimationTracks() {
        animations.put(State.IDLE,   new Animation<>(0.12f, atlas.findRegions("Idle"),   Animation.PlayMode.LOOP));
        animations.put(State.TALK,   new Animation<>(0.10f, atlas.findRegions("Talk"),   Animation.PlayMode.LOOP));
        animations.put(State.ATTACK, new Animation<>(0.08f, atlas.findRegions("Attack"), Animation.PlayMode.LOOP));
        animations.put(State.FALL,   new Animation<>(0.10f, atlas.findRegions("Fall"),   Animation.PlayMode.LOOP));
    }

    private void initializeAudioPaths() {
        gruntAudioPaths.add("audio/zote_grunt1.mp3");
        gruntAudioPaths.add("audio/zote_grunt2.mp3");
        gruntAudioPaths.add("audio/zote_grunt3.mp3");
    }

    private void playRandomGrunt() {
        if (gruntAudioPaths.isEmpty()) return;
        try {
            String randomPath = gruntAudioPaths.get(random.nextInt(gruntAudioPaths.size()));
            MASound grunt = miniAudio.createSound(randomPath);
            grunt.setVolume(0.5f);
            grunt.play();
        } catch (Exception ignored) {}
    }

    @Override
    public void update(float delta) {
        stateTime += delta;
        updateHitbox();
    }

    @Override
    public void draw(SpriteBatch batch) {
        Animation<TextureRegion> anim = animations.get(currentState);
        if (anim == null) anim = animations.get(State.IDLE);

        TextureRegion frame = anim.getKeyFrame(stateTime);

        if (!isFacingRight && frame.isFlipX()) {
            frame.flip(true, false);
        } else if (isFacingRight && !frame.isFlipX()) {
            frame.flip(true, false);
        }

        batch.draw(frame, position.x, position.y, hitbox.width, hitbox.height);
    }

    public void updateZote(float delta, Player player, Array<Rectangle> solidTiles) {
        update(delta);

        if (inConversation) {
            dialogueOverlay.update(delta);
        }

        checkIfAttackedByPlayer(player);
        executeStateLogic(player, delta);
        applyPhysicsAndCollisions(delta, solidTiles);
        handlePlayerInteraction(player);
    }

    private void checkIfAttackedByPlayer(Player player) {
        Rectangle nailBox = player.getAttackHitbox();
        if (nailBox != null && nailBox.overlaps(this.hitbox)) {
            if (currentState != State.ATTACK) {
                if (inConversation) {
                    endConversation();
                }
                setCurrentState(State.ATTACK);
                playRandomGrunt();
                isFacingRight = player.getPosition().x > position.x;
            }
        }
    }

    private void executeStateLogic(Player player, float delta) {
        switch (currentState) {
            case IDLE:
                velocity.x = 0;
                if (!isGrounded) {
                    setCurrentState(State.FALL);
                }
                if (!inConversation) {
                    isFacingRight = player.getPosition().x > position.x;
                }
                break;

            case FALL:
                velocity.y += ZOTE_GRAVITY * delta;
                break;

            case TALK:
                velocity.set(0, 0);
                if (!getPlayerInteractionBounds().overlaps(player.getHitbox())) {
                    endConversation();
                }
                break;

            case ATTACK:
                velocity.x = isFacingRight ? ZOTE_CHARGE_SPEED : -ZOTE_CHARGE_SPEED;
                isFacingRight = player.getPosition().x > position.x;

                if (stateTime > ANGER_DURATION) {
                    velocity.x = 0;
                    setCurrentState(State.IDLE);
                }
                break;
        }
    }

    private void applyPhysicsAndCollisions(float delta, Array<Rectangle> solidTiles) {
        // X Collision resolution logic
        position.x += velocity.x * delta;
        updateHitbox();
        for (Rectangle tile : solidTiles) {
            if (hitbox.overlaps(tile)) {
                if (velocity.x > 0) position.x = tile.x - hitbox.width;
                else if (velocity.x < 0) position.x = tile.x + tile.width;
                velocity.x = 0;
                break;
            }
        }

        // Y Collision resolution logic
        position.y += velocity.y * delta;
        updateHitbox();
        for (Rectangle tile : solidTiles) {
            if (hitbox.overlaps(tile)) {
                if (velocity.y < 0) {
                    position.y = tile.y + tile.height;
                    velocity.y = 0;
                } else if (velocity.y > 0) {
                    position.y = tile.y - hitbox.height;
                    velocity.y = 0;
                }
                updateHitbox();
            }
        }

        // --- FIX BUG 1: Bulletproof 1-pixel downward lookahead ground checking ---
        isGrounded = false;
        Rectangle feetLookahead = new Rectangle(hitbox.x, hitbox.y - 1f, hitbox.width, 2f);
        for (Rectangle tile : solidTiles) {
            if (feetLookahead.overlaps(tile)) {
                isGrounded = true;
                if (currentState == State.FALL) {
                    setCurrentState(State.IDLE);
                }
                break;
            }
        }
    }

    private void handlePlayerInteraction(Player player) {
        if (currentState == State.ATTACK) return;

        // --- FIX BUG 2: Accept all valid input configurations for advancing dialogue blocks ---
        boolean actionPressed = Gdx.input.isKeyJustPressed(Input.Keys.W) ||
            Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
            Gdx.input.isKeyJustPressed(Input.Keys.E) ||
            Gdx.input.isKeyJustPressed(Input.Keys.ENTER);

        if (getPlayerInteractionBounds().overlaps(player.getHitbox())) {
            if (!inConversation) {
                if (actionPressed) {
                    startInteraction();
                    playRandomGrunt();
                    dialogueOverlay.startMessage(advanceDialogue());
                }
            } else if (actionPressed) {
                if (!dialogueOverlay.isFinishedTyping()) {
                    dialogueOverlay.skipTyping();
                } else {
                    String nextLine = advanceDialogue();
                    if (nextLine.isEmpty()) {
                        endConversation();
                    } else {
                        playRandomGrunt();
                        dialogueOverlay.startMessage(nextLine);
                    }
                }
            }
        }
    }

    public void drawDialogue(SpriteBatch batch, OrthographicCamera camera) {
        if (inConversation) {
            batch.end();
            dialogueOverlay.render(batch, camera);
            batch.begin();
        }
    }

    public boolean isPlayerNearby(Player player) {
        return getPlayerInteractionBounds().overlaps(player.getHitbox());
    }

    private Rectangle getPlayerInteractionBounds() {
        return new Rectangle(position.x - 50f, position.y - 10f, hitbox.width + 100f, hitbox.height + 20f);
    }

    public void startInteraction() {
        this.inConversation = true;
        this.preceptsShownInCurrentSession = 0;
        setCurrentState(State.TALK);
    }

    public String advanceDialogue() {
        if (!inConversation) return "";

        if (!hasTalkedBefore) {
            if (currentDialogueIndex < dialogues.size()) {
                String line = dialogues.get(currentDialogueIndex);
                currentDialogueIndex++;
                return line;
            } else {
                hasTalkedBefore = true;
                return ""; // Returns empty string to safely trigger endConversation()
            }
        }

        // Subsequent Phase: Returns exactly one single precept line phrase per session
        if (preceptsShownInCurrentSession == 0) {
            String preceptLine = precepts.get(currentPreceptIndex);
            currentPreceptIndex = (currentPreceptIndex + 1) % precepts.size();
            preceptsShownInCurrentSession++;
            return preceptLine;
        }

        return ""; // Close subsequent interaction session
    }

    public void endConversation() {
        this.inConversation = false;
        setCurrentState(State.IDLE);
    }

    public void dispose() {
        dialogueOverlay.dispose();
        if (atlas != null) atlas.dispose();
    }

    public State getCurrentState() { return currentState; }
    public void setCurrentState(State state) {
        if (this.currentState != state) {
            this.currentState = state;
            this.stateTime = 0f;
        }
    }
    public boolean isInConversation() { return inConversation; }
}
