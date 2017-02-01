package com.isom.infrastructure.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.isom.infrastructure.Scene.HUD;
import com.isom.infrastructure.Util.*;
import com.isom.infrastructure.WikiJump;
import sprite.*;


public class PlayScreen implements Screen{

    public WikiJump game;
    private World world;
    private TiledMap map;

    // cam & viewport
    private OrthographicCamera cam = new OrthographicCamera();
    private Viewport viewport = new FitViewport(game.V_WIDTH / game.PPM, game.V_HEIGHT / game.PPM,cam);
    private OrthogonalTiledMapRenderer mapRenderer;
    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();


    // things requiring render
    public HUD hud;
    public Wiki wiki;
    public Array<Bullet> bullets;
    public Array<Bastion> bastions;






    public World getWorld() {return world;}





    public PlayScreen(WikiJump game) {
        this.game = game;
        hud = new HUD(game.batch, this);

        // 1. set camera
        cam.position.set(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2, 0);

        // 2. create world and pass necessary objects
        world = WorldCreator.createWorld();
        map = WorldCreator.getMap();
        mapRenderer = WorldCreator.getMapRenderer();

        // 3. create main character
        wiki = new Wiki(world, this);
        bullets = new Array<Bullet>();
        bastions = new Array<Bastion>();
        WorldCreator.createBastions(bastions, this);


        // 4. set a contact listener for our world
        world.setContactListener(new MyContactListener(this, wiki));
        world.setContactFilter(new MyContactFilter(this, wiki));

//        System.out.println(new Exception().getStackTrace()[1].getClassName());



    }






    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        // 1. set screen as black
        Gdx.gl.glClearColor(0, 0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2. render the map
        mapRenderer.setView(cam);
        mapRenderer.render();

        // 3. render the HUD
        game.batch.setProjectionMatrix(hud.hudCam.combined);
        hud.stage.draw();

        // 4. render the main character
        game.batch.setProjectionMatrix(cam.combined);
        game.batch.begin();
        wiki.draw(game.batch);
        for (Bullet bullet : bullets) {bullet.draw(game.batch);}
        for (Bastion bastion : bastions) {bastion.draw(game.batch);}
        game.batch.end();

        // 5. open debug renderer
        //debugRenderer.render(world, cam.combined);

    }

    private void update(float delta) {

        world.step(1/80f, 6, 2);

        InputHandler.handleInput(wiki);
//        System.out.println("handle input");

        // let camera follow wiki
        if (wiki.body.getPosition().x > 10.25) cam.position.x = wiki.body.getPosition().x;
//        System.out.println("cam follow");

        hud.update(delta);
        wiki.update(delta);
        for (Bullet bullet : bullets) {bullet.update(delta);}
        for (Bastion bastion : bastions) {bastion.update(delta);}
        cam.update();
        mapRenderer.setView(cam);
//        System.out.println("updates");

        // set anything as dead if it fell off a cliff
        if (wiki.body.getPosition().y < 0) wiki.die();
//        System.out.println("check dead");


    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        bullets.clear();
        bastions.clear();

        map.dispose();
        world.dispose();
        mapRenderer.dispose();
        debugRenderer.dispose();
        hud.dispose();



    }
}
