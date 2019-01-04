package com.mygdx.game.manage;

import android.graphics.Bitmap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.entity.BallData;
import com.mygdx.game.entity.DataModel;
import com.mygdx.game.sprite.NetSprite;
import com.mygdx.game.util.ImageUtils;

import java.util.ArrayList;

/**
 * @author lq.zeng
 * @date 2018/12/17
 */

public class BallsManage {
    public static final String TAG = "zlq";

    private ArrayList<BallData> ballData; //小球属性
    public ArrayList<BallSprite> ballSprites; //带小球属性的SpriteBatch
    public ArrayList<BallSprite> ballSpritesCache; //带小球属性的SpriteBatch缓存

    public ArrayList<NetSprite> netSprites; //网络精灵
    public ArrayList<Body> bodies; //引力球Body

    public BallSprite referenceSpriteBatch; //参照物SpriteBatch
    public Body referenceBody; //参照物Body
    public Sprite referenceCircle; //参照物精灵

    public int cachePosition = -1;

    private boolean isJoin;

    private IBallOperateCallBack mBallRemoveCallBack;

    private BallsManage() {
        ballSprites = new ArrayList<>();
        ballSpritesCache = new ArrayList<>();
        bodies = new ArrayList<>();
        netSprites = new ArrayList<>();
    }

    private static class SingletonInstance {
        private static final BallsManage INSTANCE = new BallsManage();
    }

    public static BallsManage getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void loadPicFromNet() {
        for (String url : DataModel.netImages) {
            loadPicFromNet(url);
        }
    }

    private void loadPicFromNet(String url) {
        // 1.创建请求构建器
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        // 2.构建请求对象
        Net.HttpRequest httpRequest = requestBuilder.newRequest().method(Net.HttpMethods.GET).url(url).build();
        // 3.发送请求, 监听结果回调
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {

            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                // 获取响应状态
                HttpStatus httpStatus = httpResponse.getStatus();
                // 请求成功
                if (httpStatus.getStatusCode() == 200) {
                    Gdx.app.log(TAG, "请求成功");
                    // 以字节数组的方式获取响应内容
                    byte[] result = httpResponse.getResult();
                    // 还可以以流或字符串的方式获取
                    // httpResponse.getResultAsStream();
                    // httpResponse.getResultAsString();
                    Bitmap bitmap = ImageUtils.bytes2Bitmap(result);
                    Bitmap transparentBitmap = ImageUtils.getTransparentBitmap(bitmap, 90);
                    Bitmap roundBitmap = ImageUtils.toRound(transparentBitmap, true);
                    Bitmap frameRoundBitmap = ImageUtils.addFrame(roundBitmap, 3, 0, true);
                    final byte[] realResult = ImageUtils.bitmap2Bytes(frameRoundBitmap, Bitmap.CompressFormat.PNG);
                    /*
                     * 在响应回调中属于其他线程, 获取到响应结果后需要
                     * 提交到 渲染线程（create 和 render 方法执行所在线程） 处理。
                     */
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            // 把字节数组加载为 Pixmap
                            Pixmap pixmap = new Pixmap(realResult, 0, realResult.length);
                            // 把 pixmap 加载为纹理
                            Texture texture = new Texture(pixmap);
                            // pixmap 不再需要使用到, 释放内存占用
                            pixmap.dispose();
                            // 使用纹理
                            NetSprite sprite = new NetSprite();
                            sprite.createSprite(texture);
                            // 添加精灵
                            netSprites.add(sprite);
                        }
                    });
                } else {
                    Gdx.app.error(TAG, "请求失败, 状态码: " + httpStatus.getStatusCode());
                    NetSprite sprite = new NetSprite();
                    sprite.createDefaultSprite();
                    netSprites.add(sprite);
                }
            }

            @Override
            public void failed(Throwable throwable) {
                Gdx.app.error(TAG, "请求失败", throwable);
                NetSprite sprite = new NetSprite();
                sprite.createDefaultSprite();
                netSprites.add(sprite);
            }

            @Override
            public void cancelled() {
                Gdx.app.log(TAG, "请求被取消");
                NetSprite sprite = new NetSprite();
                sprite.createDefaultSprite();
                netSprites.add(sprite);
            }
        });
    }

    public void replaceReferenceSprite(Sprite sprite) {
        referenceCircle = sprite;
    }

    public void setBallRemoveCallBack(IBallOperateCallBack ballRemoveCallBack) {
        mBallRemoveCallBack = ballRemoveCallBack;
    }

    public void initBallSprites(ArrayList<BallData> ballData) {
        this.ballData = ballData;

        referenceSpriteBatch = new BallSprite();
        referenceSpriteBatch.size = ballData.get(0).size;
        referenceSpriteBatch.color = ballData.get(0).color;

        for (BallData data : ballData) {
            BallSprite ballSprite = new BallSprite();
            ballSprite.size = data.size;
            ballSprite.color = data.color;
            ballSprites.add(ballSprite);
        }
    }

    public void removeBall(int position) {
        isJoin = !isJoin;
        cachePosition = position;
        if(mBallRemoveCallBack != null && position <= bodies.size() - 1) {
            mBallRemoveCallBack.onBallDetach(bodies.get(position));
            replaceReferenceSprite(netSprites.get(position).mSprite);
            netSprites.remove(position);
            ballSprites.remove(position);
            bodies.remove(position);
        }
    }

    public void addBall() {
        if(mBallRemoveCallBack != null) {
            BallSprite ballSprite = new BallSprite();
            ballSprite.size = ballData.get(cachePosition).size;
            ballSprite.color = ballData.get(cachePosition).color;
            ballSprites.add(cachePosition, ballSprite);
            Body body = mBallRemoveCallBack.onBallAttach(cachePosition);
            bodies.add(cachePosition, body);
            cachePosition = -1;
        }
    }

    public void release() {
        for (SpriteBatch spritebatch : ballSprites) {
            spritebatch.dispose();
        }
        clearReference();
        ballData.clear();
        ballSprites.clear();
        bodies.clear();
        netSprites.clear();
    }

    public void clearReference() {
        referenceSpriteBatch.dispose();
    }

    public interface IBallOperateCallBack {
        void onBallDetach(Body body);
        Body onBallAttach(int pos);
    }
}
