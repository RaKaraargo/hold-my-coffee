package com.coffee.entity;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.coffee.entity.components.*;
import com.coffee.main.Application;
import com.coffee.util.Assets;
import com.coffee.util.CollisionHandler;
import com.coffee.util.Mapper;
import com.kotcrab.vis.ui.util.ColorUtils;

/**
 * Builder class that automates the creation of entities and
 * attaching the necessary {@link Component}s onto them.
 */
public class EntityFactory {
    // Static fields that we can use throughout any instance of the EntityBuilder
    private static Viewport viewport;
    private static SpriteBatch batch;
    private static InputMultiplexer inputMultiplexer;
    private static TextureAtlas goAtlas;
    private static PooledEngine pooledEngine;

    // Boolean to check if the factory has already been pre-initialized.
    private static boolean initialized = false;

    /**
     * Initializes the {@link EntityFactory} static fields
     * only if it has not been done already.
     */
    public static void init() {
        if (!initialized) {
            Application app = (Application) Gdx.app.getApplicationListener();

            viewport = app.getViewport();
            batch = app.getBatch();
            inputMultiplexer = app.getInputMultiplexer();
            goAtlas = Assets.MANAGER.get(Assets.GameObjects.ATLAS);

            initialized = true;
        }
    }

    /**
     * Sets the {@link PooledEngine} to use for initializing poolable {@link Entity}s
     *
     * @param p the {@code PooledEngine} to use for pooling and creating poolable {@code Entity}s
     */
    public EntityFactory(PooledEngine p) {
        pooledEngine = p;
    }

