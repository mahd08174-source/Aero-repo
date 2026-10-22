package com.aero.client.core.events;

/** Fired when the player disconnects from a world or server. */
public final class GameLeftEvent {
    private static final GameLeftEvent INSTANCE = new GameLeftEvent();

    private GameLeftEvent() {}

    public static GameLeftEvent get() { return INSTANCE; }
}
