require 'pry'
require 'mysql2'
require 'benchmark'

client = Mysql2::Client.new(host: "192.168.99.100", username: "demo", password: "demo", database: "demo" )
client.query("DROP TABLE A;") rescue nil
client.query("DROP TABLE B;") rescue nil
client.query("CREATE TABLE A (ID INT NOT NULL PRIMARY KEY, VALUE VARCHAR(40));")
client.query("CREATE TABLE B (ID INT NOT NULL PRIMARY KEY, VALUE VARCHAR(40));")
puts Benchmark.measure {
  1000.times do |i|
    client.query("INSERT INTO A VALUES (#{i+1},#{i+1});")
    client.query("INSERT INTO B VALUES (#{i+1},#{i+1});")
  end
}
