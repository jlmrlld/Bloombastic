package com.ellado.bloombastic;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;


import org.w3c.dom.Text;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class bloombastic implements ApplicationListener {

    Texture road;
    Texture player;
    Texture crack1, crack2, crack3;
    Texture trafficIsland;
    Texture people;
    Texture pauseButton;
    Texture peopleCounterButton;
    Texture pauseBackground;
    Texture resumeButton;
    Texture restartButton;
    Texture exitButton;
    Texture playButton;

    SpriteBatch batch;
    FitViewport viewport;
    OrthographicCamera camera;
    Sprite playerSprite;
    Sprite pauseButtonSprite;
    Sprite peopleCounterButtonSprite;
    Sprite pauseBackgroundSprite;
    Sprite resumeButtonSprite;
    Sprite restartButtonSprite;
    Sprite exitButtonSprite;
    Sprite playButtonSprite;

    Vector2 touchPos;
    float scrollY = 0;
    boolean isPaused = false;
    boolean ignoreNextTouch = false;
    private boolean gameStarted = false;
    private final float[] lanes = {20f, 110f, 200f};
    private enum GameState { MENU, PLAYING, PAUSED}
    private GameState gameState = GameState.MENU;
    private int score = 0;
    private BitmapFont font;

    private float baseScrollSpeed = 2f; // Starting speed
    private float currentScrollSpeed; // Current obstacle speed
    private float maxSpeed = 6f; // Maximum speed cap
    private int speedIncreaseInterval = 100; // Score interval for speed increases
    private int obstacleIncreaseInterval = 200; // Score interval for adding obstacles
    private int baseCrackCount = 2; // Starting number of cracks
    private int currentCrackCount; // Current number of cracks//display for score text

    Array<Sprite> crackSprites;
    Array<Sprite> trafficIslandSprites;
    Array<Sprite> peopleSprites;
    Array<Sprite> obstacles;



    @Override
    public void create() {
        currentScrollSpeed = baseScrollSpeed;//shu
        currentCrackCount = baseCrackCount;//shu
        font = new BitmapFont();//shu
        font.getData().setScale(1.5f);//shu
        player = new Texture("player.png");
        road = new Texture("road.png");
        crack1 = new Texture("crack1.png");
        crack2 = new Texture("crack2.png");
        crack3 = new Texture("crack3.png");
        trafficIsland = new Texture("trafficIsland.png");
        people = new Texture("people.png");
        pauseButton = new Texture("pauseButton.png");
        peopleCounterButton = new Texture("peopleCounterButton.png");
        pauseBackground = new Texture("pauseBackground.png");
        resumeButton = new Texture("resumeButton.png");
        restartButton = new Texture("restartButton.png");
        exitButton = new Texture("exitButton.png");
        playButton = new Texture("playButton.png");

        road.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        viewport = new FitViewport(300, 560);
        camera = (OrthographicCamera) viewport.getCamera();
        touchPos = new Vector2();

        batch = new SpriteBatch();

        playerSprite = new Sprite(player);
        playerSprite.setSize(50, 50);
        playerSprite.setPosition(125, 20);

        pauseButtonSprite = new Sprite(pauseButton);
        pauseButtonSprite.setSize(40, 40);
        pauseButtonSprite.setPosition(40, viewport.getWorldHeight() - 50);

        peopleCounterButtonSprite = new Sprite(peopleCounterButton);
        peopleCounterButtonSprite.setSize(100, 40);
        peopleCounterButtonSprite.setPosition(
            (viewport.getWorldWidth() - peopleCounterButtonSprite. getWidth()) / 2,
            viewport.getWorldHeight() - peopleCounterButtonSprite.getHeight() - 10);

        pauseBackgroundSprite = new Sprite(pauseBackground);
        pauseBackgroundSprite.setSize(230, 330);
        pauseBackgroundSprite.setPosition(viewport.getWorldWidth() / 2 -115, viewport.getWorldHeight() / 2 - 180);

        resumeButtonSprite = new Sprite(resumeButton);
        resumeButtonSprite.setSize(150, 40);
        resumeButtonSprite.setPosition(pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - resumeButtonSprite.getWidth()) / 2,
            pauseBackgroundSprite.getY() + (pauseBackgroundSprite.getHeight() - resumeButtonSprite.getHeight()) - 60);

        restartButtonSprite = new Sprite(restartButton);
        restartButtonSprite.setSize(150, 40);
        restartButtonSprite.setPosition(
            pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - restartButtonSprite.getWidth()) / 2,
            resumeButtonSprite.getY() - restartButtonSprite.getHeight() - 10);

        exitButtonSprite = new Sprite(exitButton);
        exitButtonSprite.setSize(150, 40);
        exitButtonSprite.setPosition(
            pauseBackgroundSprite.getX() + (pauseBackgroundSprite.getWidth() - exitButtonSprite.getWidth()) / 2,
            restartButtonSprite.getY() - exitButtonSprite.getHeight() - 10);

        playButtonSprite = new Sprite(playButton);
        playButtonSprite.setSize(150, 60);
        playButtonSprite.setPosition(viewport.getWorldWidth() / 2 - 75, viewport.getWorldHeight() / 2 - 30);

        obstacles = new Array<>();
        crackSprites = new Array<>();
        trafficIslandSprites = new Array<>();
        peopleSprites = new Array<>();

        initializeCracks();
        initializeTrafficIslands();
        initializePeople();

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(viewport.getCamera().combined);

    }

    @Override
    public void render() {
        movementAndButtons();
        logic();
        draw();

    }
    private boolean checkCollision(Sprite player, Sprite people) {
        return player.getBoundingRectangle().overlaps(people.getBoundingRectangle());
    }

    private void movementAndButtons() {
        touchPos.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(touchPos);

        //Buttons
        if (Gdx.input.justTouched()) {
            //only allows the play button to work if the game has not started
            if (!gameStarted) {
                if (playButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    gameStarted = true;
                    restartGame();
                }
                return;
            }

            if (isPaused) {
                if (resumeButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    isPaused = false;
                    ignoreNextTouch = true;
                    return;
                }

                if (restartButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    restartGame();
                    isPaused = false;
                    ignoreNextTouch = true;
                    return;
                }

                if (exitButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    gameStarted =   false;
                    isPaused = false;
                    ignoreNextTouch = true;
                    return;
                }
                return;
            }

            //if the pause button is clicked the game will be paused
            if (pauseButtonSprite.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                isPaused = true;
                return;
            }
        }

        if (ignoreNextTouch) {
            if (!Gdx.input.isTouched()) {
                ignoreNextTouch = false;
            }
            return;
        }

        //Player will not move if the game has not started or is paused
        if (!gameStarted || isPaused) return;

        //player movement
        if (Gdx.input.isTouched()) {
            float touchX = touchPos.x;
            float leftLane = viewport.getWorldWidth() / 3;
            float rightLane = 2 * viewport.getWorldWidth() / 3;

            if (touchX < leftLane) {
                playerSprite.setPosition(40f, playerSprite.getY());
            } else if (touchX > rightLane) {
                playerSprite.setPosition(viewport.getWorldWidth() - playerSprite.getWidth() - 40f, playerSprite.getY());
            } else {
                playerSprite.setPosition(viewport.getWorldWidth() / 2 - playerSprite.getWidth() / 2, playerSprite.getY());
            }
        }
    }

    private void restartGame() {
        score = 0;
        gameState = GameState.MENU;
        isPaused = false;

        playerSprite.setPosition(125, 20);

        scrollY = 0f;

        obstacles.clear();
        crackSprites.clear();
        trafficIslandSprites.clear();
        peopleSprites.clear();

        initializeCracks();
        initializeTrafficIslands();
        initializePeople();
    }

    private void initializeCracks() {
        int numberOfCracks = 2;
        float minDistance = 300f;

        for (int i = 0; i < numberOfCracks; i++) {
            //randomly select a crack texture
            Texture selectedTexture;
            int type = MathUtils.random(1, 3);
            if (type == 1) {
                selectedTexture = crack1;
            } else if (type == 2) {
                selectedTexture = crack2;
            } else {
                selectedTexture = crack3;
            }

            Sprite crackSprite = new Sprite(selectedTexture);
            crackSprite.setSize(80, 80);

            float x = lanes[MathUtils.random(0, lanes.length - 1)];
            float y = viewport.getWorldHeight() + i * minDistance;

            crackSprite.setPosition(x, y);
            obstacles.add(crackSprite);
            crackSprites.add(crackSprite);
        }
    }

    private void initializeTrafficIslands() {
        int numberOfTrafficIslands = 2;
        float minDistance = 300f;

        for (int i = 0; i < numberOfTrafficIslands; i++) {
            Sprite trafficIslandSprite = new Sprite(trafficIsland);
            trafficIslandSprite.setSize(80, 210);

            float x = lanes[MathUtils.random(0, lanes.length - 1)];
            float y = viewport.getWorldHeight() + i * minDistance;

            trafficIslandSprite.setPosition(x, y);
            trafficIslandSprites.add(trafficIslandSprite);
        }

    }

    private void initializePeople() {
        int numberOfPeople = 2;
        float minDistance = 300f;

        for (int i = 0; i < numberOfPeople; i++) {
            Sprite peopleSprite = new Sprite(people);
            peopleSprite.setSize(80, 100);

            float x = lanes[MathUtils.random(0, lanes.length - 1)];
            float y = viewport.getWorldHeight() + i * minDistance;

            peopleSprite.setPosition(x, y);
            peopleSprites.add(peopleSprite);
        }
    }

    private void logic() {
        if (!gameStarted || isPaused) return;

        // Increase speed based on score
        currentScrollSpeed = baseScrollSpeed + (score / 100f);
        if (currentScrollSpeed > maxSpeed) {
            currentScrollSpeed = maxSpeed;
        }

        scrollY -= currentScrollSpeed;
        if (scrollY <= -viewport.getWorldHeight()) {
            scrollY = 0;
        }

        for (Sprite crackSprite : crackSprites) {
            crackSprite.translateY(-currentScrollSpeed);

            if (crackSprite.getY() + crackSprite.getHeight() < 0) {
                float x = lanes[MathUtils.random(0, lanes.length - 1)];
                crackSprite.setPosition(x, viewport.getWorldHeight());
            }
        }

        for (Sprite trafficIslandSprite : trafficIslandSprites) {
            trafficIslandSprite.translateY(-currentScrollSpeed);

            if (trafficIslandSprite.getY() + trafficIslandSprite.getHeight() < 0) {
                float x = lanes[MathUtils.random(0, lanes.length - 1)];
                trafficIslandSprite.setPosition(x, viewport.getWorldHeight());
            }
        }

        for (Sprite peopleSprite : peopleSprites) {
            peopleSprite.translateY(-currentScrollSpeed);

            if (peopleSprite.getY() + peopleSprite.getHeight() < 0) {
                float x = lanes[MathUtils.random(0, lanes.length - 1)];
                peopleSprite.setPosition(x, viewport.getWorldHeight());
            }
        }

        // Check for collision and increase score
        for (int i = peopleSprites.size - 1; i >= 0; i--) {
            Sprite person = peopleSprites.get(i);
            if (checkCollision(playerSprite, person)) {
                score += 10; // Increase score for each person collected
                float x = lanes[MathUtils.random(0, lanes.length - 1)];
                person.setPosition(x, viewport.getWorldHeight());
            }
        }
    }


    private void draw() {
        batch.setProjectionMatrix(viewport.getCamera().combined);
        viewport.apply();

        batch.begin();

        float roadWidth = viewport.getWorldWidth();
        float roadHeight = viewport.getWorldHeight();

        batch.draw(road, 0, scrollY, roadWidth, roadHeight);
        batch.draw(road, 0, scrollY + roadHeight, roadWidth, roadHeight);

        if (!gameStarted) {
            playButtonSprite.draw(batch);
            batch.end();
            return;
        }

        for (Sprite crackSprite : crackSprites) {
            crackSprite.draw(batch);

        }

        for (Sprite trafficIslandSprite : trafficIslandSprites) {
            trafficIslandSprite.draw(batch);
        }

        for (Sprite peopleSprite : peopleSprites) {
            peopleSprite.draw(batch);
        }

        playerSprite.draw(batch);
        pauseButtonSprite.draw(batch);
        peopleCounterButtonSprite.draw(batch);

        if (isPaused) {
            pauseBackgroundSprite.draw(batch);
            resumeButtonSprite.draw(batch);
            restartButtonSprite.draw(batch);
            exitButtonSprite.draw(batch);
        }

        if (gameStarted) {
            font.draw(batch, "Score: " + score, 10, viewport.getWorldHeight() - 20);
        }

        batch.end();

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        font.dispose();
        road.dispose();
        player.dispose();
        crack1.dispose();
        crack2.dispose();
        crack3.dispose();
        trafficIsland.dispose();
    }
}
