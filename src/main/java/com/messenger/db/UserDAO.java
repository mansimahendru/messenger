package com.messenger.db;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.ArrayList;

import com.messenger.models.User;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;

/**
 * Created by mamahendru on 4/10/17.
 */
public class UserDAO {
    public static final String DB_NAME = "messengerdb";
    public static final String USER_COLLECTION = "user";

    /**
     *
     * @param user
     * Adds the user in mongodb
     */
    public void addUser(User user) {
        MongoClient mongoClient = null;
        try {
            mongoClient = DBUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, DB_NAME);
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
            mongoClient = DBUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, DB_NAME);
            Query query = new Query(Criteria.where("userId").is(userId));
            user = mongoOps.findOne(query, User.class, USER_COLLECTION);
        }
        catch(Exception ex) {

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
            mongoClient = DBUtil.getMongoClient();
            MongoOperations mongoOps = new MongoTemplate(mongoClient, DB_NAME);
            mongoOps.save(existingUser);
        }
        catch(Exception ex) {

        }
        finally {
            if(mongoClient != null)
                mongoClient.close();
        }
    }

    /**
     *
     * @param user
     * @return BasicDBObject constructed out of user object
     * Storing only userids of users in friends list to stop the cyclic operations
     */

    public BasicDBObject getDBUserObject(User user) {
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put("user_id",user.getUserId());
        dbObject.put("first_name",user.getUserId());
        dbObject.put("last_name",user.getUserId());
        dbObject.put("password",user.getUserId());
        dbObject.put("session_id",user.getUserId());
        dbObject.put("status",user.getUserId());
        List<BasicDBObject> friends = new ArrayList<BasicDBObject>();
        for(User u : user.getFriends()) {
            BasicDBObject obj = new BasicDBObject();
            obj.put("user_id", u.getUserId());
        }
        dbObject.put("friends",friends);
        return dbObject;
    }
}
