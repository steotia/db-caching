require "kafka"
require "json"
require 'benchmark'


USERS = 1000

kafka = Kafka.new(
  seed_brokers: ["192.168.99.100:9092"],
  client_id: "demo",
)
producer = kafka.producer

puts Benchmark.measure {
  USERS.times do |x|
    producer.produce(JSON.dump({aid:x+1,bid:Random.rand(USERS+1)}), topic: "events")
    producer.deliver_messages
  end
}

puts "DONE"

