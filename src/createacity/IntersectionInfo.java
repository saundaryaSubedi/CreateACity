package createacity;

import java.util.HashMap;

public class IntersectionInfo {
    HashMap<String, Integer[]> roadIntersections;
    
    public IntersectionInfo(HashMap<String, Integer[]> roadIntersections){
        this.roadIntersections = roadIntersections;
    }
    
    @Override
    public String toString() {
        return roadIntersections.toString();
    }
    
    public HashMap<String, Integer[]> getRoadIntersections() {
        return roadIntersections;
    }
}