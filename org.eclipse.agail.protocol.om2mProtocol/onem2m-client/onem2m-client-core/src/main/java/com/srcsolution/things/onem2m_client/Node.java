package com.srcsolution.things.onem2m_client;

import java.util.Objects;

import com.srcsolution.things.onem2m_client.resource.CseBase;

public class Node {

    private String url;

    private String id;

    private String user;

    private String password;

    public Node() {
    }

    public Node(String url, String id, String user, String password) {
        this.url = url;
        this.id = id;
        this.user = user;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getResourceUrl(String resourceId) {
        String resourceUrl = getUrl() + "/~";
        if (resourceId != null && !resourceId.isEmpty()) {
            if (resourceId.charAt(0) != '/') {
                resourceUrl += "/";
            }
            resourceUrl += resourceId;
        }
        return resourceUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(getUrl(), node.getUrl()) &&
                Objects.equals(getId(), node.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUrl(), getId());
    }

    @Override
    public String toString() {
        return "Node{" +
                "url='" + url + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
