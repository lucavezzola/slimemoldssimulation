/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vezzolaluca.slimemolds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.util.Random;
import static vezzolaluca.slimemolds.Constants.*;

/**
 *
 * @author lucav
 */
public class WorldGrid implements InputProcessor{
    private Random rand;
    private Probe[] probes;
    
    private float trailMap[][];
    private Pixmap worldPixmap;
    public Texture worldTexture;
    
    
    
    public static float vanishing_factor = 0.99f;
    private float alpha = 0.1f; // Coefficiente di miscelazione, più vicino a 0 rallenta il blurring
    
    private float r = Probe.randomFloatFrom0To1();
    private float g = Probe.randomFloatFrom0To1();
    private float b = Probe.randomFloatFrom0To1();
    
    private float newR = Probe.randomFloatFrom0To1();
    private float newG = Probe.randomFloatFrom0To1();
    private float newB = Probe.randomFloatFrom0To1();
    
    private float colorChangingFactor = 0.001f;
    
    private OrthographicCamera cam;
    
    private static int mouseDrawnRadius = 10;
    
    public WorldGrid(OrthographicCamera cam){
        Gdx.input.setInputProcessor(this); //Sets this object to be the input processor of the game
        
        rand = new Random();
        
        Vector2 worldCenter = new Vector2(WORLD_WIDTH/2, WORLD_HEIGHT/2);
        
        probes = new Probe[PROBES_NUMBER];
        //Initialize every probe inside a circle with the direction pointing to its center
        int spawnRadius = (int)(Math.min(WORLD_WIDTH, WORLD_HEIGHT)*0.5f/2); //Pixels - the numerator is the scale diameter-to-screenDimension
        for(int i = 0; i < probes.length; i++){
            double angle = Probe.randomFloatFrom0To1() * 2 * Math.PI;
            double distance = Math.sqrt(Probe.randomFloatFrom0To1()) * spawnRadius;

            int spawnX = (int)(worldCenter.x + Math.cos(angle) * distance);
            int spawnY = (int)(worldCenter.y + Math.sin(angle) * distance);

            // Set direction towards the center
            float direction = (float)Math.atan2(worldCenter.y - spawnY, worldCenter.x - spawnX);
            
            //The starting velocity is proportional to the distance of that probe from the world center
            float velocity = (float)Math.abs(spawnX - WORLD_WIDTH/2)/100f +
                    (float)Math.abs(spawnY - WORLD_HEIGHT/2)/100f;

            probes[i] = new Probe(new Vector2(spawnX, spawnY), direction, 0.5f);
            
            this.cam = cam;
        }


        
        
        trailMap = new float[WORLD_WIDTH][WORLD_HEIGHT];
        for(int i=0; i<trailMap.length; i++){
            for(int j=0; j<trailMap[i].length; j++){
                trailMap[i][j] = 0;                
            }
        }
        
        worldPixmap = new Pixmap(WORLD_WIDTH, WORLD_HEIGHT, Format.RGBA8888);
        worldPixmap.setBlending(Pixmap.Blending.None);
        worldTexture = new Texture(worldPixmap);
    }

    public void updateLogic(){        
        //Updating the trails (making them vanish over time)
        for(int i=0; i<trailMap.length; i++){
            for(int j=0; j<trailMap[i].length; j++){
                trailMap[i][j] *= vanishing_factor;
            }
        }
        
        //Updating the positions of the probes based on the trails
        for(Probe probe : probes){
            probe.updatePosition(trailMap);
        }
    }
    
