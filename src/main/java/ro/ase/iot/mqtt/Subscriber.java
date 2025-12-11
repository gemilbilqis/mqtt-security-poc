package ro.ase.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;

public class Subscriber {

    private static final String BROKER = "tcp://test.mosquitto.org:1883";
    private static final String TOPIC = "ro/ase/iot/mqtt";

    public static void main(String[] args) {
        System.out.println("Subscriber running...");
        try {
            MqttClient client = new MqttClient(BROKER, "SubscriberClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection lost: " + throwable);
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    System.out.println("Received message: ");
                    System.out.println("    → Topic: " + topic);
                    System.out.println("    → Payload: " + new String(mqttMessage.getPayload()));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) { }
            });

            client.connect(options);
            System.out.println("Subscriber connected...");

            client.subscribe(TOPIC, 1);
            System.out.println("Subscribed to: " + TOPIC);
            System.out.println("Waiting for messages...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
