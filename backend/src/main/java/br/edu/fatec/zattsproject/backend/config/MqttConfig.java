package br.edu.fatec.zattsproject.backend.config;

import br.edu.fatec.zattsproject.backend.service.MqttLeituraService;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.util.UUID;

@Configuration
@EnableIntegration
public class MqttConfig {

    private static final String MQTT_BROKER_URL = "ssl://z600fcff.ala.us-east-1.emqxsl.com:8883";
    private static final String CLIENT_ID = "zatts-backend-sub-" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TOPIC = "zatts/dispositivos/leitura";

    private static final String MQTT_USER = "useresp32";
    private static final String MQTT_PASSWORD = "Esp1234@";

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setServerURIs(new String[] { MQTT_BROKER_URL });
        options.setUserName(MQTT_USER);
        options.setPassword(MQTT_PASSWORD.toCharArray());
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setKeepAliveInterval(30);

        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(CLIENT_ID, mqttClientFactory(), TOPIC);

        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler mqttHandler(MqttLeituraService mqttLeituraService) {
        return message -> {
            String payload = message.getPayload().toString();
            System.out.println(">>> MQTT CHEGOU COM SUCESSO: " + payload);
            mqttLeituraService.processarEMensurarLeitura(payload);
        };
    }
}