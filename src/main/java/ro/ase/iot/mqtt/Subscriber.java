package ro.ase.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.SSLSocketFactory;
import java.security.Key;

public class Subscriber {

    private static final String BROKER = "ssl://broker.emqx.io:8883";
    private static final String TOPIC = "ro/ase/iot/mqtt";

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
                        System.out.println("Encrypted payload received: " + encrypted);

                        String plaintext = CryptoUtils.decrypt(encrypted, key);
                        System.out.println("Received message: ");
                        System.out.println("    → Topic: " + topic);
                        System.out.println("    → Payload: " + plaintext);
                    } catch (Exception e) {
                        System.out.println("ERROR: Failed to decrypt message!");
                        e.printStackTrace();
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }
            });

            System.out.println("Connecting to broker...");
            client.connect(options);
            System.out.println("Subscriber connected over TLS...");

            client.subscribe(TOPIC, 1);
            System.out.println("Subscribed to: " + TOPIC);
            System.out.println("Waiting for encrypted messages...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