    /**
     * Creates a player that can move and shoot. Note that you need to add the
     * {@link InputProcessor} to the game's {@link InputMultiplexer}; this builder does not do so.
     *
     * @return a player {@code Entity} that can move, shoot, and be killed.
     */
    public static Entity createPlayer() {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final MovementComponent MOVEMENT = new MovementComponent();
        final SpriteComponent SPRITE = new SpriteComponent();
        final PlayerComponent PLAYER = new PlayerComponent();
        final HealthComponent HEALTH = new HealthComponent(100, .2f);
        final ColliderComponent COLLIDER;
        final InputComponent INPUT;

        // Initialize MovmementComponent
        MOVEMENT.moveSpeed = 5;

        // Initialize SpriteComponent
        Sprite main = goAtlas.createSprite("player");
        main.setOrigin(main.getWidth() / 2, main.getHeight() / 2);

        SPRITE.SPRITES.add(main);

        // Initialize TransformComponent
        TRANSFORM.SIZE.setSize(main.getWidth(), main.getHeight());
        TRANSFORM.ORIGIN.set(main.getOriginX(), main.getOriginY());

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
                    case Input.Keys.SPACE:
                    case Input.Keys.SHIFT_LEFT:
                    case Input.Keys.SHIFT_RIGHT:
                        PLAYER.shoot = true;
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
                    case Input.Keys.SPACE:
                    case Input.Keys.SHIFT_LEFT:
                    case Input.Keys.SHIFT_RIGHT:
                        PLAYER.shoot = false;
                        break;
                    default:
                        return false;
                }
                return true;
            }
        };

        INPUT = new InputComponent(ip);

        PLAYER.bulletsPerSecond = 10;

        return E.add(TRANSFORM).add(MOVEMENT).add(COLLIDER).add(SPRITE).add(INPUT).add(PLAYER).add(HEALTH);
    }

    /**
     * Creates a bullet with a velocity of 10 at the specified location.
     *
     * @param x      the x-coordinate of the bullet
     * @param y      the y-coordinate of the bullet
     * @param rot    the rotation of the bullet
     * @param source the {@code Entity} that shot the bullet
     * @return an {@code Entity} with all the necessary components for a bullet
     */
    public static Entity createPlayerBullet(float x, float y, float rot, Entity source) {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final MovementComponent MOVEMENT = new MovementComponent();
        final SpriteComponent SPRITE = new SpriteComponent();
        final ColliderComponent COLLIDER;
        final BulletComponent BULLET = new BulletComponent();

        // Initialize SpriteComponent
        Sprite main = goAtlas.createSprite("bullet");
        main.setOrigin(main.getWidth(), main.getHeight() / 2);
        SPRITE.SPRITES.add(main);
        SPRITE.zIndex = -99;

        // Initialize TransformComponent
        TRANSFORM.SIZE.setSize(main.getWidth(), main.getHeight());
        TRANSFORM.ORIGIN.set(main.getOriginX(), main.getOriginY());
        TRANSFORM.POSITION.set(x - main.getOriginX(), y - main.getOriginY());
        TRANSFORM.rotation = rot;

        // Initialize MovementComponent
        MOVEMENT.moveSpeed = 10;
        MOVEMENT.MOVEMENT_NORMAL.set(Vector2.Y);

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
                4,0,
                4,4,
                0,4
        });
        COLLIDER.body.setOrigin(2, 2);
        COLLIDER.solid = false;

        return E.add(TRANSFORM).add(MOVEMENT).add(COLLIDER).add(SPRITE).add(BULLET);
    }

    /**
     * Create a spawner that randomly spawns power-ups
     *
     * @param x x location of bottom left corner
     * @param y y location of bottom left corner
     * @param engine {@link PooledEngine}
     * @return a random power-up spawner
     */
    public static Entity createRandomPowerUpSpawner(float x, float y, PooledEngine engine) {
        final PooledEngine ENGINE = engine;
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final Sprite REF = new Sprite(goAtlas.createSprite("upgrade_base"));
        final SpawnerComponent SPAWNER;

        //Set up Transform Component
        TRANSFORM.POSITION.set(x, y);

        //Set up Spawn Component
        SPAWNER = new SpawnerComponent(() -> {
            Array<Entity> spawnedEntities = new Array<Entity>(1);

            // Spawn at the top of the screen. Make sure that it isn't out of the reach of the player.
            float spawnX = MathUtils.random(REF.getWidth(), viewport.getWorldWidth() - REF.getWidth() * 2);
            float spawnY = viewport.getWorldHeight() + 32;
            int randomPowerUpSpawned = (int) ((Math.random() * 3));

            switch (randomPowerUpSpawned) {
                case 0: //health power up
                    spawnedEntities.add(createHealthPowerUp(spawnX, spawnY, ENGINE));
                    break;
                case 1: //speed power up
                    spawnedEntities.add(createSpeedPowerUp(spawnX, spawnY, ENGINE));
                    break;
                case 2: //create fire rate up
                    spawnedEntities.add(createFireRatePowerUp(spawnX, spawnY, ENGINE));
                    break;
            }

            return spawnedEntities;
        });
        SPAWNER.spawnRateMin = 2;
        SPAWNER.spawnRateMax = 4;

        return E.add(TRANSFORM).add(SPAWNER);
    }

    /**
     * Builder method for the basis of any powerup
     *
     * @param x the x-coordinate to spawn the powerup at
     * @param y the y-coordinate to spawn the powerup at
     * @return an {@code Entity} with the necessary {@code Component}s attached
     *         to make any powerup (sans the {@code ColliderComponent}).
     */
    public static Entity createBasePowerUp(float x, float y) {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final SpriteComponent SPRITE = new SpriteComponent();
        final MovementComponent MOVEMENT = new MovementComponent();

        //Set up Sprite Component
        Sprite
            base = goAtlas.createSprite("upgrade_base"),
            up = goAtlas.createSprite("up_arrow");

        base.setOriginCenter();
        up.setOriginCenter();

        SPRITE.SPRITES.add(base);
        SPRITE.SPRITES.add(up);

        // Set up TransformComponent
        TRANSFORM.SIZE.setSize(base.getWidth(), base.getHeight());
        TRANSFORM.ORIGIN.set(base.getOriginX(), base.getOriginY());
        TRANSFORM.POSITION.set(x - TRANSFORM.ORIGIN.x, y - TRANSFORM.ORIGIN.y);

        // Set up MovementComponent
        MOVEMENT.MOVEMENT_NORMAL.set(0, -1);
        MOVEMENT.moveSpeed = 4;

        return E.add(TRANSFORM).add(SPRITE).add(MOVEMENT);
    }

    /**
     * Creates a health power up that restores the player's health to max.
     */
    public static Entity createHealthPowerUp(float x, float y, PooledEngine engine) {
        final Entity E = createBasePowerUp(x, y);
        final TransformComponent TRANSFORM = Mapper.TRANSFORM.get(E);
        final SpriteComponent SPRITE = Mapper.SPRITE.get(E);
        final ColliderComponent COLLIDER;

        // Set up base sprite
        SPRITE.SPRITES.get(0).setColor(Color.RED);

        //Set up Collider Component
        COLLIDER = new ColliderComponent(new CollisionHandler() {
            @Override
            public void enterCollision(Entity entity) {
                if (Mapper.PLAYER.has(entity)) {
                    Mapper.HEALTH.get(entity).health = Mapper.HEALTH.get(entity).MAX_HEALTH;
                    System.out.println("Health up!");
                    engine.removeEntity(E);
                }
            }

            @Override
            public void whileCollision(Entity entity) {
            }

            @Override
            public void exitCollision(Entity entity) {
            }
        });

        COLLIDER.body = new Polygon(new float[] {
                0, 0,
                TRANSFORM.SIZE.width, 0,
                TRANSFORM.SIZE.width, TRANSFORM.SIZE.height,
                0, TRANSFORM.SIZE.height
        });
        COLLIDER.solid = false;
        COLLIDER.body.setOrigin(TRANSFORM.ORIGIN.x, TRANSFORM.ORIGIN.y);

        return E.add(COLLIDER);
    }

    /**
     * Creates a fire rate power up that increases the player's fire rate. Stacks up to 5 times.
     */
    public static Entity createFireRatePowerUp(float x, float y, PooledEngine engine) {
        final PooledEngine ENGINE = engine;
        final Entity E = createBasePowerUp(x, y);
        final TransformComponent TRANSFORM = Mapper.TRANSFORM.get(E);
        final SpriteComponent SPRITE = Mapper.SPRITE.get(E);
        final ColliderComponent COLLIDER;

        // Set up base sprite
        SPRITE.SPRITES.get(0).setColor(Color.YELLOW);

        //Set up Collider Component
        COLLIDER = new ColliderComponent(new CollisionHandler() {
            @Override
            public void enterCollision(Entity entity) {
                if (Mapper.PLAYER.has(entity)) {
                    PlayerComponent player = Mapper.PLAYER.get(entity);
                    player.bulletsPerSecond = MathUtils.clamp(player.bulletsPerSecond + 4, 0, 30);
                    System.out.println("Bullet Up : " + player.bulletsPerSecond);
                    ENGINE.removeEntity(E);
                }
            }

            @Override
            public void whileCollision(Entity entity) {
            }

            @Override
            public void exitCollision(Entity entity) {
            }
        });

        COLLIDER.body = new Polygon(new float[] {
                0, 0,
                TRANSFORM.SIZE.width, 0,
                TRANSFORM.SIZE.width, TRANSFORM.SIZE.height,
                0, TRANSFORM.SIZE.height
        });
        COLLIDER.solid = false;
        COLLIDER.body.setOrigin(TRANSFORM.ORIGIN.x, TRANSFORM.ORIGIN.y);

        return E.add(COLLIDER);
    }

    /**
     * Creates a speed power up that increases the player's speed. Stacks up to 5 times.
     */
    public static Entity createSpeedPowerUp(float x, float y, PooledEngine engine) {
        final PooledEngine ENGINE = engine;
        final Entity E = createBasePowerUp(x, y);
        final TransformComponent TRANSFORM = Mapper.TRANSFORM.get(E);
        final SpriteComponent SPRITE = Mapper.SPRITE.get(E);
        final ColliderComponent COLLIDER;

        // Set up base sprite
        SPRITE.SPRITES.get(0).setColor(Color.CYAN);

        //Set up Collider Component
        COLLIDER = new ColliderComponent(new CollisionHandler() {
            @Override
            public void enterCollision(Entity entity) {
                if (Mapper.PLAYER.has(entity)) {
                    MovementComponent move = Mapper.MOVEMENT.get(entity);
                    move.moveSpeed = MathUtils.clamp(move.moveSpeed + .5, 0, 7.5);
                    System.out.println("Speed Up : " + move.moveSpeed);
                    ENGINE.removeEntity(E);
                }
            }

            @Override
            public void whileCollision(Entity entity) {
            }

            @Override
            public void exitCollision(Entity entity) {
            }
        });

        COLLIDER.body = new Polygon(new float[] {
                0, 0,
                TRANSFORM.SIZE.width, 0,
                TRANSFORM.SIZE.width, TRANSFORM.SIZE.height,
                0, TRANSFORM.SIZE.height
        });
        COLLIDER.solid = false;
        COLLIDER.body.setOrigin(TRANSFORM.ORIGIN.x, TRANSFORM.ORIGIN.y);

        return E.add(COLLIDER);
    }

    /**
     * Creates a star based on its z-index and temperature
     *
     * @param z   the depth of the star
     * @param hue the hue of the star
     * @return an {@link Entity} with a sprite made to look like a star
     */
    public static Entity createStar(float x, float y, int z, float hue) {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final MovementComponent MOVEMENT = new MovementComponent();
        final SpriteComponent SPRITE = new SpriteComponent();

        // Clamp z between 0 and 100
        z = MathUtils.clamp(z, 0, 100);

        // Create star sprite
        Sprite main = new Sprite(goAtlas.createSprite("star1"));
        float val = MathUtils.lerp(10, 100, 100);
        Color tint = ColorUtils.HSVtoRGB(hue, MathUtils.random(15, 30), val);
        float size = MathUtils.lerp(3, 6, (100 - z) / 100f);
        main.setSize(size, size);
        main.setOriginCenter();
        main.setColor(tint);
        SPRITE.SPRITES.add(main);
        SPRITE.zIndex = -100 - z;

        // Initialize transform
        TRANSFORM.SIZE.setSize(main.getWidth(), main.getHeight());
        TRANSFORM.ORIGIN.set(main.getOriginX(), main.getOriginY());
        TRANSFORM.POSITION.set(x - TRANSFORM.ORIGIN.x, y - TRANSFORM.ORIGIN.y);

        // Initialize movement
        MOVEMENT.MOVEMENT_NORMAL.set(0, -1);
        MOVEMENT.moveSpeed = MathUtils.lerp(3, 10, (100 - z) / 100f);

        return E.add(TRANSFORM).add(MOVEMENT).add(SPRITE);
    }

    /**
     * Creates a particle generator that simulates traveling through space.
     *
     * @return an {@link Entity} with a {@link SpawnerComponent} configured to spawn stars, asteroids, etc.
     */
    public static Entity createParticleGenerator() {
        final Entity E = new Entity();
        final TransformComponent TRANSFORM = new TransformComponent();
        final SpawnerComponent SPAWNER;

        TRANSFORM.SIZE.setSize(viewport.getWorldWidth(), viewport.getWorldHeight());
        TRANSFORM.POSITION.set(0, 0);
        TRANSFORM.ORIGIN.set(TRANSFORM.SIZE.width / 2f, TRANSFORM.SIZE.height / 2f);

        SPAWNER = new SpawnerComponent(() -> {
            Array<Entity> entities = new Array<>();

            float x = MathUtils.random(0, viewport.getWorldWidth());
            float y = viewport.getWorldHeight();
            int z = MathUtils.random(0, 100);
            float hue = MathUtils.random(180, 300);

            entities.add(createStar(x, y, z, hue));

            return entities;
        });

        SPAWNER.spawnRateMin = 0.02;
        SPAWNER.spawnRateMax = 0.05;

        return E.add(TRANSFORM).add(SPAWNER);
    }
}