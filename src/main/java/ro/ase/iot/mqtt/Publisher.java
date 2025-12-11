package ro.ase.iot.mqtt;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

public class Publisher {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String TOPIC = "ro/ase/iot/mqtt";

    public static void main(String[] args) {
        System.out.println("Publisher running...");
        try {
            MqttClient client = new MqttClient(BROKER, "PublisherClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.connect(options);
            System.out.println("Publisher connected.");

            String messageText = "Hello MQTT from Java!";
            MqttMessage message = new MqttMessage(messageText.getBytes());
            message.setQos(1);

            client.publish(TOPIC, message);
            System.out.println("Published: " + messageText);

            client.disconnect();
            client.close();
            System.out.println("Publisher finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
