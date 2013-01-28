package createacity;

public enum Direction {
    N ("North"),
    S ("South"),
    E ("East"),
    W ("West"),
    NE ("Northeast"),
    SE ("Southeast"),
    SW ("Southwest"),
    NW ("Northwest"),
    RIGHT ("Right"),
    LEFT ("Left");
    
    private final String fullDir;
    
    Direction(String fullDir) {
        this.fullDir = fullDir;
    }
    
    public String toString() {
        return fullDir;
    }
}
