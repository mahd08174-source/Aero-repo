package com.aero.client.core.events;

/** Fired at the start (Pre) and end (Post) of every client tick. */
public final class TickEvent {
    private TickEvent() {}

    public static final class Pre {
        private static final Pre INSTANCE = new Pre();

        public static Pre get() { return INSTANCE; }
    }

    public static final class Post {
        private static final Post INSTANCE = new Post();

        public static Post get() { return INSTANCE; }
    }
}
