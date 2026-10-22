package com.aero.client.core.utils;

/** Result of an inventory search. */
public record FindItemResult(int slot, int count) {
    public boolean found() { return slot != -1; }
}
