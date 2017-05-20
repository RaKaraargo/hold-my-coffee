package com.coffee.entity;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.coffee.entity.components.*;
import com.coffee.main.Application;
import com.coffee.util.CollisionHandler;

import java.awt.*;

/**
 * Builder class that automates the creation of entities and
 * attaching the necessary {@link Component}s onto them.
 */
public class EntityBuilder {
    // Please never change these values once they have been initialized.
    // I will throw a hissy fit otherwise.
    // - Game
    private static Viewport viewport;
    private static PooledEngine engine;
    private static Batch batch;
    private static InputMultiplexer inputMultiplexer;

    private static EntityBuilder _inst;

    /**
     * Initializes the {@link EntityBuilder} instance only if it has not been already.
     *
     * @param a the {@code Application} to feed into the {@code EntityBuilder} constructor
     */
    public static void init(Application a) {
        if (_inst == null) {
            _inst = new EntityBuilder(a);

            System.out.println("Initialized EntityBuilder");
        }
    }

    /**
     * Initializes the constants in the class.
     *
     * @param app the {@code Application} to take the {@code Viewport}, {@code Engine}, {@code InputMultiplexer}, and {@code Batch} from
     */
    public EntityBuilder(Application app) {
        viewport = app.getViewport();
        engine = app.getEngine();
        batch = app.getBatch();
        inputMultiplexer = app.getInputMultiplexer();
    }

    /**
     * Creates a player that can move and shoot.
     *
     * @return a player {@code Entity} that can move, shoot, and be killed.
     */
    public static Entity createPlayer() {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final MovementComponent MOVEMENT = new MovementComponent();
        final SpriteComponent SPRITE = new SpriteComponent();
        final PlayerComponent PLAYER = new PlayerComponent();
        final ColliderComponent COLLIDER;
        final InputComponent INPUT;

        // Initialize TransformComponent
        TRANSFORM.SIZE.setSize(48, 48);
        TRANSFORM.ORIGIN.set(24, 24);

        // Initialize MovmementComponent
        MOVEMENT.moveSpeed = 5;

        // Initialize SpriteComponent
        Sprite main = new Sprite(new Texture("badlogic.jpg"));
        main.setSize(48, 48);
        main.setOrigin(24, 24);

        SPRITE.SPRITES.add(main);

        // Initialize ColliderComponent
        COLLIDER = new ColliderComponent(new CollisionHandler() {
            @Override
            public void enterCollision(Entity entity) {

            }

            @Override
            public void whileCollision(Entity entity) {

            }

            @Override
            public void exitCollision(Entity entity) {

            }
        });
        COLLIDER.body.setVertices(new float[]{
                0,0,
                16,0,
                16,16,
                0,16
        });
        COLLIDER.body.setOrigin(8, 8);
        COLLIDER.solid = true;

        // Initialize InputComponent
        InputProcessor ip = new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        PLAYER.up = 1;
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        PLAYER.left = 1;
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        PLAYER.down = 1;
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        PLAYER.right = 1;
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public boolean keyUp(int keycode) {
                switch (keycode) {
                    case Input.Keys.W:
                    case Input.Keys.UP:
                        PLAYER.up = 0;
                        break;
                    case Input.Keys.A:
                    case Input.Keys.LEFT:
                        PLAYER.left = 0;
                        break;
                    case Input.Keys.S:
                    case Input.Keys.DOWN:
                        PLAYER.down = 0;
                        break;
                    case Input.Keys.D:
                    case Input.Keys.RIGHT:
                        PLAYER.right = 0;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        };

        inputMultiplexer.addProcessor(ip);

        INPUT = new InputComponent(ip);

        return E.add(TRANSFORM).add(MOVEMENT).add(COLLIDER).add(SPRITE).add(INPUT).add(PLAYER);
    }


}