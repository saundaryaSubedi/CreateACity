package createacity.ai;


public abstract class DrivingInstruction {
    protected String road;
    protected String text;
    protected float distance;
    
    @Override
    public String toString() {
        return text;
    }
}
