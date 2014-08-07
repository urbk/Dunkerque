package wifiConnect;

/**
 * Created by Antoine on 26/06/2014.
 */
public enum Network {
    R1("Urbik"),
    R2("Urbik1"),
    R3("Urbik2"),
    R4("Urbik3");

    private String ssid = "";

    //Constructeur
    Network(String ssid) {
        this.ssid = ssid;
    }

    public String toString() {
        return ssid;
    }

}
