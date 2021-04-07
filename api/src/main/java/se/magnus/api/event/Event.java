package se.magnus.api.event;

import java.time.LocalDateTime;

public class Event<T, K> {

    public enum Type {CREATE, DELETE}

    public Type type;
    public T key;
    public K data;
    public LocalDateTime createdAt;


    public Event() {
        this.type = null;
        this.key = null;
        this.data = null;
        this.createdAt = null;
    }

    public Event(Type type, T key, K data) {
        this.type = type;
        this.key = key;
        this.data = data;
        this.createdAt = LocalDateTime.now();
    }

    public Type getType() {
        return type;
    }

    public T getKey() {
        return key;
    }

    public K getData() {
        return data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
