# Setup (Mac OSX)

## Startup docker-machine
```shell
cd docker
docker-machine up docker-vm
```

## Note the docker host IP
```shell
docker-machine env docker-vm
```
Use this at relevant places.

## Startup MYSQL
```shell
docker-compose up -d mysql
```

## Grant privileges
```shell
docker exec -it my-mysql bash -c "mysql -uroot -p -e \"GRANT ALL on demo.* to 'demo'@'%' identified by 'demo';CREATE DATABASE demo;SELECT User FROM mysql.user;\""
docker exec -it my-mysql bash -c "mysql -uroot -p -e \"GRANT ALL on maxwell.* to 'maxwell'@'%' identified by 'maxwell';GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE on *.* to 'maxwell'@'%';SELECT User FROM mysql.user;\""
```

## Start Kafka container
```shell
docker-compose up -d kafka
```

## Start maxwell binlog replicator
```shell
docker-compose run maxwell bash -c "bin/maxwell --user=maxwell --password=maxwell --host=mysql --producer=kafka --kafka.bootstrap.servers=kafka:9092"
```

## Check if maxwell topic is created
```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-topics.sh --zookeeper localhost:2181 --list"
```

## Put a consumer on maxwell and create some users
```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic maxwell"
```
```shell
cd data-generators/ruby
bundle exec ruby create_users.rb
```
You will have created 1000 records each on demo.A and demo.B

Now, let us look at how we can do DB denormalization while stream processing

## Publish some events into 'events' kafka topic
```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-topics.sh --create --topic events --zookeeper localhost:2181 --partitions 1 --replication-factor 1"
```
```shell
bundle exec ruby create_events.rb
```

Everytime you run ```create_events.rb``` 1000 arbitrary events will be logged of type ```{"aid":803,"bid":205}```.

## Install YARN
```shell
cd data-processors/stream-processors/samza
bin/grid install yarn
bin/grid start yarn
```
Once YARN is up, you should be able to access [http://localhost:8088/cluster](http://localhost:8088/cluster).

## Denormalize these events using Samza
```shell
mvn clean package
mkdir -p deploy/samza
tar -xvf ./target/db-caching-0.0.1-dist.tar.gz -C deploy/samza
deploy/samza/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/deploy/samza/config/stream-db-denormalization.properties
```
Wait for job to get into RUNNING status on [YARN](http://localhost:8088/cluster).

```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic db-de-normalized-events
```
Let this shell remain open.

## Try again (this time observe the pace of denormalization
```shell
bundle exec ruby create_events.rb
```

## Now, use the DB cache denormalizer
```shell
deploy/samza/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/deploy/samza/config/db-to-changelog.properties
```
## Compare stream-de-normalized-events vs db-de-normalized-events 
```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic stream-de-normalized-events"
```
```shell
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic db-de-normalized-events"
```
```shell
bundle exec ruby create_events.rb
```
Note the difference in speed.

Enjoy!
