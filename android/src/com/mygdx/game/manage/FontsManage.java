package com.mygdx.game.manage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.mygdx.game.MyGdxGame;

/**
 * @author lq.zeng
 * @date 2019/1/2
 */

public class FontsManage {

    private BitmapFont font;

    public SpriteBatch batch;

    private float referenceRadius; //参照物半径

    private static class SingletonInstance {
        private static final FontsManage INSTANCE = new FontsManage();
    }

    public static FontsManage getInstance() {
        return FontsManage.SingletonInstance.INSTANCE;
    }

    public void setReferenceRadius(float referenceRadius) {
        this.referenceRadius = referenceRadius;
    }

    public void create() {
        batch = new SpriteBatch();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/simkai.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter freeTypeFontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        freeTypeFontParameter.characters = "拖拽到这里查看";
        freeTypeFontParameter.size = 16;

        font = generator.generateFont(freeTypeFontParameter);// 这里需要把你要输出的字，全部写上，前提是不能有重复的字。
        font.setColor(Color.WHITE);

        generator.dispose();
    }

    public void dispose() {
        font.dispose();
        batch.dispose();
    }

    public void render() {
        batch.begin();
        font.draw(batch, "拖拽到这里查看", -referenceRadius * MyGdxGame.PPM / 2 - 10, -30);
        batch.end();
    }
}
