package com.coffee.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Array;

/**
 * Contains an ArrayList of sprites that will be drawn in the DrawSystem.
 * Sprites with lower indices are drawn on the bottom.
 * @author Phillip O'Reggio
 */
public class SpriteComponent implements Component {
    public Array<Sprite> sprites;
    public int zIndex;

    public SpriteComponent(Array<Sprite> s) {
        sprites = s;
    }

    public SpriteComponent(Sprite[] s, int z) {
        sprites = new Array<Sprite>(s);
        zIndex = z;
    }
}


