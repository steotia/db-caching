package demo.examples.denormalization.task;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.system.SystemStream;
import org.apache.samza.task.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

/**
 * Created by shashankteotia on 6/3/16.
 */
public class DBDenormalizationStreamTask implements StreamTask, InitableTask, WindowableTask {
    private static final String VALUE = "value";
    private String oTopic;
    private TaskContext context;
    private KeyValueStore<String, Map> aData;
    private KeyValueStore<String, Map> bData;
    private String dbHost;
    private String dbPort;
    private String dbUserName;
    private String dbPassword;
    private String dbSchema;

    @Override
    public void init(Config config, TaskContext taskContext) throws Exception {
        oTopic = config.get("output.success.topic.name", "db-de-normalized-events");
        dbHost = config.get("db.host");
        dbPort = config.get("db.port");
        dbUserName = config.get("db.userName");
        dbPassword = config.get("db.password");
        dbSchema = config.get("db.schema");
    }

    @Override
    public void process(IncomingMessageEnvelope incomingMessageEnvelope, MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {
        Map<String, Object> message = (Map<String, Object>) incomingMessageEnvelope.getMessage();
        Statement statement = null;
        Connection connection = null;
        ResultSet aResultSet = null;
        ResultSet bResultSet = null;
        System.out.println(message);
        String aQuery = String.format("select * from A where id = '%s'", message.get("aid"));
        String bQuery = String.format("select * from B where id = '%s'", message.get("bid"));
        try{
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url = String.format("jdbc:mysql://%s:%s/%s", dbHost, dbPort, dbSchema);
            connection = DriverManager
                    .getConnection(url, dbUserName, dbPassword);
            statement = connection.createStatement();
            aResultSet = statement.executeQuery(aQuery);
            while(aResultSet.next()){
                System.out.println("a->"+aResultSet.getString(VALUE));
                message.put("aval", aResultSet.getString(VALUE));
            }
            aResultSet.close();
            bResultSet = statement.executeQuery(bQuery);
            while(bResultSet.next()) {
                System.out.println("b->"+bResultSet.getString(VALUE));
                message.put("bval", bResultSet.getString(VALUE));
            }
            bResultSet.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(statement!=null)
                statement.close();
            if(connection!=null)
                connection.close();
            if((aResultSet!=null)&&(!aResultSet.isClosed()))
                aResultSet.close();
            if((bResultSet!=null)&&(!bResultSet.isClosed()))
                bResultSet.close();

        }
        messageCollector.send(new OutgoingMessageEnvelope(new SystemStream("kafka", oTopic), message));
    }

    @Override
    public void window(MessageCollector messageCollector, TaskCoordinator taskCoordinator) throws Exception {

    }
}
