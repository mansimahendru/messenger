package com.messenger.db;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.UnknownHostException;
import java.util.Properties;

/**
 * Created by mamahendru on 4/10/17.
 */
@Service
public class DBUtil {
    @Autowired
    @Qualifier("configEnv")
    protected Properties configEnv;

    /**
     *
     * @return
     * @throws UnknownHostException
     */
    public MongoClient getMongoClient() throws UnknownHostException{
        MongoClient mongoClient = new MongoClient(this.configEnv.getProperty("mongodb.server"), Integer.parseInt(this.configEnv.getProperty("mongodb.port")));
        return mongoClient;
    }
}
