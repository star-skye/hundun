package org.hundun.complier.semantic.entity;

/**
 * else条件
 */
public class ElseInstr extends InstrBase{
    private final AssignInstr assign;
    public ElseInstr(AssignInstr assign) {
        super(InstrKind.Else);
        this.assign = assign;
    }

    public AssignInstr getAssign() {
        return assign;
    }
}
