package com.mygdx.game.body;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import java.io.Serializable;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public class CacheBody extends Body implements Serializable {

    /**
     * Constructs a new body with the given address
     *
     * @param world the world
     * @param addr  the address
     */
    protected CacheBody(World world, long addr) {
        super(world, addr);
    }
}
