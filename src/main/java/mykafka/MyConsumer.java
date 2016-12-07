package mykafka;

import java.util.Arrays;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import database.MongoDb;
import util.MyProperties;

public class MyConsumer {

	private static KafkaConsumer<String, String> kafkaConsumer;

	public static void main(String[] args) {
		System.out.println("It's started!");
		MyProperties myProperties = new MyProperties();
		myProperties.loadProperties("conf/myconsumer.properties");
		kafkaConsumer = new KafkaConsumer<>(myProperties);
		kafkaConsumer.subscribe(Arrays.asList("TEKTU"));
		MongoDb mongoDb = new MongoDb();
		new Thread() {
			public void run() {
				while (true) {
					ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
					for (ConsumerRecord<String, String> record : records) {
						System.out.printf("offset = %d, value = %s", record.offset(), record.value());
						System.out.println();
						mongoDb.addToTable("kafka", "TEKTU", record.value());
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();

	}

}
