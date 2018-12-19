package com.mygdx.game.body;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public abstract class BaseBody {
    public abstract Body createBody(World world);
}
