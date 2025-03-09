package vezzolaluca.slimemolds;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import static vezzolaluca.slimemolds.Constants.*;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private OrthographicCamera cam;

    private WorldGrid worldGrid;

    private boolean isPaused = false; // Variabile per gestire lo stato di pausa
    
    @Override
    public void create() {
        cam = new OrthographicCamera(WORLD_WIDTH, WORLD_HEIGHT);
        cam.position.set(cam.viewportWidth / 2f, cam.viewportHeight / 2f, 0);
        cam.update();
        
        worldGrid = new WorldGrid();
        batch = new SpriteBatch();
    }

    @Override
    public void render() {
        // Controlla se la barra spaziatrice viene premuta
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            isPaused = !isPaused; // Inverte lo stato di pausa
        }
        
        if (!isPaused) {
            worldGrid.updateLogic();
        }
        
        worldGrid.manageInputs();
        worldGrid.blurTrails();
        
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
