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
    public float velocity;
    
    float lastWeightFront;
    float lastWeightLeft;
    float lastWeightRight;
    
    public static float sensor_angle_space = 0.3f; //In radians
    public static float turning_speed = 0.5f;
    public static int sensor_offset_distance = 100;
    public static int sensor_radius = 2;
    public static float reactivityFactor = 0.001f; //Used to control acceleration
    public static float maxVelocity = 0.5f;
    public static boolean loopingBorders = false;

    public Probe(Vector2 position, float direction, float velocity){
        this.position = position;
        this.direction = direction;
        this.velocity = velocity;
        
        //Initializes the last weights to zero to be able to compare it on the first updatePosition iteration
        lastWeightFront = 0;
        lastWeightLeft = 0;
        lastWeightRight = 0;
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
        
        float deltaFront = weightFront - lastWeightFront;
        float deltaLeft = weightLeft - lastWeightLeft;
        float deltaRight = weightRight - lastWeightRight;
        
        
        if(weightFront>weightLeft && weightFront>weightRight){
            //Don't turn
            velocity = Math.min(maxVelocity, Math.max(velocity, velocity + reactivityFactor * weightFront));
        } else if(weightLeft>weightFront && weightRight>weightFront){
            //Turn a random value between left and right
            direction += (randomFloatFrom0To1() - 0.5) * 2 * turning_speed * deltaTime;
            //accelerates/decelerates based on the average right-left pixel density
            velocity = Math.max(0, velocity - reactivityFactor * ((weightLeft+weightRight)/2));
        } else if(weightLeft>weightRight){
            //Turn left
            direction += randomFloatFrom0To1() * turning_speed * deltaTime;
            //accelerates/decelerates based on the left pixel density
            velocity = Math.max(0, velocity - reactivityFactor * weightLeft);
        } else if(weightRight>weightLeft){
            //Turn right
            direction -= randomFloatFrom0To1() * turning_speed * deltaTime;
            //accelerates/decelerates based on the right pixel density
            velocity = Math.max(0, velocity - reactivityFactor * weightRight);
        }
        
        //Stores the value of the weights for the next iteration
        lastWeightFront = weightFront*2;
        lastWeightLeft = weightLeft*2;
        lastWeightRight = weightRight*2;
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
    
    public static String staticsToString(){
        return "The values of the variables shared between all of the probes are the following:" +
                "\nsensor_angle_space: " + sensor_angle_space +
                "\nturning_speed: " + turning_speed +
                "\nsensor_offset_distance: " + sensor_offset_distance +
                "\nreactivity_factor: " + reactivityFactor +
                "\nsensor_radius: " + sensor_radius;
    }
}
