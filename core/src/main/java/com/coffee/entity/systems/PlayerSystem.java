package com.coffee.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.coffee.entity.EntityFactory;
import com.coffee.entity.components.*;
import com.coffee.util.Assets;
import com.coffee.util.Mapper;
import com.coffee.util.OptionsManager;

import java.awt.*;

/**
 * System that listens for input via fields in the {@link PlayerComponent},
 * moves the player, and gets the player to shoot bullets.
 *
 * @author Jared Tulayan
 */
public class PlayerSystem extends IteratingSystem {
    private final Dimension GAME_SIZE;
    private float timer;

    public PlayerSystem(Viewport viewport) {
        super(Family.all(PlayerComponent.class).get());

        GAME_SIZE = new Dimension((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
        timer = 0;
    }

    @Override
    public void update(float deltaTime) {
        timer += deltaTime;
        super.update(deltaTime);

        if (timer >= 1)
            timer = 0;
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = Mapper.PLAYER.get(entity);
        SpriteComponent sprite = Mapper.SPRITE.get(entity);
        MovementComponent move = Mapper.MOVEMENT.get(entity);
        TransformComponent transform = Mapper.TRANSFORM.get(entity);
        HealthComponent health = Mapper.HEALTH.get(entity);

        if (timer >= 1)
            player.timeAlive++;

        if (health.getHealthPercent() > 0) {
            // Update the shoot timer
            // Since we can, we need to clamp the value of the timer between 0 and the value of the shotsPerSecond
            // to avoid any overflow exceptions.
            player.shotsPerSecond = MathUtils.lerp(5, 10, player.upFireRate / 4f);
            player.shootTimer = MathUtils.clamp(player.shootTimer - player.shotsPerSecond * deltaTime, 0, 1);

            // Any invalid moves the player tries to take, we should combat ASAP.
            if (player.up == 1 && transform.POSITION.y + move.moveSpeed * deltaTime > GAME_SIZE.height * 2 / 3)
                player.up = 0;
            if (player.left == 1 && transform.POSITION.x + move.moveSpeed * deltaTime < 0)
                player.left = 0;
            if (player.down == 1 && transform.POSITION.y - move.moveSpeed * deltaTime < 64)
                player.down = 0;
            if (player.right == 1 && transform.POSITION.x + move.moveSpeed * deltaTime > GAME_SIZE.width - transform.SIZE.width)
                player.right = 0;

            // Shoot if we can
            if (player.shoot && player.shootTimer == 0) {
                Sound shoot = Assets.MANAGER.get(Assets.Audio.LASER_SHOOT);
                shoot.play(OptionsManager.sfxVolume);
                switch (player.upBulletDamage) {
                    case 1:
                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 8,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 8,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));
                        break;

                    case 2:
                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 16,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 16,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));
                        break;

                    case 3:
                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 8,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 24,
                                transform.POSITION.y + transform.SIZE.height - 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 8,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 24,
                                transform.POSITION.y + transform.SIZE.height - 10
                        ));
                        break;

                    case 4:
                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 16,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x + 32,
                                transform.POSITION.y + transform.SIZE.height - 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 16,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));

                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x - 32,
                                transform.POSITION.y + transform.SIZE.height - 10
                        ));
                        break;

                    default:
                        getEngine().addEntity(EntityFactory.createPlayerBullet(
                                transform.POSITION.x + transform.ORIGIN.x,
                                transform.POSITION.y + transform.SIZE.height + 10
                        ));
                        break;
                }
                player.shootTimer = 1;
                player.shotsFired += 1 + player.upBulletDamage;
            }

            // Make player a "ghost" while invincible
            if (health.invincible)
                sprite.SPRITES.get(0).setAlpha(0.5f);
            else
                sprite.SPRITES.get(0).setAlpha(1);

            // Update position
            move.moveSpeed = MathUtils.lerp(3, 5, player.upSpeed / 4.0f);
            move.MOVEMENT_NORMAL.set(player.right - player.left, player.up - player.down);

        } else {
            if (player.lives > 0) { // If we still got lives left, reset the player while not in control
                if (!player.revive) {
                    sprite.SPRITES.first().setAlpha(0);
                    move.MOVEMENT_NORMAL.setZero();
                    player.upBulletDamage = 0;
                    player.upFireRate = 0;
                    player.upSpeed = 0;
                    player.shotsPerSecond = 3;
                    player.shootTimer = player.shotsPerSecond;
                    transform.POSITION.set(GAME_SIZE.width / 2 - transform.ORIGIN.x, 128 - transform.ORIGIN.y);

                    health.respawnTimer = health.respawnDuration;
                    player.revive = true;
                } else if (health.respawnTimer == 0) { // Wait to revive, then respawn.
                    player.lives--;
                    health.invincibilityTimer = health.invincibilityDuration;
                    health.health = health.maxHealth;
                    sprite.SPRITES.first().setAlpha(1);
                    player.revive = false;
                }
            } else { // If we dead, just remove enough so that we can keep the gui but we are still dead.
                entity.remove(ColliderComponent.class);
                entity.remove(SpriteComponent.class);
                entity.remove(MovementComponent.class);
            }
        }
    }
}
