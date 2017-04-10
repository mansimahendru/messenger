package com.messenger.models;

import org.springframework.data.annotation.Id;

/**
 * Created by mamahendru on 4/10/17.
 */
public class Message {
    @Id
    private String id;
    private String to;
    private String from;
    private String message;

    public Message(String to, String from, String message) {
        this.to = to;
        this.from = from;
        this.message = message;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
