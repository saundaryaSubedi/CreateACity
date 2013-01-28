package createacity.info;

public class StreetAddress {
    private int streetNumber;
    private String streetName;
    
    private StreetAddress(int number, String name) {
        streetNumber = number;
        streetName = name;
    }
    
    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }
    
}
