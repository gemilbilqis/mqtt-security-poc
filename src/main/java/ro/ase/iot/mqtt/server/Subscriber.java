package ro.ase.iot.mqtt.server;

import org.eclipse.paho.client.mqttv3.*;
import ro.ase.iot.mqtt.model.TelemetryData;
import ro.ase.iot.mqtt.utils.KeyLoader;
import ro.ase.iot.mqtt.utils.CryptoUtils;

import javax.net.ssl.SSLSocketFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Subscriber {
    private static final String BROKER = "ssl://broker.emqx.io:8883";
    private static final String TOPIC = "ro/ase/iot/mqtt";
    private static final String TELEMETRY_TOPIC_FILTER = "/+/telemetry";

    private static final Map<String, TelemetryData> lastState = new ConcurrentHashMap<>();
    private static final Map<String, Long> lastTimestamp = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Subscriber running...");
        try {
            byte[] key = KeyLoader.loadKey("keys/aes.key");

            MqttClient client = new MqttClient(BROKER, "SubscriberClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("iot_user");
            options.setPassword("password123".toCharArray());

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            options.setSocketFactory(sslSocketFactory);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection lost: " + throwable);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) {
                    try {
                        String encrypted = new String(mqttMessage.getPayload());
                        System.out.println("Encrypted payload received from topic: " + topic);
                        System.out.println("    → " + encrypted);

                        TelemetryData decryptedTelemetry = CryptoUtils.decryptTelemetry(encrypted, key, TelemetryData.class);
                        validateTelemetry(TOPIC + TELEMETRY_TOPIC_FILTER, decryptedTelemetry);
                        updateDeviceState(decryptedTelemetry);
                        printTelemetry(decryptedTelemetry);
                    } catch (Exception e) {
                        System.out.println("ERROR: Failed to decrypt message or parse telemetry!");
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }
            });

            System.out.println("Connecting to broker...");
            client.connect(options);
            System.out.println("Subscriber connected over TLS...");

            client.subscribe(TOPIC + TELEMETRY_TOPIC_FILTER, 1);
            System.out.println("Subscribed to: " + TOPIC + TELEMETRY_TOPIC_FILTER);
            System.out.println("Waiting for encrypted messages...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printTelemetry(TelemetryData t) {
        System.out.println("--------------------------------------------------");
        System.out.println("Telemetry Received:");
        System.out.println(" Device ID   : " + t.deviceId);
        System.out.println(" Timestamp   : " + t.timestamp);
        System.out.println(" Temperature : " + t.temperature + " °C");
        System.out.println(" Humidity    : " + t.humidity + " %");
        System.out.println(" Battery     : " + t.battery + " %");
        System.out.println(" Status      : " + t.status);

        if (t.status.equals("LOW_BATTERY")) {
            System.out.println(" [NOTICE] Device running on low battery!");
        }
        if (t.status.equals("HIGH_TEMP")) {
            System.out.println(" [WARNING] High temperature condition!");
        }
        if (t.status.equals("OVERHEAT")) {
            System.out.println(" [ALERT] Device overheating!");
        }

        System.out.println("--------------------------------------------------");
    }

    private static void updateDeviceState(TelemetryData t) {
        lastState.put(t.deviceId, t);
        lastTimestamp.put(t.deviceId, t.timestamp);
    }

    private static void validateTelemetry(String topic, TelemetryData t) {
        long now = System.currentTimeMillis();

        if (t.timestamp > now + 5000) System.out.println("[Warning] Device timestamp is ahead of server clock!");

        if (t.temperature < -10 || t.temperature > 80) System.out.println("[ALERT] Abnormal temperature reading detected!");

        if (t.humidity < 10 || t.humidity > 90) System.out.println("[ALERT] Humidity outside normal range!");

        if (t.battery < 0 || t.battery > 100) System.out.println("[ALERT] Invalid battery level!");

        if(lastTimestamp.containsKey(t.deviceId)) {
            long delta = t.timestamp - lastTimestamp.get(t.deviceId);
            if (delta < 0) System.out.println("[WARNING] Telemetry timestamp moved backwards!");
            if (delta > 120_000) System.out.println("[WARNING] Telemetry gap > 2 minutes detected!");
        }
    }
}
