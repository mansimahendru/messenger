package com.messenger.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by mamahendru on 4/9/17.
 * User is the registered user of the messenger
 * friends maintains the list of contacts of user
 * friends are kept at serer side so if user logs in from any client, it always has correct contact list
 * sessionid is used to identify user's session
 * sessionid ensures that one user cannot see another user's message or update contact list
 * status is user's status in messenger system. This can also be used to determine presense though it is currently not implemented.
 * userid is unique identifier of user in messenger system.
 */
public class User {
    private String userId;
    private String firstName;
    private String lastName;
    private String password;
    private Status status;
    private List<User> friends;
    private String sessionId;

    public User(String userId, String password, String firstName, String lastName) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.status = Status.ACTIVE;
        friends = new ArrayList<User>();
        sessionId = UUID.randomUUID().toString();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void addFriend(User user) {
        friends.add(user);
    }

    public void removeFriend(User user) {
        friends.remove(user);
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!firstName.equals(user.firstName)) return false;
        if (!friends.equals(user.friends)) return false;
        if (!lastName.equals(user.lastName)) return false;
        if (!password.equals(user.password)) return false;
        if (status != user.status) return false;
        if (!userId.equals(user.userId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + friends.hashCode();
        return result;
    }
}
