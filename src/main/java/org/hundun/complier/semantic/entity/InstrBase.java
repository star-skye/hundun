package org.hundun.complier.semantic.entity;

public class InstrBase {
    protected InstrKind kind;

    public InstrBase(InstrKind kind) {
        this.kind = kind;
    }

    public InstrKind getKind() {
        return kind;
    }
}
