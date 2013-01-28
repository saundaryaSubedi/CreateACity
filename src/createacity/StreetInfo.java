package createacity;

public class StreetInfo {
    private String streetName, nextStreetName, prevStreetName;
    
    public StreetInfo(String streetName, String nextStreetName, String prevStreetName){
        this.streetName = streetName;
        this.nextStreetName = nextStreetName;
        this.prevStreetName = prevStreetName;
    }

    public String getNextStreetName() {
        return nextStreetName;
    }

    public void setNextStreetName(String nextStreetName) {
        this.nextStreetName = nextStreetName;
    }

    public String getPrevStreetName() {
        return prevStreetName;
    }

    public void setPrevStreetName(String prevStreetName) {
        this.prevStreetName = prevStreetName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }
    
    @Override
    public String toString() {
        return "[" + streetName + ", " + prevStreetName + ", " + nextStreetName + "]";
                
    }
}