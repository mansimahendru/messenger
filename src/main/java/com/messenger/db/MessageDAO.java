package com.messenger.db;

import com.mongodb.MongoClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

import com.messenger.models.Message;

/**
 * Created by mamahendru on 4/10/17.
 */
@Service
public class MessageDAO {
    @Autowired
    @Qualifier("configEnv")
    protected Properties configEnv;
    @Autowired
    DBUtil dbUtil;
    public static final String MESSAGE_COLLECTION = "messages";

    /**
     *
     * @param message
     * we throw exception as if we can't save message, user needs to know
     */
    public void addMessage(Message message) throws Exception{
        MongoClient mongoClient = null;
        try{
            mongoClient = dbUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, this.configEnv.getProperty("mongodb.db"));
            mongoOps.insert(message, MESSAGE_COLLECTION);
        }
        catch(Exception ex){
            //throw exception as user needs to know send message failed
            throw ex;
        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
    }

    /**
     *
     * @param to
     * @return
     * If we can't find messages or there is exception, simple return null
     *
     */
    public List<Message> getMessages(String to) {
        MongoClient mongoClient = null;
        try {
            mongoClient = dbUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, this.configEnv.getProperty("mongodb.db"));
            Query query = new Query(Criteria.where("to").is(to));
            List<Message> messages = mongoOps.findAllAndRemove(query, Message.class, MESSAGE_COLLECTION);
            return messages;
        }
        catch(Exception ex){
            //If exception, return null.
            ex.printStackTrace();
        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
        return null;
    }
}
