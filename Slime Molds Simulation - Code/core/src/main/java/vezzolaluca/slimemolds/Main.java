package vezzolaluca.slimemolds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import static vezzolaluca.slimemolds.Constants.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera cam;

    private WorldGrid worldGrid;

    private boolean isPaused = true; // Variabile per gestire lo stato di pausa
    
    private long startTimeNs;
    private long simulationTimeNs;
    
    @Override
    public void create() {
        cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        
        worldGrid = new WorldGrid();
        batch = new SpriteBatch();
        
        startTimeNs = TimeUtils.nanoTime(); //The starting time (in nanoseconds)
        simulationTimeNs = 1000000000; //One second (in nanoseconds)
    }

    @Override
    public void render() {
        // if time passed since the time you set startTimeNs at is more than 10 seconds
        if (TimeUtils.timeSinceNanos(startTimeNs) > simulationTimeNs*50) {
            worldGrid.randomizeTrailsColor();
            worldGrid.randomizeProperties();
            System.out.println("Changing color....");
            startTimeNs = TimeUtils.nanoTime();
        }

        // Controlla se la barra spaziatrice viene premuta
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            isPaused = !isPaused; // Inverte lo stato di pausa
        }

        if (!isPaused) {
            worldGrid.updateLogic();
            worldGrid.blurTrails();
        }

        worldGrid.manageInputs();

        batch.setProjectionMatrix(cam.combined);

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        batch.begin();
        worldGrid.draw(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        worldGrid.dispose();
    }
}
