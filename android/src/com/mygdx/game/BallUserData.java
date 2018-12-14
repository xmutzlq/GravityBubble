package com.mygdx.game;

/**
 * @author lq.zeng
 * @date 2018/12/13
 */

public class BallUserData {
    int position; //索引
    int color; //颜色
    boolean isSelected; //是否选中
    boolean isNeedGravity; //是否需要引力
    boolean isOnDrag; //是否拖拽

    public BallUserData() {}

    public BallUserData(int position, int color, boolean isSelected, boolean isNeedGravity) {
        this.position = position;
        this.color = color;
        this.isSelected = isSelected;
        this.isNeedGravity = isNeedGravity;
    }
}