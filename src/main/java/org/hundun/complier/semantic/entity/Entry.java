package org.hundun.complier.semantic.entity;

public class Entry {
    public InstrBase value;
    public Entry left;
    public Entry right;

    public Entry(InstrBase value, Entry left, Entry right) {
        this.value = value;
        this.left = left;
        this.right = right;
    }
}