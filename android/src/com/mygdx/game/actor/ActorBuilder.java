package com.mygdx.game.actor;

import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * @author lq.zeng
 * @date 2018/12/20
 */

public class ActorBuilder {
    IActors actors;
    public ActorBuilder(IActors actors) {
        this.actors = actors;
    }

    public Actor getBgActor() {
        return actors.bgActor();
    }
}
