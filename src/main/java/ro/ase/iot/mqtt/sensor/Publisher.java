package ro.ase.iot.mqtt.sensor;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import ro.ase.iot.mqtt.model.TelemetryData;
import ro.ase.iot.mqtt.utils.KeyLoader;
import ro.ase.iot.mqtt.utils.CryptoUtils;
import ro.ase.iot.mqtt.utils.TelemetryGenerator;

import javax.net.ssl.SSLSocketFactory;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Publisher {

    private static final String BROKER = "ssl://broker.emqx.io:8883";
    private static final String TOPIC = "ro/ase/iot/mqtt";
    private static final String DEVICE_ID = "sensor01";
    private static final String TELEMETRY_EXTENSION = "/" + DEVICE_ID + "/telemetry";

    public static void main(String[] args) {
        System.out.println("Publisher running...");
        try {
            byte[] key = KeyLoader.loadKey("keys/aes.key");


            MqttClient client = new MqttClient(BROKER, "PublisherClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("iot_user");
            options.setPassword("password123".toCharArray());

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            options.setSocketFactory(sslSocketFactory);

            client.connect(options);
            System.out.println("Publisher connected over TLS.");
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(() -> {
                sendEncryptedTelemetry(client, key);
            }, 2, 30, TimeUnit.SECONDS);

            System.out.println("Periodic encrypted telemetry started.");
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendEncryptedTelemetry(MqttClient client, byte[] key) {
        try {
            TelemetryData telemetryData = TelemetryGenerator.generate(DEVICE_ID);

            String encryptedPayload = CryptoUtils.encryptTelemetry(telemetryData, key);
            System.out.println("Encrypted payload: " + encryptedPayload);

            MqttMessage message = new MqttMessage(encryptedPayload.getBytes());
            message.setQos(1);

            client.publish(TOPIC + TELEMETRY_EXTENSION, message);
            System.out.println("Published encrypted payload to topic: " + TOPIC + TELEMETRY_EXTENSION + " at " + telemetryData.timestamp);
        } catch (Exception e) {
            System.out.println("ERROR: Failed to publish telemetry!");
            e.printStackTrace();
        }
    }
}
