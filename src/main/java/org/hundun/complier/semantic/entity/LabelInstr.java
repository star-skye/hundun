package org.hundun.complier.semantic.entity;

/**
 * else条件
 */
public class LabelInstr extends InstrBase{
    private final String name;
    public LabelInstr(String name) {
        super(InstrKind.Label);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
