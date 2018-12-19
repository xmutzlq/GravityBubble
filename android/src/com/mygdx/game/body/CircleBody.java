package com.mygdx.game.body;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public class CircleBody extends BaseBody {

    private BodyParams bodyParams;

    public CircleBody(BodyParams bodyParams) {
        this.bodyParams = bodyParams;
    }

    @Override
    public Body createBody(World world) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = bodyParams.bodyType;
        bodyDef.position.set(bodyParams.x, bodyParams.y);
        bodyDef.fixedRotation = true;

        CircleShape circleSmall = new CircleShape();
        circleSmall.setRadius(bodyParams.radius);

        FixtureDef fixtureDefSmall = new FixtureDef();
        fixtureDefSmall.shape = circleSmall;
        fixtureDefSmall.density = 1f;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDefSmall);
        return body;
    }

    public static class BodyParams {
        public BodyDef.BodyType bodyType; //类型
        public float x, y; //锚点
        public float radius; //半径

        public BodyParams() {
            bodyType = BodyDef.BodyType.DynamicBody;
            x = 0;
            y = 0;
            radius = 10;
        }
    }
}
