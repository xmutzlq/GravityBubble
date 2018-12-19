package com.mygdx.game.sprite;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * @author lq.zeng
 * @date 2018/12/19
 */

public class NetSprite {
    private static final String TEXTURE_PATH = "circle.png";
    public Sprite mSprite; //精灵

    public void createSprite(Texture texture) {
        if(texture == null) {
            texture = new Texture(TEXTURE_PATH);
        }
        mSprite = new Sprite(texture);
    }

    public void createDefaultSprite() {
        mSprite = new Sprite(new Texture(TEXTURE_PATH));
    }
}
