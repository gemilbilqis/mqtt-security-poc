package ro.ase.iot.mqtt.model;

public class TelemetryData {
    public String deviceId;
    public long timestamp;
    public int battery;
    public double temperature;
    public double humidity;
    public String status;

    //for Jackson
    public TelemetryData() {
    }
}
