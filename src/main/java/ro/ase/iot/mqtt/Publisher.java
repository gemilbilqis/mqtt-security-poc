package ro.ase.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import javax.net.ssl.SSLSocketFactory;
import java.nio.charset.StandardCharsets;

public class Publisher {

    private static final String BROKER = "ssl://broker.emqx.io:8883";
    private static final String TOPIC = "ro/ase/iot/mqtt";

    public static void main(String[] args) {
        System.out.println("Publisher running...");
        try {
            byte[] key = KeyLoader.loadKey("keys/aes.key");
            String plaintext = "Temperature reading: 23.7°C";
            String encryptedPayload = CryptoUtils.encrypt(plaintext, key);
            System.out.println("Encrypted payload: " + encryptedPayload);

            MqttClient client = new MqttClient(BROKER, "PublisherClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName("iot_user");
            options.setPassword("password123".toCharArray());

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            options.setSocketFactory(sslSocketFactory);

            client.connect(options);
            System.out.println("Publisher connected over TLS.");

            MqttMessage message = new MqttMessage(encryptedPayload.getBytes());
            message.setQos(1);

            client.publish(TOPIC, message);
            System.out.println("Published encrypted payload to topic: " + TOPIC);

            client.disconnect();
            client.close();
            System.out.println("Publisher finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
