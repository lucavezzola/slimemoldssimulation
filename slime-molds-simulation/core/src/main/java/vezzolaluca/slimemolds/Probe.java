/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vezzolaluca.slimemolds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;
import static vezzolaluca.slimemolds.Constants.*;

/**
 *
 * @author lucav
 */

//A probe will navigate the world leaving a trail behind, following other probes' trails
//It will be represented by a single white pixel
public class Probe {
    private static final Random RAND = new Random();
    
    public Vector2 position;
    public float direction;//The angle of the vector of motion in radians (from 0 to 2*PI radians ==> from 0 to 360 degrees)
    
    public static float velocity = RAND.nextFloat(0.5f, 1f);
    public static float sensor_angle_space = RAND.nextFloat(0.1f, (float)Math.PI); //In radians
    public static float turning_speed = RAND.nextFloat(1, 10);
    public static int sensor_offset_distance = RAND.nextInt(3, 300);
    public static int sensor_radius = RAND.nextInt(0, 3);
    public static boolean loopingBorders = true;

    public Probe(Vector2 position, float direction){
        this.position = position;
        this.direction = direction;
    }
    public void updatePosition(float[][] trailMap){
        updateDirection(trailMap);
        
        Vector2 newPosition = new Vector2(position.x+(float)Math.cos(direction)*velocity,
                    position.y+(float)Math.sin(direction)*velocity);
        if(loopingBorders){
            //Updating the position and keeping the probe inside the screen,
            //making it reflect its direction if going outside the borders
            if(newPosition.x>=WORLD_WIDTH){
                newPosition.x = newPosition.x%(WORLD_WIDTH - 1);
            } else if(newPosition.x<0){
                newPosition.x = newPosition.x+(WORLD_WIDTH - 1);
            }
            if(newPosition.y>=WORLD_HEIGHT){
                newPosition.y = newPosition.y%(WORLD_HEIGHT - 1);
            } else if(newPosition.y<0){
                newPosition.y = newPosition.y+(WORLD_HEIGHT - 1);
            }
        } else{
            //Keep the probes inside the borders
            if(newPosition.x<0 || newPosition.x>WORLD_WIDTH || newPosition.y<0 || newPosition.y>WORLD_HEIGHT){
                newPosition.x = (float)Math.min(WORLD_WIDTH-0.1, (float)Math.max(0, newPosition.x));
                newPosition.y = (float)Math.min(WORLD_HEIGHT-0.1, (float)Math.max(0, newPosition.y));
                this.direction = RAND.nextFloat(2*(float)Math.PI); //Randomize the direction to a number between 0 and 2PI
            }
        }

        

        
        this.position = newPosition;
    }

    //Changes its direction based on the surrounding envrionment
    private void updateDirection(float[][] trailMap){
        float deltaTime = Gdx.graphics.getDeltaTime();
        //Sensing three points surrounding the probe
        float weightFront = sense(0, trailMap);
        float weightLeft = sense(sensor_angle_space, trailMap);
        float weightRight = sense(-sensor_angle_space, trailMap);
        
        
        if(weightFront>weightLeft && weightFront>weightRight){
            //Don't turn
        } else if(weightLeft>weightFront && weightRight>weightFront){
            //Turn a random value between left and right
            direction += (randomFloatFrom0To1() - 0.5) * 2 * turning_speed * deltaTime; //turning speed * deltaTime * a number from -1(included) to 1(excluded)
        } else if(weightLeft>weightRight){
            //Turn left
            direction += randomFloatFrom0To1() * turning_speed * deltaTime;
        } else if(weightRight>weightLeft){
            //Turn right
            direction -= randomFloatFrom0To1() * turning_speed * deltaTime;
        }
    }

    
    private float sense(float sensorAngleOffset, float[][] trailMap) {
        float sum = 0;
        float sensorDirection = this.direction + sensorAngleOffset;
        // Usa una copia di this.position per calcolare il centro del sensore
        Vector2 sensorCenter = this.position.cpy().add((float)Math.cos(sensorDirection) * sensor_offset_distance,
                (float)Math.sin(sensorDirection) * sensor_offset_distance);

        for (int offsetX = -sensor_radius; offsetX <= sensor_radius; offsetX++) {
            for (int offsetY = -sensor_radius; offsetY <= sensor_radius; offsetY++) {
                Vector2 pos = sensorCenter.cpy().add(offsetX, offsetY);

                if(loopingBorders){
                    // If the position is outside the map, make it sense on the opposite side
                    if (pos.x < 0) {
                        pos.x += WORLD_WIDTH;
                    } else if (pos.x >= WORLD_WIDTH) {
                        pos.x -= WORLD_WIDTH;
                    }
                    if (pos.y < 0) {
                        pos.y += WORLD_HEIGHT;
                    } else if (pos.y >= WORLD_HEIGHT) {
                        pos.y -= WORLD_HEIGHT;
                    }

                    // Evita indici fuori dai limiti
                    int x = (int) pos.x;
                    int y = (int) pos.y;

                    if (x >= 0 && x < WORLD_WIDTH && y >= 0 && y < WORLD_HEIGHT) {
                        sum += trailMap[x][y];
                    }
                } else{
                    // If the position is inside the map, sense
                    if(pos.x>0 && pos.x<WORLD_WIDTH && pos.y>0 && pos.y<WORLD_HEIGHT){
                        sum += trailMap[(int)pos.x][(int)pos.y];
                    }
                }
                
            }
        }

        return sum/(float)Math.pow(sensor_radius*2+1, 2);  //It returns the average of the square of the pixels tested by the sensor
    }

    
    public static float randomFloatFrom0To1(){
        return RAND.nextFloat(); //Return a number >=0.0 and <1
    }
    
    public static void randomizeProperties() {
        loopingBorders = RAND.nextBoolean();
        velocity = RAND.nextFloat(0.5f, 5f);
        sensor_angle_space = RAND.nextFloat(0.1f, (float)Math.PI/2);
        turning_speed = RAND.nextFloat(0.1f, 1f);
        sensor_offset_distance = RAND.nextInt(10, 300);
        sensor_radius = RAND.nextInt(1, 3);
    }
    
    public static String staticsToString(){
        return "The values of the variables shared between all of the probes are the following:" +
                "\nvelocity: " + velocity +
                "\nsensor_angle_space: " + sensor_angle_space +
                "\nturning_speed: " + turning_speed +
                "\nsensor_offset_distance: " + sensor_offset_distance +
                "\nsensor_radius: " + sensor_radius;
    }
    
    public static String getCurrentSystemVariables() {
        String s = "";
        
        s += "velocity: " + velocity + "\n";
        s += "sensor_angle_space: " + sensor_angle_space + "\n";
        s += "turning_speed: " + turning_speed + "\n";
        s += "sensor_offset_distance: " + sensor_offset_distance + "\n";
        s += "sensor_radius: " + sensor_radius + "\n";
        s += "loopingBorders: " + loopingBorders + "\n";
        
        return s;
    }
}
