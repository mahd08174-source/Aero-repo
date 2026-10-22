package com.aero.client.api;

public record FindItemResult(int slot, int count) {
    public boolean found() { return slot != -1; }
}
