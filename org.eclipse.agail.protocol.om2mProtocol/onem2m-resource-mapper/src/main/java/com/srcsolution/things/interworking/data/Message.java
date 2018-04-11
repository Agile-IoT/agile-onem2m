package com.srcsolution.things.interworking.data;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;

public class Message {

    private ZonedDateTime timestamp;

    private String payload;

    private Double[] geolocation; // lat/lng/(elev)

    private Map<String, Object> metadata;

    private Topic topic;

    private ZonedDateTime resourceCreationDate;

    public Message() {
        this.timestamp = ZonedDateTime.now();
    }

    public Message(Topic topic) {
        this();
        this.topic = topic;
    }

    public Message(ZonedDateTime timestamp, String payload, Topic topic) {
        this.timestamp = timestamp;
        this.payload = payload;
        this.topic = topic;
    }

    public Message(ZonedDateTime timestamp, ZonedDateTime resourceCreationDate, String payload, Topic topic) {
        this.timestamp = timestamp;
        this.resourceCreationDate = resourceCreationDate;
        this.payload = payload;
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public Message setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Topic getTopic() {
        return topic;
    }

    public ZonedDateTime getResourceCreationDate() {
        return resourceCreationDate;
    }

    public void setResourceCreationDate(ZonedDateTime resourceCreationDate) {
        this.resourceCreationDate = resourceCreationDate;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Double getLatitude() {
        return geolocation != null ? geolocation[0] : null;
    }

    public Double getLongitude() {
        return geolocation != null ? geolocation[1] : null;
    }

    public void setGeolocation(Double lat, Double lng) {
        setLatitude(lat);
        setLongitude(lng);
    }

    public void setLatitude(Double d) {
        if (geolocation == null) {
            geolocation = new Double[2];
        }
        geolocation[0] = d;
    }

    public void setLongitude(Double d) {
        if (geolocation == null) {
            geolocation = new Double[2];
        }
        geolocation[1] = d;
    }

    public Double[] getGeolocation() {
        return geolocation;
    }

    public Message setGeolocation(Double[] geolocation) {
        this.geolocation = geolocation;
        return this;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Message setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(getTimestamp(), message.getTimestamp()) &&
                Objects.equals(getPayload(), message.getPayload()) &&
                Objects.equals(getTopic(), message.getTopic());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTimestamp(), getPayload(), getTopic());
    }
}
