
package GOPR_2;

import java.awt.Color;

/**
 *
 * @author Michal
 */
public class Grain {
    int id;
    int red;
    int green;
    int blue;


    public Grain( int red, int green, int blue) {
        this.id = red+green+blue;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public String toString() {
        return "Ziarno {" + "id=" + id + '}';
    }
    
    


    
    
    
    }
