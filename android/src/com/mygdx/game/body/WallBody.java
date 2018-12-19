package com.mygdx.game.body;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public class WallBody extends BaseBody {

    private float v1x, v1y, v2x, v2y;

    public WallBody(float v1x, float v1y, float v2x, float v2y) {
        this.v1x = v1x;
        this.v1y = v1y;
        this.v2x = v2x;
        this.v2y = v2y;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(0, 0);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(v1x, v1y, v2x, v2y);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = edgeShape;

        Body bodyEdgeScreen = world.createBody(bodyDef2);
        bodyEdgeScreen.createFixture(fixtureDef2);
        edgeShape.dispose();
        return bodyEdgeScreen;
    }
}
