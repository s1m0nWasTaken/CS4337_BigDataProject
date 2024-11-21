package CS4337.Project;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
  public static final String SHOP_QUEUE = "shop.queue";
  public static final String SHOP_DEAD_LETTER_QUEUE = "shop.dlq";
  public static final String DIR_EXCHANGE = "amq.direct";
  public static final String DL_EXCHANGE = "dx.exchange";
  public static final String SHOP_ROUTING_KEY = "shop";
  public static final String SHOP_DLQ_ROUTING_KEY = "shop.dlq";
  public static final int MESSAGE_ACK_TIMEOUT = 15000;
  public static final int SHOP_RETRY_DELAY = 60000;

  @Bean
  public DirectExchange directExchange() {
    return new DirectExchange(DIR_EXCHANGE);
  }

  // Define the DirectExchange for the DLQ
  @Bean
  public DirectExchange deadLetterExchange() {
    return new DirectExchange(DL_EXCHANGE);
  }

  // Define the shop queue (with DLQ arguments)
  @Bean
  public Queue shopQueue() {
    return QueueBuilder.durable(SHOP_QUEUE)
        .withArgument("x-dead-letter-exchange", DL_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", SHOP_DLQ_ROUTING_KEY)
        .withArgument("x-message-ttl", MESSAGE_ACK_TIMEOUT) // TTL in milliseconds
        .build();
  }

  // Define the dead letter queue
  @Bean
  public Queue deadLetterQueue() {
    return QueueBuilder.durable(SHOP_DEAD_LETTER_QUEUE)
        .withArgument(
            "x-dead-letter-exchange", DIR_EXCHANGE) // Send messages back to the main exchange
        .withArgument("x-dead-letter-routing-key", SHOP_ROUTING_KEY)
        .withArgument("x-message-ttl", SHOP_RETRY_DELAY)
        .build();
  }

  // Bind the shop queue to the amq.direct exchange with the shop routing key
  @Bean
  public Binding shopQueueBinding(Queue shopQueue, DirectExchange directExchange) {
    return BindingBuilder.bind(shopQueue).to(directExchange).with(SHOP_ROUTING_KEY);
  }

  // Bind the DLQ to the dead letter exchange with the shop.dlq routing key
  @Bean
  public Binding deadLetterQueueBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
    return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(SHOP_DLQ_ROUTING_KEY);
  }
}