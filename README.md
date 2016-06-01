# Setup

## Create a volume container for the binlog
```shell
docker create -v /var/log/mysql-bin --name mysql-bin db-mysql:dockerfile /bin/true
```

## Build the MYSQL docker image
```shell
cd docker/mysql/
docker build -t "db-mysql:dockerfile" .
docker run -d --volumes-from mysql-bin -p 3306:3306 --name my-mysql -e MYSQL_ROOT_PASSWORD=my-secret-pw db-mysql:dockerfile
```
