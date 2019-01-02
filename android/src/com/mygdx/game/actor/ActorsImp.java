package com.mygdx.game.actor;

import android.graphics.Color;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

/**
 * @author lq.zeng
 * @date 2018/12/20
 */

public class ActorsImp implements IActors {

    public final String ID_BG_ACTOR = "bg_actor";

    @Override
    public Actor bgActor() {
        Image image = new Image(new Texture("timg.jpg"));
        image.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        return image;
    }

    @Override
    public Actor shownTvActor() {
        Label label = new Label("拖拽到这儿查看", new Label.LabelStyle(new BitmapFont(),
                new com.badlogic.gdx.graphics.Color(Color.WHITE)));
        label.setPosition(Gdx.graphics.getWidth() - label.getPrefWidth(), Gdx.graphics.getHeight() - label.getPrefHeight());
        return label;
    }
}
