FROM mysql:5.6

MAINTAINER Shashank Teotia "shashankteotia@gmail.com"

COPY  binlog.cnf  /etc/mysql/conf.d/binlog.cnf

RUN mkdir -p /var/log/mysql-bin
RUN chown -R mysql:mysql /var/log/mysql-bin
VOLUME ["/var/log/mysql-bin"]
