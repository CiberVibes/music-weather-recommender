package es.ulpgc.dacd.business.handler;

public interface EventHandler {
    void handle(String json);
    default void setActive(boolean active) {}
}
