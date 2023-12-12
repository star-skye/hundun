package org.hundun.complier.semantic.entity;

/**
 * else条件
 */
public class GotoInstr extends InstrBase{
    private final String name;
    public GotoInstr(String name) {
        super(InstrKind.Goto);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
