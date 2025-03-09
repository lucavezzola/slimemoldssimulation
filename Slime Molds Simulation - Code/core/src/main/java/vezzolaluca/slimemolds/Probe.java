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
    public Vector2 position;
    public float direction;//The angle of the vector of motion in radians (from 0 to 2*PI radians ==> from 0 to 360 degrees)
    public static float velocity = 0.3f;
    
    public static float sensor_angle_space = 0.1f; //In radians
    public static float turning_speed = 2f;
    public static int sensor_offset_distance = 2;
    public static int sensor_radius = 1;
    
    private static final Random RAND = new Random();

    public Probe(Vector2 position, float direction){
        this.position = position;
        this.direction = direction;
    }
    public void updatePosition(float[][] trailMap){
        updateDirection(trailMap);
        int padding = 10;
        
        Vector2 newPosition = new Vector2(position.x+(float)Math.cos(direction)*velocity, position.y+(float)Math.sin(direction)*velocity);
        if(newPosition.x<0 || newPosition.x>WORLD_WIDTH || newPosition.y<0 || newPosition.y>WORLD_HEIGHT){
            newPosition.x = (float)Math.min(WORLD_WIDTH-0.1, (float)Math.max(0, newPosition.x));
            newPosition.y = (float)Math.min(WORLD_HEIGHT-0.1, (float)Math.max(0, newPosition.y));
            this.direction = RAND.nextFloat(2*(float)Math.PI);
        } else if(newPosition.x<padding || newPosition.x>WORLD_WIDTH-padding || newPosition.y<padding || newPosition.y>WORLD_HEIGHT-padding){
            this.direction += 0.1f;
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
    
    private float sense(float sensorAngleOffset, float[][] trailMap){
        float sum = 0;
        float sensorDirection = this.direction + sensorAngleOffset;
        // Usa una copia di this.position per calcolare il centro del sensore
        Vector2 sensorCenter = this.position.cpy().add((float)Math.cos(sensorDirection)*sensor_offset_distance,
                (float)Math.sin(sensorDirection)*sensor_offset_distance);
        
        for(int offsetX=-sensor_radius; offsetX<=sensor_radius; offsetX++){
            for(int offsetY=-sensor_radius; offsetY<=sensor_radius; offsetY++){
                Vector2 pos = sensorCenter.cpy().add(offsetX, offsetY);
                
                if(pos.x>0 && pos.x<WORLD_WIDTH && pos.y>0 && pos.y<WORLD_HEIGHT){
                    sum += trailMap[(int)pos.x][(int)pos.y];
                }
            }
        }
        
        return sum /= Math.pow(sensor_radius*2+1, 2); //It returns the average of the square of the pixels tested by the sensor
    }
    
    public static float randomFloatFrom0To1(){
        return RAND.nextFloat(); //Return a number >=0.0 and <1
    }
    
    public static String staticsToString(){
        return "The values of the variables shared between all of the probes are the following:" +
                "\nvelocity: " + velocity +
                "\nsensor_angle_space: " + sensor_angle_space +
                "\nturning_speed: " + turning_speed +
                "\nsensor_offset_distance: " + sensor_offset_distance +
                "\nsensor_radius: " + sensor_radius;
    }
}
