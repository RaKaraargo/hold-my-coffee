package com.coffee.util;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

/**
 * {@link AssetManager} wrapper class that contains some {@link AssetDescriptor}s
 * to easily load in assets on the fly.
 *
 * @author Jared Tulayan
 */
public class Assets {
    public static final AssetManager MANAGER;


    // Initialize the asset manager by constructing it
    // and placing all the assets into the loading queue.
    static {
        MANAGER = new AssetManager();

        MANAGER.load(GameObjects.ATLAS);
    }

    /**
     * Subclass containing {@link AssetDescriptor}s for game object textures
     */
    public static class GameObjects {
        public static AssetDescriptor<TextureAtlas> ATLAS = new AssetDescriptor<TextureAtlas>("gameobjects.pack", TextureAtlas.class);
    }
}
