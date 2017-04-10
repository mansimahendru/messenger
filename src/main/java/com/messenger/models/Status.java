package com.messenger.models;

/**
 * Created by mamahendru on 4/9/17.
 * Status enum show user's status in messenger system.
 * ACTIVE means user is signed in and active.
 * SIGNEDOUT means user is not logged in. User object has null session id. send/receive of messages
 * and update of contact list not possible if sessionid is null or do not match.
 * AWAY is not currently implemented.
 */
public enum Status {
    ACTIVE, AWAY, SIGNEDOUT
}
