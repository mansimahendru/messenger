package com.messenger.db;

import com.mongodb.MongoClient;

import java.net.UnknownHostException;

/**
 * Created by mamahendru on 4/10/17.
 */
public class DBUtil {
    public static final String MONGO_HOST = "localhost";
    public static final int MONGO_PORT = 27017;
    public static MongoClient getMongoClient() throws UnknownHostException{
        MongoClient mongoClient = new MongoClient(MONGO_HOST, MONGO_PORT);
        return mongoClient;
    }
}
