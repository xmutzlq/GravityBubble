package com.mygdx.game;

import android.app.Activity;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.mygdx.game.actor.ActorBuilder;
import com.mygdx.game.actor.ActorsImp;
import com.mygdx.game.body.BaseBody;
import com.mygdx.game.body.CircleBody;
import com.mygdx.game.body.WallBody;
import com.mygdx.game.entity.BallData;
import com.mygdx.game.util.ScreenUtil;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter implements GestureDetector.GestureListener, InputProcessor, BallsManage.IBallOperateCallBack {
    private static final String TAG = "zlq";
    private static final float PPM = 30f;
    private static final int ReferenceBallScale = 12;

    private Activity mActivity;

    private int width, height;

    private Box2DDebugRenderer m_debugRenderer;
    private OrthographicCamera camera;
    private World world;
    private Stage stage;

    private ArrayList<BallData> ballData;

    private BallsManage mBallsManage;
    private ActorBuilder mActorBuilder;

    private Vector2 reversVec; //反向力
    private Vector3 point;
    private Body bodyThatWasHit;

    private boolean inReference;
    private BallUserData tmpBallUserData; // 小球的属性

    private float referenceRadius;
    private int radiusUsed;
    private int touchPosition;

    MyGdxGame(Activity activity, ArrayList<BallData> ballData) {
        this.mActivity = activity;
        this.ballData = ballData;
    }

    @Override
    public void create() {
        // 设置Log输出级别
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        width = Gdx.graphics.getWidth();
        height = Gdx.graphics.getHeight();
        float cameraWidth = width / 2;
        float cameraHeight = height / 2;

        mBallsManage = BallsManage.getInstance();
        mBallsManage.setBallRemoveCallBack(this);

        //演着构建器
        mActorBuilder = new ActorBuilder(new ActorsImp());

        //舞台
        stage = new Stage(new ScalingViewport(Scaling.stretch, width, height, new OrthographicCamera()));
        stage.addActor(mActorBuilder.getBgActor());

        //手势
        InputMultiplexer im = new InputMultiplexer();
        im.addProcessor(stage); // 添加舞台
        im.addProcessor(this);
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        Gdx.input.setInputProcessor(im);

        //半径横竖屏适配
        radiusUsed = ScreenUtil.isScreenPortrait(mActivity) ? width : height;

        //设置视角的大小
        camera = new OrthographicCamera();
        camera.setToOrtho(false, cameraWidth, cameraHeight);

        //(0,0)太空失重
        world = new World(new Vector2(0, 0), false);
        //描绘器
        m_debugRenderer = new Box2DDebugRenderer();
        m_debugRenderer.setDrawAABBs(true);
        m_debugRenderer.setDrawVelocities(true);

        //相机位置
        point = new Vector3(cameraWidth, cameraHeight, 0);
        camera.unproject(point);

        //创建墙
        createWall(-point.x / (PPM), point.y / (PPM), point.x / (PPM), point.y / (PPM));     //top wall
        createWall(-point.x / (PPM), -point.y / (PPM), point.x / (PPM), -point.y / (PPM));   //bottom wall
        createWall(-point.x / (PPM), point.y / (PPM), -point.x / (PPM), -point.y / (PPM));   //left wall
        createWall(point.x / (PPM), point.y / (PPM), point.x / (PPM), -point.y / (PPM));     //right wall

        mBallsManage.referenceCircle = new Sprite(new Texture("circle.png"));
        mBallsManage.initBallSprites(ballData);

        //创建参照物
        createReferenceBody();
        //创建其他小球
        for (int i = 0; i < mBallsManage.ballSprites.size(); i++) {
            float x = (float) (Math.random() * (width / PPM - mBallsManage.ballSprites.get(i).size) + mBallsManage.ballSprites.get(i).size);
            float y = (float) (Math.random() * (height / PPM - mBallsManage.ballSprites.get(i).size) + mBallsManage.ballSprites.get(i).size);
            createNormalBody(x, y, i);
        }

        moveToCenter();

        mBallsManage.loadPicFromNet();
    }

    @Override
    public void render() {
        update();
        Gdx.gl.glClearColor(255f, 255f, 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        m_debugRenderer.render(world, camera.combined);

        stage.act();
        stage.draw();

        //参照物
        BallSprite spriteBatchReference = mBallsManage.referenceSpriteBatch;
        spriteBatchReference.begin();
        spriteBatchReference.draw(mBallsManage.referenceCircle,
                mBallsManage.referenceBody.getWorldCenter().x - radiusUsed / ReferenceBallScale,
                mBallsManage.referenceBody.getWorldCenter().y - radiusUsed / ReferenceBallScale,
                radiusUsed / 6, radiusUsed / 6);
        spriteBatchReference.end();

        //其他小球
        for (int i = 0; i < mBallsManage.bodies.size(); i++) {
            Body body = mBallsManage.bodies.get(i);
            batchUpdate(i, mBallsManage.ballSprites.get(i), body, mBallsManage.ballSprites.get(i));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    public void update() {
        world.step(1 / 60f, 6, 2);
        for (Body body : mBallsManage.bodies) {
            gravityUpdate(body, true);
        }
        cameraUpdate();
    }

    /**小球位置刷新**/
    public void batchUpdate(int pos, SpriteBatch batchCircle, Body round, BallSprite ballSprite) {
        BallUserData userData = (BallUserData) round.getUserData();
        float ballSize = ballSprite.size;
        batchCircle.begin();
        if(pos <= mBallsManage.netSprites.size() - 1 && mBallsManage.netSprites.get(pos).mSprite != null) {
            if(userData != null && userData.isOnDrag) {
                batchCircle.draw(mBallsManage.netSprites.get(pos).mSprite,
                        point.x - ballSize / 2, point.y - ballSize / 2, ballSize, ballSize);
            } else {
                batchCircle.draw(mBallsManage.netSprites.get(pos).mSprite,
                        round.getPosition().x * PPM - ballSize / 2, round.getPosition().y * PPM - ballSize / 2, ballSize, ballSize);
            }
        }
        batchCircle.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / 2, height / 2);
    }

    @Override
    public void dispose() {
        m_debugRenderer.dispose();
        world.dispose();
        stage.dispose();
        mBallsManage.release();
    }

    /** 创建墙体 **/
    public void createWall(float w1, float h1, float w2, float h2) {
        BaseBody baseBody = new WallBody(w1, h1, w2, h2);
        baseBody.createBody(world);
    }

    /**创建参照物球体**/
    private void createReferenceBody() {
        referenceRadius = radiusUsed / ReferenceBallScale / PPM;
        Body body = createBody(BodyDef.BodyType.StaticBody, 0, 0, referenceRadius);
        body.setUserData(new BallUserData(-1, mBallsManage.referenceSpriteBatch.color, referenceRadius, false, true));
        mBallsManage.referenceBody = body;
    }

    /**创建普通球体**/
    private void createNormalBody(float x, float y, int pos) {
        float radius = mBallsManage.ballSprites.get(pos).size / 2 / PPM;
        Body body = createBody(BodyDef.BodyType.DynamicBody, x / PPM, y / PPM, radius);
        body.setUserData(new BallUserData(pos, mBallsManage.ballSprites.get(pos).color, radius, false, true));
        mBallsManage.bodies.add(body);
    }

    /**创建球体**/
    public Body createBody(BodyDef.BodyType boyType, float x, float y, float radius) {
        CircleBody.BodyParams bodyParams = new CircleBody.BodyParams();
        bodyParams.bodyType = boyType;
        bodyParams.x = x;
        bodyParams.y = y;
        bodyParams.radius = radius;
        BaseBody baseBody = new CircleBody(bodyParams);
        Body circleBody = baseBody.createBody(world);
        return circleBody;
    }

    /**更新相机**/
    public void cameraUpdate() {
        Vector3 position = camera.position;
        position.x = 0;
        position.y = 0;
        camera.position.set(position);
        camera.update();
        mBallsManage.referenceSpriteBatch.setProjectionMatrix(camera.combined);
        for (SpriteBatch batch : mBallsManage.ballSprites) {
            batch.setProjectionMatrix(camera.combined);
        }
    }

    /**
     * 更新力场
     * @param round
     * @param isGravity true[引力] false[分离力]
     */
    public void gravityUpdate(Body round, boolean isGravity) {
        BallUserData userData = (BallUserData) round.getUserData();
        if(userData != null && userData.isNeedGravity) {
            float a = -(round.getPosition().x);
            float b = -(round.getPosition().y);
            float c = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            float d = c / 100;
            Vector2 v2 = new Vector2(round.getPosition().x, round.getPosition().y);
            Vector2 v1 = new Vector2(a / d, b / d);
            if(isGravity) {
                round.applyForce(v1, v2, false);
            } else {
                round.applyForce(v2, v1, false);
            }
        }
    }

    /**立即运动到指定位置**/
    public void motionUpdate(float a, float b, int speedFactor) {
        for (Body body : mBallsManage.bodies) {
            a = a - (body.getPosition().x * 2);
            b = b - (body.getPosition().y * 2);
            float c = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            float d = c / speedFactor;
            Vector2 v1 = new Vector2(a / d, b / d);
            body.setLinearVelocity(v1);
        }
    }

    /**向中心靠拢**/
    private void moveToCenter() {
        for (int i = 0; i < mBallsManage.bodies.size(); i ++) {
            Body body = mBallsManage.bodies.get(i);
            float c = (float) Math.sqrt(Math.pow(body.getWorldCenter().x, 2) + Math.pow(body.getWorldCenter().x, 2));
            float d = c / 100;
            Vector2 v1 = new Vector2(body.getWorldCenter().x / d, body.getWorldCenter().y / d);
            body.applyForceToCenter(v1, false);
        }
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        camera.unproject(point.set(x, y, 0));
        world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.testPoint(point.x / PPM, point.y / PPM)) {
                    BallUserData hitUserData = (BallUserData) fixture.getBody().getUserData();
                    if (hitUserData != null && hitUserData.position != -1) {
                        joinToReference(tmpBallUserData.color);
                    }
                }
                return false;
            }
        }, point.x / PPM, point.y / PPM, point.x / PPM, point.y / PPM);
        bodyThatWasHit = null;
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {}

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, final int pointer, int button) {
        if(pointer == 0) {
            camera.unproject(point.set(screenX, screenY, 0));
            world.QueryAABB(new QueryCallback() {
                @Override
                public boolean reportFixture(Fixture fixture) {
                    if (fixture.testPoint(point.x / PPM, point.y / PPM)) {
                        bodyThatWasHit = fixture.getBody();
                        BallUserData hitUserData = (BallUserData) bodyThatWasHit.getUserData();
                        if (hitUserData == null || hitUserData.position == -1) {
                            touchPosition = -1;
                        } else {
                            for (int i = 0; i < mBallsManage.bodies.size(); i++) {
                                Body body = mBallsManage.bodies.get(i);
                                BallUserData userData = (BallUserData) body.getUserData();
                                if (userData != null && userData.position == hitUserData.position) {
                                    touchPosition = i;
                                    userData.isNeedGravity = true;
                                    userData.isOnDrag = true;
                                    reversVec = new Vector2(bodyThatWasHit.getPosition().x, bodyThatWasHit.getPosition().y);
                                    break;
                                }
                            }
                        }
                    }
                    return false;
                }
            }, point.x / PPM, point.y / PPM, point.x / PPM, point.y / PPM);
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(pointer == 0) {
            point = new Vector3(screenX, screenY, 0);
            camera.unproject(point);
            if (bodyThatWasHit != null) {
                tmpBallUserData = (BallUserData) bodyThatWasHit.getUserData();
                //处理小球在参照物内
                if (inReference) {
                    inReference = false;
                    joinToReference(tmpBallUserData.color);
                }
                bodyThatWasHit = null;
            }
            for (Body body : mBallsManage.bodies) {
                BallUserData userData = (BallUserData) body.getUserData();
                if (userData != null) {
                    userData.isNeedGravity = true;
                    userData.isOnDrag = false;
                }
            }
        }
        return false;
    }

    /**融合参照物**/
    private void joinToReference(int color) {
//        float changeRadius = referenceRadius + 2;
//        mBallsManage.bodies.get(0).getFixtureList().get(0).getShape().setRadius(changeRadius); //改变半径
        int tmpColor = color;
        mBallsManage.removeBall(touchPosition);
        BallSprite referenceData = mBallsManage.ballSprites.get(0);
        referenceData.color = tmpColor; //改变内容
//        for (int i = 1; i < mBallsManage.bodies.size(); i++) {
//            Body body = mBallsManage.bodies.get(i);
//            DistanceJointDef jointDefDis = new DistanceJointDef();
//            jointDefDis.bodyA = mBallsManage.bodies.get(0);
//            jointDefDis.bodyB = body;
//            jointDefDis.type = DistanceJointDef.JointType.DistanceJoint;
//            float pointDistance = 6;
//            jointDefDis.length = pointDistance;
//            world.createJoint( jointDefDis );
//        }
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(pointer == 0) {
            if (bodyThatWasHit == null || touchPosition == -1) return false;
            camera.unproject(point.set(screenX, screenY, 0));
            BallUserData hitUserData = (BallUserData) bodyThatWasHit.getUserData();
            if (hitUserData != null && hitUserData.isOnDrag) {
                bodyThatWasHit.setLinearVelocity(reversVec);
            }
            //参照物包含物体算法(两点间的距离+拖拽物的半径 <= 参照物半径)
            float x1 = width / 2;
            float x2 = screenX;
            float y1 = height / 2;
            float y2 = screenY;
            float pointDistance = (float) Math.sqrt(Math.pow(Math.abs(x1 - x2), 2) + Math.pow(Math.abs(y1 - y2), 2)) / PPM;
            if (pointDistance + hitUserData.radius <= mBallsManage.bodies.get(0).getFixtureList().get(0).getShape().getRadius()) {
                inReference = true;
            } else {
                inReference = false;
            }
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    @Override
    public void onBallDetach(Body body) {
        if(world != null) world.destroyBody(body);
    }

    @Override
    public Body onBallAttach(int pos) {
        float radius = mBallsManage.ballSprites.get(pos).size / 2 / PPM;
        float x = (float) (Math.random() * (width / PPM - mBallsManage.ballSprites.get(pos).size) + mBallsManage.ballSprites.get(pos).size);
        float y = (float) (Math.random() * (height / PPM - mBallsManage.ballSprites.get(pos).size) + mBallsManage.ballSprites.get(pos).size);
        Body body = createBody(BodyDef.BodyType.DynamicBody, x / PPM, y / PPM, radius);
        body.setUserData(new BallUserData(pos, mBallsManage.ballSprites.get(pos).color, radius,false, true));
        return body;
    }
}