    public void blurTrails(){
        float[][] tempTrailMap = new float[trailMap.length][trailMap[0].length];

        for (int i = 0; i < trailMap.length; i++) {
            for (int j = 0; j < trailMap[i].length; j++) {
                float sum = 0;
                int count = 0;

                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetY = -1; offsetY <= 1; offsetY++) {
                        int posX = i + offsetX;
                        int posY = j + offsetY;

                        if (posX >= 0 && posX < WORLD_WIDTH && posY >= 0 && posY < WORLD_HEIGHT) {
                            sum += trailMap[posX][posY];
                            count++;
                        }
                    }
                }

                float blurredValue = sum / count; // Usa count per normalizzare correttamente
                tempTrailMap[i][j] = alpha * blurredValue + (1 - alpha) * trailMap[i][j]; // Interpolazione
            }
        }

        // Copia i valori dalla matrice temporanea a quella originale
        for (int i = 0; i < trailMap.length; i++) {
            for (int j = 0; j < trailMap[i].length; j++) {
                trailMap[i][j] = tempTrailMap[i][j];
            }
        }
    }


    
    public void draw(SpriteBatch batch){
        float a;
        
        //Drawing the trails
        for(int i=0; i<trailMap.length; i++){
            for(int j=0; j<trailMap[i].length; j++){
                a = trailMap[i][j];                
                worldPixmap.setColor(Color.rgba8888(0.3f, 0.3f, 0.3f, a));
                worldPixmap.drawPixel(i, j);
            }
        }
        
        int probeX;
        int probeY;
        //Drawing the probes
        for(Probe probe : probes){
            float rProbe = (float) (Math.cos(probe.direction));
            float gProbe = Probe.randomFloatFrom0To1();
            float bProbe = (float) (Math.sin(probe.direction));
            
            //on the worldPixmap
            worldPixmap.setColor(Color.rgba8888(probe.velocity, 0, 1-probe.velocity, 1f));
            worldPixmap.drawPixel((int)probe.position.x, (int)probe.position.y);
            
            //and on the trailMap
            probeX = (int)Math.min(WORLD_WIDTH-1, Math.max(0, (int)probe.position.x));
            probeY = (int)Math.min(WORLD_HEIGHT-1, Math.max(0, (int)probe.position.y));
            trailMap[probeX][probeY] = 1f;
        }
        
        //Drawing on the texture
        worldTexture.draw(worldPixmap, 0, 0);
        //Displaying the updated texture
        batch.draw(worldTexture, 0, 0);
        
        //Blending the trail color with the new random color
        r = colorChangingFactor * newR + (1 - colorChangingFactor) * r;
        g = colorChangingFactor * newG + (1 - colorChangingFactor) * g;
        b = colorChangingFactor * newB + (1 - colorChangingFactor) * b;

    }
    
    
    //The values of the static variables of probes can be changed by pressing keys

    
    public void randomizeTrailsColor(){
        //The possibility to randomize the values by adding a number between -0.5 and +0.5
        do{
            newR = (float)Math.min(1, Math.max(0, newR + (Probe.randomFloatFrom0To1() - 0.5)*2));
            newG = (float)Math.min(1, Math.max(0, newG + (Probe.randomFloatFrom0To1() - 0.5)*2));
            newB = (float)Math.min(1, Math.max(0, newB + (Probe.randomFloatFrom0To1() - 0.5)*2));
        }while((newR+newG+newB)<2.5f);
    }
    
    private void drawTrail(Vector3 pos, int radius){
        Vector3 tempPos = new Vector3();
        for(int x=(-radius); x<=radius; x++){
            for(int y=(-radius); y<=radius; y++){
                tempPos.x = (int)pos.x+x;
                tempPos.y = (int)pos.y+y;
                
                if(tempPos.x>0 && tempPos.x<WORLD_WIDTH && tempPos.y>0 && tempPos.y<WORLD_HEIGHT){
                    this.trailMap[(int)tempPos.x][(int)tempPos.y] = 1f;
                }                
            }
        }
    }

    
    public void dispose(){
        worldPixmap.dispose();
        worldTexture.dispose();
    }

    @Override
    public boolean keyDown(int i) {        
        switch (i) {
            // reactivity_factor
            case Input.Keys.V:
                Probe.reactivityFactor = (float) Math.min(100, Probe.reactivityFactor + 0.01f * Probe.reactivityFactor);
                break;
            case Input.Keys.C:
                Probe.reactivityFactor = (float) Math.max(0, Probe.reactivityFactor - 0.01f * Probe.reactivityFactor);
                break;

            // sensor_angle_space
            case Input.Keys.P:
                Probe.sensor_angle_space = (float) Math.min(Math.PI * 2, Probe.sensor_angle_space + 0.02f);
                break;
            case Input.Keys.O:
                Probe.sensor_angle_space = (float) Math.max(0, Probe.sensor_angle_space - 0.02f);
                break;

            // turning_speed
            case Input.Keys.L:
                Probe.turning_speed = (float) Math.min(100, Probe.turning_speed + 0.02f);
                break;
            case Input.Keys.K:
                Probe.turning_speed = (float) Math.max(0, Probe.turning_speed - 0.02f);
                break;

            // sensor_offset_distance
            case Input.Keys.W:
                Probe.sensor_offset_distance = (int) Math.min(500, Probe.sensor_offset_distance + 1);
                break;
            case Input.Keys.Q:
                Probe.sensor_offset_distance = (int) Math.max(0, Probe.sensor_offset_distance - 1);
                break;

            // vanishing_factor
            case Input.Keys.Y:
                vanishing_factor = (float) Math.min(1, vanishing_factor + 0.001f / vanishing_factor);
                break;
            case Input.Keys.T:
                vanishing_factor = (float) Math.max(0, vanishing_factor - 0.001f / vanishing_factor);
                break;

            // alpha
            case Input.Keys.H:
                alpha = (float) Math.min(1, alpha + 0.01f);
                break;
            case Input.Keys.G:
                alpha = (float) Math.max(0, alpha - 0.01f);
                break;

            default:
                //No valid keyword pressed
                return false;
        }
        
        return true;
    }

    @Override
    public boolean keyUp(int i) {
        return false;
    }

    @Override
    public boolean keyTyped(char c) {
        switch (c) {
            // sensor_radius
            case 's':
            case 'S':
                Probe.sensor_radius = (int) Math.min(500, Probe.sensor_radius + 1);
                break;
            case 'a':
            case 'A':
                Probe.sensor_radius = (int) Math.max(0, Probe.sensor_radius - 1);
                break;

            // Enable/disable looping borders
            case '0':
                Probe.loopingBorders = !Probe.loopingBorders;
                break;

            // Display Probe's static variables
            case 'b':
            case 'B':
                System.out.println(Probe.staticsToString() +
                        "\nvanishing_factor: " + vanishing_factor +
                        "\nalpha: " + alpha +
                        "\n\n");
                break;

            default:
                //No valid keyword typed
                return false;
        }
        
        return true;
    }

    // Variabile d'istanza
    private boolean leftButtonPressed = false;

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            leftButtonPressed = true;
            Vector3 inputPos = new Vector3(x, y, 0);
            cam.unproject(inputPos);
            //Inverts the y axis
            inputPos.y = WORLD_HEIGHT - inputPos.y;
            //Draw the pixel
            drawTrail(inputPos, mouseDrawnRadius);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        if (leftButtonPressed) {
            Vector3 inputPos = new Vector3(x, y, 0);
            cam.unproject(inputPos);
            //Inverts the y axis
            inputPos.y = WORLD_HEIGHT - inputPos.y;
            // Colora il pixel
            drawTrail(inputPos, mouseDrawnRadius);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        if (button == Input.Buttons.LEFT) {
            leftButtonPressed = false;
            return true;
        }
        return false;
    }


    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        leftButtonPressed = false;
        return true;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean scrolled(float f, float f1) {
        return false;
    }
}
