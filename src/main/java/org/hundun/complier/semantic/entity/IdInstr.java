package org.hundun.complier.semantic.entity;

/**
 * 变量名
 */
public class IdInstr extends InstrBase{
    private final String name;
    public IdInstr(String name) {
        super(InstrKind.Name);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
