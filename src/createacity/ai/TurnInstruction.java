package createacity.ai;

import createacity.Direction;

public class TurnInstruction extends DrivingInstruction {
    private Direction dir;
    
    public TurnInstruction(Direction direction, String road) {
        dir = direction;
        this.road = road;
        text = "Turn " + direction + " onto " + road;
    }
    
    
}
