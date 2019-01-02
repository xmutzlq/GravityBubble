package com.mygdx.game.manage;

/**
 * @author lq.zeng
 * @date 2018/12/13
 */

public class BallUserData {
    public int position; //索引
    public int color; //颜色
    public float radius; //半径
    public boolean isSelected; //是否选中
    public boolean isNeedGravity; //是否需要引力
    public boolean isOnDrag; //是否拖拽

    public BallUserData() {}

    public BallUserData(int position, int color, float radius, boolean isSelected, boolean isNeedGravity) {
        this.position = position;
        this.color = color;
        this.radius = radius;
        this.isSelected = isSelected;
        this.isNeedGravity = isNeedGravity;
    }
}
