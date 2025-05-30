package vezzolaluca.slimemolds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import static vezzolaluca.slimemolds.Constants.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera cam;
    
    private BitmapFont font; // Default font

    private WorldGrid worldGrid;

    private boolean isPaused = false; // Variabile per gestire lo stato di pausa
    
    private long startTimeNs;
    private long simulationTimeNs;
    
    @Override
    public void create() {
        cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        
        font = new BitmapFont();
        
        worldGrid = new WorldGrid();
        batch = new SpriteBatch();
        
        startTimeNs = TimeUtils.nanoTime(); //The starting time (in nanoseconds)
        simulationTimeNs = 1000000000l; //One second (in nanoseconds)
    }

    @Override
    public void render() {
        // if time passed since the time you set startTimeNs at is more than 10 seconds
        if (TimeUtils.timeSinceNanos(startTimeNs) > simulationTimeNs*Probe.randomFloatFrom0To1()*30) {
            worldGrid.randomizeTrailsColor();
            startTimeNs = TimeUtils.nanoTime();
        }

        // Controlla se la barra spaziatrice viene premuta
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            isPaused = !isPaused; // Inverte lo stato di pausa
        }
        
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            worldGrid.randomizeProperties(); //Randomizes all of the system (probes and worldGrid's) variables
        }
        
        if(Gdx.input.isKeyJustPressed(Input.Keys.DEL)){
            worldGrid = new WorldGrid(); //Restarts the system
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
        font.draw(batch, getCurrentSystemVariables(), 10, cam.viewportHeight - 10);
        batch.end();
    }

    public String getCurrentSystemVariables() {
        String s = "";
        
        s += worldGrid.getCurrentSystemVariables();
        
        return s;
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        worldGrid.dispose();
    }
}
