package org.hundun.complier.semantic.entity;

/**
 * 声明
 */
public class DeclarationInstr extends InstrBase{
    private final TypeInstr type;
    private final IdInstr id;

    public DeclarationInstr(TypeInstr type, IdInstr id) {
        super(InstrKind.Declaration);
        this.type = type;
        this.id = id;
    }

    public TypeInstr getType() {
        return type;
    }

    public IdInstr getId() {
        return id;
    }
}
