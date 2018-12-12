package com.mygdx.game;

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
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter implements GestureDetector.GestureListener, InputProcessor {
    float PPM = 30f;

    ArrayList<BallSprite> ballSprites;
    ArrayList<Body> bodyList;
    ArrayList<SpriteBatch> batchCircleList;

    Vector3 point;
    Sprite spriteCircle;

    private Box2DDebugRenderer m_debugRenderer;
    private OrthographicCamera camera;
    private World world;

    MyGdxGame(ArrayList<BallSprite> ballSprites) {
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
            float x = (float) (Math.random() * (50 - 15) + 15);
            float y = (float) (Math.random() * (50 - 15) + 15);
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
                bodyList.get(i).getWorldCenter().x * PPM - ballSprites.get(i).size / 2,
                bodyList.get(i).getWorldCenter().y * PPM - ballSprites.get(i).size / 2,
                ballSprites.get(i).size, ballSprites.get(i).size);
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
        circleSmall.setRadius(4f); //半径

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
        circleSmall.setRadius(1.4f);

        FixtureDef fixtureDefSmall = new FixtureDef();
        fixtureDefSmall.shape = circleSmall;
        fixtureDefSmall.density = 1f;

        Body body = world.createBody(bodyDef);
        body.createFixture(fixtureDefSmall);
        body.setUserData(new String[]{"unselected", String.valueOf(pos)});

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
        float a = -(round.getPosition().x);
        float b = -(round.getPosition().y);
        float c = (float) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        float d = c / 50;

        Vector2 v2 = new Vector2(bodyList.get(0).getPosition().x, bodyList.get(0).getPosition().y);
        Vector2 v1 = new Vector2(a / d, b / d);
        round.applyForceToCenter(v1, false);
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
            body.applyForceToCenter(body.getWorldCenter(), false);
        }
    }

    public void batchUpdate(SpriteBatch batchCircle, Body round, BallSprite ballSprite) {
        float ballSize = ballSprite.size;
        batchCircle.begin();
        batchCircle.draw(spriteCircle, round.getPosition().x * PPM - ballSize / 2, round.getPosition().y * PPM - ballSize / 2, ballSize, ballSize);
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
        System.out.println("event tp screen" + x + " " + y);
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
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        point.set(screenX, screenY, 0);
        camera.unproject(point);
        motionUpdate(point.x, point.y, 10);
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
