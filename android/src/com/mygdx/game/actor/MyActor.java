package com.mygdx.game.actor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * @author lq.zeng
 * @date 2018/12/20
 */

public class MyActor extends Actor implements Pool.Poolable {
    private int tag;

    private Texture bgTexture;

    private TextureRegion bgTextureRegion;

    public MyActor() {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a);

        float x = getX();
        float y = getY();
        float scaleX = getScaleX();
        float scaleY = getScaleY();

        float width = getWidth();
        float height = getHeight();

        if (bgTexture != null) {
            batch.draw(bgTexture, x, y, getOriginX(), getOriginY(), getWidth(), getHeight(), scaleX, scaleY,
                    getRotation(), 0, 0, (int) width, (int) height, false, false);
        }

        if (bgTextureRegion != null) {
            if (bgTextureRegion instanceof Sprite) {
                Sprite sprite = (Sprite) bgTextureRegion;
                sprite.setColor(batch.getColor());
                sprite.setOrigin(getOriginX(), getOriginY());
                sprite.setPosition(x, y);
                sprite.setScale(scaleX, scaleY);
                sprite.setSize(width, height);
                sprite.setRotation(getRotation());
                sprite.draw(batch);
            } else {
                batch.draw(bgTextureRegion, x, y, getOriginX(), getOriginY(), width, height, scaleX, scaleY,
                        getRotation());
            }
        }
    }

    public void setBgTextureWithSize(Texture bgTexture, int width, int height) {
        this.bgTexture = bgTexture;
        if (bgTexture != null) {
            setSize(width, height);
        }
        setOrigin(Align.center);
    }

    public void setBgTexture(Texture bgTexture) {
        this.bgTexture = bgTexture;
        if (bgTexture != null) {
            setSize(bgTexture.getWidth(), bgTexture.getHeight());
        }
        setOrigin(Align.center);
    }

    /**
     *
     * <pre>
     * 使用缓存池
     *
     * date: 2015-1-3
     * </pre>
     *
     * @author caohao
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends MyActor> T obtain(Class<T> type) {
        Pool<MyActor> pool = (Pool<MyActor>) Pools.get(type);
        MyActor actor = pool.obtain();
        actor.setBgTexture(null);
        return (T) actor;
    }

    public static MyActor obtain() {
        return obtain(MyActor.class);
    }

    @Override
    public void reset() {
        // 初始化
        this.bgTexture = null;
        this.bgTextureRegion = null;
        setScale(1);
        setRotation(0);
        clear();
        setUserObject(null);
        this.setColor(new Color(1, 1, 1, 1));
        setStage(null);
        setParent(null);
        setVisible(true);
        setName(null);
        setOrigin(Align.center);
        setPosition(0, 0);
    }

    public Texture getBgTexture() {
        return bgTexture;
    }

    public TextureRegion getBgTextureRegion() {
        return bgTextureRegion;
    }

    public void setBgTextureRegion(TextureRegion textureRegion) {
        this.bgTextureRegion = textureRegion;
        if (bgTextureRegion != null) {
            if (bgTextureRegion instanceof Sprite) {
                Sprite sprite = (Sprite) bgTextureRegion;
                setSize(sprite.getWidth(), sprite.getHeight());
            } else if (bgTextureRegion instanceof TextureAtlas.AtlasRegion) {
                TextureAtlas.AtlasRegion atlasRegion = (TextureAtlas.AtlasRegion) bgTextureRegion;
                bgTextureRegion = createSprite(atlasRegion);
                Sprite sprite = (Sprite) bgTextureRegion;
                setSize(sprite.getWidth(), sprite.getHeight());
            } else {
                setSize(bgTextureRegion.getRegionWidth(), bgTextureRegion.getRegionHeight());
            }
        }

        setOrigin(Align.center);
    }

    private Sprite createSprite (TextureAtlas.AtlasRegion region) {
        if (region.rotate) {
            Sprite sprite = new Sprite(region);
            sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
            sprite.rotate90(true);
            return sprite;
        }
        return new Sprite(region);
    }

    @Override
    public boolean remove() {
        boolean remove = super.remove();
        if (remove) {
            Pools.free(this);
        }
        return remove;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

}
