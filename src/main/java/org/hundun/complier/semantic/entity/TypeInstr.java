package org.hundun.complier.semantic.entity;

/**
 * 类型
 */
public class TypeInstr extends InstrBase{
    private final String type;
    public TypeInstr(String type) {
        super(InstrKind.Type);
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
