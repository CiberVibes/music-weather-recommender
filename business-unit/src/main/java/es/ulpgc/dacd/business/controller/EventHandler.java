package es.ulpgc.dacd.business.controller;

public interface EventHandler {
    void handle(String json);
    default void setActive(boolean active) {}
}
