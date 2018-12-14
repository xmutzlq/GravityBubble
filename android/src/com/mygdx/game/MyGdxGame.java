package com.mygdx.game;

import android.app.Activity;
import android.util.Log;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter implements GestureDetector.GestureListener, InputProcessor {
    private static final float PPM = 30f;
    private static final int ReferenceBallScale = 12;

    Activity mActivity;

    ArrayList<BallSprite> ballSprites;
    ArrayList<Body> bodyList;
    ArrayList<SpriteBatch> batchCircleList;

    Vector3 point;
    Sprite spriteCircle;
    Body bodyThatWasHit;

    private int radiusUsed;
    private int touchPosition;

    private Box2DDebugRenderer m_debugRenderer;
    private OrthographicCamera camera;
    private World world;

    MyGdxGame(Activity activity, ArrayList<BallSprite> ballSprites) {
        this.mActivity = activity;
        this.ballSprites = ballSprites;
    }


    @Override
    public void create() {
        batchCircleList = new ArrayList<>();
        bodyList = new ArrayList<>();

        //手势
        InputMultiplexer im = new InputMultiplexer();
        im.addProcessor(this);
        GestureDetector gd = new GestureDetector(this);
        im.addProcessor(gd);
        Gdx.input.setInputProcessor(im);

        float width = Gdx.graphics.getWidth() / 2;
        float height = Gdx.graphics.getHeight() / 2;

        radiusUsed = ScreenUtil.isScreenPortrait(mActivity) ? Gdx.graphics.getWidth() : Gdx.graphics.getHeight();

        //设置视角举行的大小
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width, height);

        //太空失重
        world = new World(new Vector2(0, 0), false);
        m_debugRenderer = new Box2DDebugRenderer();

        //相机位置
        point = new Vector3(width, height, 0);
        camera.unproject(point);

        //创建墙
        createWall(-point.x / (PPM), point.y / (PPM), point.x / (PPM), point.y / (PPM));     //top wall
        createWall(-point.x / (PPM), -point.y / (PPM), point.x / (PPM), -point.y / (PPM));   //bottom wall
        createWall(-point.x / (PPM), point.y / (PPM), -point.x / (PPM), -point.y / (PPM));   //left wall
        createWall(point.x / (PPM), point.y / (PPM), point.x / (PPM), -point.y / (PPM));     //right wall

        //创建物体
        Texture imgCircle = new Texture("circle.png");
        spriteCircle = new Sprite(imgCircle);

        //创建参照物
        createReferenceCircle();

        //创建圆体
        for (int i = 0; i < ballSprites.size(); i++) {
            float x = (float) (Math.random() * (width / PPM - ballSprites.get(i).size) + ballSprites.get(i).size);
            float y = (float) (Math.random() * (height / PPM - ballSprites.get(i).size) + ballSprites.get(i).size);
            createCircle(x, y, i);
        }

        for (int i = 0; i < bodyList.size(); i++) {
            batchCircleList.add(new SpriteBatch());
        }

        moveToCenter();
    }

    @Override
    public void render() {
        update();
        Gdx.gl.glClearColor(255f, 255f, 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        m_debugRenderer.render(world, camera.combined);

        //参照物
        int i = 0;
        SpriteBatch spriteBatchReference = batchCircleList.get(i);
        spriteBatchReference.begin();
        spriteBatchReference.draw(spriteCircle,
                bodyList.get(i).getWorldCenter().x - radiusUsed / ReferenceBallScale,
                bodyList.get(i).getWorldCenter().y - radiusUsed / ReferenceBallScale,
                radiusUsed / 6, radiusUsed / 6);
        spriteBatchReference.setColor(new Color(ballSprites.get(i).color));
        spriteBatchReference.end();

        //吸附球
        int position = 0;
        for (i = 1; i < bodyList.size(); i++) {
            Body body = bodyList.get(i);
            batchUpdate(batchCircleList.get(position), body, ballSprites.get(position));
            position++;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / 2, height / 2);
    }

    @Override
    public void dispose() {
        world.dispose();

        for (SpriteBatch spritebatch : batchCircleList) {
            spritebatch.dispose();
        }
        m_debugRenderer.dispose();
    }

    public void update() {
        world.step(1 / 60f, 6, 2);

        for (Body body : bodyList) {
            gravityUpdate(body);
        }
        cameraUpdate();
    }

    /**
     * 创建墙体
     * @param w1
     * @param h1
     * @param w2
     * @param h2
     * @return
     */
    public Body createWall(float w1, float h1, float w2, float h2) {

        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(0, 0);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(w1, h1, w2, h2);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = edgeShape;

        Body bodyEdgeScreen = world.createBody(bodyDef2);
        bodyEdgeScreen.createFixture(fixtureDef2);
        edgeShape.dispose();
        return bodyEdgeScreen;
    }

    /**
     * 创建参照物
     */
    private void createReferenceCircle() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);

        CircleShape circleSmall = new CircleShape();
        circleSmall.setRadius(radiusUsed / ReferenceBallScale / PPM); //半径(屏幕1/4)

        FixtureDef fixtureDefSmall = new FixtureDef();
        fixtureDefSmall.shape = circleSmall;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDefSmall);

        bodyList.add(body);
    }

    /**
     * 创建球体
     * @param x
     * @param y
     * @param pos
     * @return
     */
    private Body createCircle(float x, float y, int pos) {
        return createCircle(x, y, pos, false);
    }

    public Body createCircle(float x, float y, int pos, boolean isStaticBody) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = isStaticBody ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x / PPM, y / PPM);
        bodyDef.fixedRotation = true;

        CircleShape circleSmall = new CircleShape();
        circleSmall.setRadius(ballSprites.get(pos).size / 2 / PPM);

        FixtureDef fixtureDefSmall = new FixtureDef();
        fixtureDefSmall.shape = circleSmall;
        fixtureDefSmall.density = 1f;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDefSmall);
        body.setUserData(new BallUserData(pos, ballSprites.get(pos).color, false, true));

        bodyList.add(body);
        return body;
    }

    public void cameraUpdate() {

        Vector3 position = camera.position;
        position.x = 0;
        position.y = 0;
        camera.position.set(position);
        camera.update();

        for (SpriteBatch batch : batchCircleList) {
            batch.setProjectionMatrix(camera.combined);
        }

    }

    /**加速度到指定位置**/
    public void gravityUpdate(Body round) {
        BallUserData userData = (BallUserData) round.getUserData();
        if(userData != null && userData.isNeedGravity) {
            float a = -(round.getPosition().x);
            float b = -(round.getPosition().y);
            float c = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            float d = c / 100;
            Vector2 v2 = new Vector2(round.getPosition().x, round.getPosition().y);
            Vector2 v1 = new Vector2(a / d, b / d);
            round.applyForce(v1, v2, true);
        }
    }

    /**立即运动到指定位置**/
    public void motionUpdate(float a, float b, int speedFactor) {
        for (Body body : bodyList) {
            a = a - (body.getPosition().x * 2);
            b = b - (body.getPosition().y * 2);
            float c = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            float d = c / speedFactor;
            Vector2 v1 = new Vector2(a / d, b / d);
            body.setLinearVelocity(v1);
        }
    }

    private void moveToCenter() {
        for (int i = 0; i < bodyList.size(); i ++) {
            Body body = bodyList.get(i);
            float c = (float) Math.sqrt(Math.pow(body.getWorldCenter().x, 2) + Math.pow(body.getWorldCenter().x, 2));
            float d = c / 100;
            Vector2 v1 = new Vector2(body.getWorldCenter().x / d, body.getWorldCenter().y / d);
            body.applyForceToCenter(v1, false);
        }
    }

    public void batchUpdate(SpriteBatch batchCircle, Body round, BallSprite ballSprite) {
        BallUserData userData = (BallUserData) round.getUserData();
        float ballSize = ballSprite.size;
        batchCircle.begin();
        if(userData.isOnDrag) {
            batchCircle.draw(spriteCircle, point.x - ballSize / 2, point.y - ballSize / 2, ballSize, ballSize);
        } else {
            batchCircle.draw(spriteCircle, round.getPosition().x * PPM - ballSize / 2, round.getPosition().y * PPM - ballSize / 2, ballSize, ballSize);
        }
        batchCircle.setColor(new Color(ballSprite.color));
        batchCircle.end();
    }


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        System.out.println("event td " + x + y);
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Log.e("zlq", "tap");
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        System.out.println("event lp screen" + x + " " + y);
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        System.out.println("event fl screen" + velocityX + " " + velocityY);
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
    public void pinchStop() {

    }

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
        point = new Vector3(screenX, screenY, 0);
        camera.unproject(point);
        world.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                if (fixture.testPoint(point.x / PPM, point.y / PPM)) {
                    bodyThatWasHit = fixture.getBody();
                    int position = 0;
                    for (Body body : bodyList) {
                        BallUserData userData = (BallUserData) body.getUserData();
                        BallUserData hitUserData = (BallUserData) bodyThatWasHit.getUserData();
                        if(hitUserData == null) {
                            touchPosition = -1;
                            break;
                        }
                        if(userData != null && hitUserData != null &&
                                userData.position == hitUserData.position) {
                            touchPosition = position;
                            userData.isNeedGravity = false;
                            userData.isOnDrag = true;
                            break;
                        }
                        position ++;
                    }
                }
                return false;
            }
        }, point.x / PPM, point.y / PPM, point.x / PPM, point.y / PPM);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        point = new Vector3(screenX, screenY, 0);
        camera.unproject(point);
        if(bodyThatWasHit != null) {
            point.x = point.x - (bodyThatWasHit.getPosition().x * 2);
            point.y = point.y - (bodyThatWasHit.getPosition().y * 2);
            float c = (float) Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
            float d = c / 10;
            bodyList.get(touchPosition).getPosition().x = point.x / d;
            bodyList.get(touchPosition).getPosition().y = point.y / d;
            bodyThatWasHit = null;
        }
        for (Body body : bodyList) {
            BallUserData userData = (BallUserData) body.getUserData();
            if(userData != null) {
                userData.isNeedGravity = true;
                userData.isOnDrag = false;
            }
        }
        //参照物包含物体
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(bodyThatWasHit == null || pointer == 1 || touchPosition == -1) return false;
        point = new Vector3(screenX, screenY, 0);
        camera.unproject(point);
        point.x = point.x - (bodyThatWasHit.getPosition().x * 2);
        point.y = point.y - (bodyThatWasHit.getPosition().y * 2);
        float c = (float) Math.sqrt(Math.pow(point.x, 2) + Math.pow(point.y, 2));
        float d = c / 10;
        Vector2 v2 = new Vector2(bodyThatWasHit.getPosition().x, bodyThatWasHit.getPosition().y);
        Vector2 v1 = new Vector2(point.x / d, point.y / d);
        bodyThatWasHit.setLinearVelocity(v1);
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
}
