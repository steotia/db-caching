#!/bin/bash

docker exec -it my-mysql bash -c "mysql -uroot -p -e \"GRANT ALL on demo.* to 'demo'@'%' identified by 'demo';CREATE DATABASE demo;SELECT User FROM mysql.user;\""
docker exec -it my-mysql bash -c "mysql -uroot -p -e \"GRANT ALL on maxwell.* to 'maxwell'@'%' identified by 'maxwell';GRANT SELECT, REPLICATION CLIENT, REPLICATION SLAVE on *.* to 'maxwell'@'%';SELECT User FROM mysql.user;\""
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-topics.sh --create --topic users --zookeeper localhost:2181 --partitions 1 --replication-factor 1"
docker exec -it my-kafka bash -c "/opt/kafka_2.11-0.8.2.1/bin/kafka-topics.sh --create --topic events --zookeeper localhost:2181 --partitions 1 --replication-factor 1"


#bin/maxwell --user=maxwell --password=maxwell --host=mysql --producer=kafka --kafka.bootstrap.servers=kafka:9092
