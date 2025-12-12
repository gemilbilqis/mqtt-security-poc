package ro.ase.iot.mqtt.utils;

import ro.ase.iot.mqtt.model.TelemetryData;

import java.util.concurrent.ThreadLocalRandom;

public class TelemetryGenerator {
    private static int batteryLevel = 100;
    private static double temperature = 22.0;
    private static double humidity = 45.0;

    public static TelemetryData generate(String deviceId) {
        TelemetryData t = new TelemetryData();
        t.deviceId = deviceId;
        t.timestamp = System.currentTimeMillis();

        temperature += (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.4;
        if(ThreadLocalRandom.current().nextDouble() < 0.01) {
            temperature += ThreadLocalRandom.current().nextDouble(2.0, 6.0);    //thermal spike of 1% chance
        }
        t.temperature = Math.round(temperature * 10.0) / 10.0;

        humidity += (ThreadLocalRandom.current().nextDouble() - 0.5) * 1.0;
        if(humidity < 30) humidity = 30;
        if(humidity > 60) humidity = 60;
        t.humidity = Math.round(humidity * 10.0) /10.0;

        batteryLevel -= ThreadLocalRandom.current().nextInt(0, 2);
        if (batteryLevel < 5) batteryLevel = 100;
        t.battery = batteryLevel;

        if (batteryLevel < 15) {
            t.status = "LOW_BATTERY";
        } else if (temperature > 28.0) {
            t.status = "OVERHEAT";
        } else if (temperature > 25.5) {
            t.status = "HIGH_TEMP";
        } else {
            t.status = "OK";
        }

        return t;
    }
}
