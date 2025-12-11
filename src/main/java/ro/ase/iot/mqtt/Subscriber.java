package ro.ase.iot.mqtt;

import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.SSLSocketFactory;

public class Subscriber {

    private static final String BROKER = "ssl://broker.emqx.io:8883";
    private static final String TOPIC = "ro/ase/iot/mqtt";

    public static void main(String[] args) {
        System.out.println("Subscriber running...");
        try {
            MqttClient client = new MqttClient(BROKER, "SubscriberClient");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            options.setSocketFactory(sslSocketFactory);

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

            System.out.println("Connecting to broker...");
            client.connect(options);
            System.out.println("Subscriber connected over TLS...");

            client.subscribe(TOPIC, 1);
            System.out.println("Subscribed to: " + TOPIC);
            System.out.println("Waiting for messages...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
