package com.messenger.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import com.messenger.models.User;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by mamahendru on 4/10/17.
 */
@Service
public class UserDAO {
    @Autowired
    @Qualifier("configEnv")
    protected Properties configEnv;
    @Autowired
    DBUtil dbUtil;
    public static final String USER_COLLECTION = "user";

    /**
     *
     * @param user
     * Adds the user in mongodb
     */
    public void addUser(User user) {
        MongoClient mongoClient = null;
        try {
            mongoClient = dbUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, this.configEnv.getProperty("mongodb.db"));
            mongoOps.insert(user, USER_COLLECTION);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
    }

    /**
     *
     * @param userId
     * @return User object.
     * Currently doesn't lazy load the friends list. Populates entire friends list.
     * Loading only first level of friends list to avoid infinite looping
     *
     */
    public User getUser(String userId) {
        MongoClient mongoClient = null;
        User user = null;
        try{
            mongoClient = dbUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, this.configEnv.getProperty("mongodb.db"));
            Query query = new Query(Criteria.where("userId").is(userId));
            user = mongoOps.findOne(query, User.class, USER_COLLECTION);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
        return user;
    }

    /**
     *
     */
    public void updateUser(User user) {
        MongoClient mongoClient = null;
        try {
            User existingUser = getUser(user.getUserId());
            existingUser.setSessionId(user.getSessionId());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setPassword(user.getPassword());
            existingUser.setFriends(user.getFriends());
            existingUser.setStatus(user.getStatus());
            mongoClient = dbUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, this.configEnv.getProperty("mongodb.db"));
            mongoOps.save(existingUser);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
    }
}
