package com.mygdx.game.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * @author lq.zeng
 * @date 2018/12/20
 */

public class ActorsImp implements IActors {
    @Override
    public Actor bgActor() {
        Image image = new Image(new Texture("timg.jpg"));
        image.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return image;
    }
}
