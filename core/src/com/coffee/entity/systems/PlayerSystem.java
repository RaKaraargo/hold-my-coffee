package com.coffee.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.coffee.entity.components.MovementComponent;
import com.coffee.entity.components.PlayerComponent;
import com.coffee.entity.components.TransformComponent;
import com.coffee.util.Mapper;

import java.awt.*;

/**
 * System that listens for input via fields in the {@link PlayerComponent},
 * moves the player, and gets the player to shoot bullets.
 *
 * @author Jared Tulayan
 */
public class PlayerSystem extends IteratingSystem {
    private final Dimension GAME_SIZE;

    public PlayerSystem(Viewport viewport) {
        super(Family.all(PlayerComponent.class).get());

        GAME_SIZE = new Dimension((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        PlayerComponent player = Mapper.PLAYER.get(entity);
        MovementComponent move = Mapper.MOVEMENT.get(entity);
        TransformComponent transform = Mapper.TRANSFORM.get(entity);

        // Update the shoot timer
        // Since we can, we need to clamp the value of the timer between 0 and the value of the fireRate
        // to avoid any overflow exceptions.
        player.shootTimer = MathUtils.clamp(player.shootTimer - deltaTime, 0, player.fireRate);

        // Any invalid moves the player tries to take, we should combat ASAP.
        if (player.up == 1 && transform.POSITION.y + move.moveSpeed * deltaTime > GAME_SIZE.height - 500)
            player.up = 0;
        if (player.left == 1 && transform.POSITION.x + move.moveSpeed * deltaTime < 0)
            player.left = 0;
        if (player.down == 1 && transform.POSITION.y - move.moveSpeed * deltaTime < 0)
            player.down = 0;
        if (player.right == 1 && transform.POSITION.x + move.moveSpeed * deltaTime > GAME_SIZE.width - transform.SIZE.width)
            player.right = 0;

        // Shoot if we can
        if (player.shoot && player.shootTimer == 0) {

            player.shootTimer = player.fireRate;
        }

        // Update position
        move.MOVEMENT_NORMAL.set(player.right - player.left, player.up - player.down);
    }
}
