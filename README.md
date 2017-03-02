# KafkaConsumer
This project consumes data from Apache Kafka Producer service and writes it mongo database under topics.

This is kafka producer project that produce data for this consumer project: https://github.com/brscrt/KafkaProducer

This project is a part of [this project](https://github.com/brscrt/Volume-Weighted-Average-Price).

## Dockerizing this project
This project can be dockerized with this prepared [Dockerfile](Dockerfile). If you want to generate a Dockerfile by yourself, you can run distDocker gradle task that is situated in [build.gradle file](build.gradle).

## Add Apache Kafka and MongoDb libraries to project
Apache Kafka and MongoDb libraries should be added as dependency. 

```sh
compile group: 'org.apache.kafka', name: 'kafka_2.11', version: '0.10.0.0'
compile group: 'org.mongodb', name: 'mongo-java-driver', version: '3.2.2' 
```
and refresh gradle project

## Apache Kafka properties
A config file must be defined to comminicate kafka service. In this project, myproperties.properties file was used as shown below.

```
bootstrap.servers=172.17.0.3:9092
group.id=group-1
enable.auto.commit=true
auto.commit.interval.ms=1000
auto.offset.reset=earliest
session.timeout.ms=30000
key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```
### Kafka in Docker
The kafka service running in docker was used in this project. Before run kafka, zookeper should be run. In this project, kafka and zookeeper images were pulled as shown below.

#### Usage of zookeeper
```sh
docker pull brscrt/zookeeper
docker run -d -p 2181:2181 --name zookeeper brscrt/zookeeper
```
Zookeeper was started as daemon (-d) and manually assigned port (-p) 2181:2181.

#### Usage of kafka
```sh
docker pull brscrt/kafka
docker run -d --name kafka --link zookeeper:zookeeper brscrt/kafka
```
By this way Kafka service was started and linked to the running zookeeper.

## Mongo Properties
Mongo was started at localhost via 27017 port.

### Mongo in Docker
The mongo service running in docker was used in this project. In this project, mongo image was pulled as shown below.

#### Usage of mongodb
```sh
docker pull brscrt/mongodb
docker run -d -p 27017:27017 --name mongodb -e AUTH=no brscrt/mongodb
```
Mongodb was started as daemon (-d) without authorized and manually assigned port (-p) 27017:27017


## Consumer class

```java
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
```

First it loads the properties file that we specify.
```java
myProperties.loadProperties("conf/myconsumer.properties");
```
Then we create KafkaProducer object with myProperties.
```java
kafkaConsumer = new KafkaConsumer<>(myProperties);
```
Wanted to subscribe topics is defined
```java
kafkaConsumer.subscribe(Arrays.asList("TEKTU"));
```
Every 100 ms it scans for the data
```java
ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
```
Finally, all data is writed to mongoDb via Mongo class that is specified.
```java
mongoDb.addToTable("kafka", "TEKTU", record.value());
```

## Mongo class
```java
public class MongoDb {	

	public void addToTable(String table, String key, String value) {
		MongoClient mongoClient = null;
		try {
			//mongoClient = new MongoClient("localhost",27017); //for direct running in ubuntu
			mongoClient = new MongoClient("172.17.0.1",27017);  //if this app is in docker
			MongoDatabase db = mongoClient.getDatabase("datas");
			
			MongoCollection<Document> collection = db.getCollection(table);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("topic", key);
			map.put("data", value);

			collection.insertOne(new Document(map));
			System.out.println("Added to mongodb");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Failed to add to mongodb");
		} finally {
			mongoClient.close();
		}
	}
}
```

As shown, the communication is being provided with localhost via 27017 port. And the coming data are being writed to mongodb in docker. 
