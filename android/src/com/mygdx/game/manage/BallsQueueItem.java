package com.mygdx.game.manage;

import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.sprite.NetSprite;

/**
 * @author lq.zeng
 * @date 2019/1/4
 */

public class BallsQueueItem {
    private NetSprite netSprite;
    private BallSprite ballSprite;
    private Body body;

    public NetSprite getNetSprite() {
        return netSprite;
    }

    public void setNetSprite(NetSprite netSprite) {
        this.netSprite = netSprite;
    }

    public BallSprite getBallSprite() {
        return ballSprite;
    }

    public void setBallSprite(BallSprite ballSprite) {
        this.ballSprite = ballSprite;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}
