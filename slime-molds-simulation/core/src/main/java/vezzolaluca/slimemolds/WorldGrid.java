/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vezzolaluca.slimemolds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;
import static vezzolaluca.slimemolds.Constants.*;

/**
 *
 * @author lucav
 */
public class WorldGrid {
    private Random rand;
    private Probe[] probes;
    
    private float trailMap[][];
    private Pixmap worldPixmap;
    public Texture worldTexture;
    
    
    //System variables
    public static float vanishing_factor = 0.99f;
    private float alpha = 0.2f; // Coefficiente di miscelazione, più vicino a 0 rallenta il blurring
    
    private float r = Probe.randomFloatFrom0To1();
    private float g = Probe.randomFloatFrom0To1();
    private float b = Probe.randomFloatFrom0To1();
    
    private float newR = Probe.randomFloatFrom0To1();
    private float newG = Probe.randomFloatFrom0To1();
    private float newB = Probe.randomFloatFrom0To1();
    
    private float colorChangingFactor = 0.005f;
    
    public WorldGrid(){
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

            probes[i] = new Probe(new Vector2(spawnX, spawnY), direction);
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
                worldPixmap.setColor(Color.rgba8888(r, g, b, a));
                worldPixmap.drawPixel(i, j);
            }
        }
        
        int probeX;
        int probeY;
        //Drawing the probes
        for(Probe probe : probes){
            //on the worldPixmap
            worldPixmap.setColor(Color.rgba8888(r, g, b, 1f));
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
    public void manageInputs(){
        //velocity
        if(Gdx.input.isKeyPressed(Input.Keys.V)){
            //the maximum rendering-secure velocity is 1 (the difference is inversely proportional to the velocity)
            Probe.velocity = (float)Math.min(100, Probe.velocity+0.01f*Probe.velocity);
        } else if(Gdx.input.isKeyPressed(Input.Keys.C)){
            //Min velocity is 0
            Probe.velocity = (float)Math.max(0, Probe.velocity-0.01f*Probe.velocity);
        }
        
        //sensor_angle_space
        if(Gdx.input.isKeyPressed(Input.Keys.P)){
            //Max is 2PI radians (bigger vakues give the same results)
            Probe.sensor_angle_space = (float)Math.min(Math.PI*2, Probe.sensor_angle_space+0.02f);
        } else if(Gdx.input.isKeyPressed(Input.Keys.O)){
            Probe.sensor_angle_space = (float)Math.max(0, Probe.sensor_angle_space-0.02f);
        }
        
        //turning_speed
        if(Gdx.input.isKeyPressed(Input.Keys.L)){
            //Max turning speed is 100
            Probe.turning_speed = (float)Math.min(100, Probe.turning_speed+0.02f);
        } else if(Gdx.input.isKeyPressed(Input.Keys.K)){
            //Min turning speed is 0
            Probe.turning_speed = (float)Math.max(0, Probe.turning_speed-0.02f);
        }
        
        //sensor_offset_distance
        if(Gdx.input.isKeyPressed(Input.Keys.W)){
            //Max offset is 500 pixels
            Probe.sensor_offset_distance = (int)Math.min(500, Probe.sensor_offset_distance+1);
        } else if(Gdx.input.isKeyPressed(Input.Keys.Q)){
            //Min offset is 0
            Probe.sensor_offset_distance = (int)Math.max(0, Probe.sensor_offset_distance-1);
        }
        
        //sensor_radius
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)){
            //Max radius is 500 pixels
            Probe.sensor_radius = (int)Math.min(500, Probe.sensor_radius+1);
        } else if(Gdx.input.isKeyJustPressed(Input.Keys.A)){
            //Min radius is 0
            Probe.sensor_radius = (int)Math.max(0, Probe.sensor_radius-1);
        }
        
        //vanishing_factor (the difference is inversely proportional to the factor)
        if(Gdx.input.isKeyPressed(Input.Keys.Y)){
            //Max factor is 1 (never decreases)
            vanishing_factor = (float)Math.min(1, vanishing_factor+0.001f/vanishing_factor);
        } else if(Gdx.input.isKeyPressed(Input.Keys.T)){
            //Min factor is 0 (instantly vanished)
            vanishing_factor = (float)Math.max(0, vanishing_factor-0.001f/vanishing_factor);
        }
        
        //alpha
        if(Gdx.input.isKeyPressed(Input.Keys.H)){
            //Max factor is 1 (maximum blur)
            alpha = (float)Math.min(1, alpha+0.01f);
        } else if(Gdx.input.isKeyPressed(Input.Keys.G)){
            //Min factor is 0 (no blur)
            alpha = (float)Math.max(0, alpha-0.01f);
        }
        
        //Enable/disable the looping borders
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_0)){
            Probe.loopingBorders = !Probe.loopingBorders;
        }
        
        //display Probe's static variables written before
        if(Gdx.input.isKeyJustPressed(Input.Keys.B)){
            System.out.println(Probe.staticsToString() + "\nvanishing_factor: " + vanishing_factor + "\nalpha: " + alpha + "\n\n");
        }
    }
    
    public void randomizeTrailsColor(){
        //The possibility to randomize the values by adding a number between -0.5 and +0.5
        do{
            newR = (float)Math.min(1, Math.max(0, newR + (Probe.randomFloatFrom0To1() - 0.5)*2));
            newG = (float)Math.min(1, Math.max(0, newG + (Probe.randomFloatFrom0To1() - 0.5)*2));
            newB = (float)Math.min(1, Math.max(0, newB + (Probe.randomFloatFrom0To1() - 0.5)*2));
        }while((newR+newG+newB)<2.5f);
    }

    
    public void randomizeProperties() {
        this.alpha = rand.nextFloat(0f, 1f);
        vanishing_factor = rand.nextFloat(0.80f, 1f);
        Probe.randomizeProperties();
    }
    
    public String getCurrentSystemVariables() {
        String s = "";
        
        s += Probe.getCurrentSystemVariables();
        
        s += "vanishing_factor: " + vanishing_factor + "\n";
        s += "alpha: " + alpha + "\n";
        
        s += "probe RGB: " + (int)(r*255) + ";" + (int)(g*255) + ";" + (int)(b*255) + "\n";
        
        return s;
    }
    
    public void dispose(){
        worldPixmap.dispose();
        worldTexture.dispose();
    }
}
