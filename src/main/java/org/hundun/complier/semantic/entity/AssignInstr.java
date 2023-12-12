package org.hundun.complier.semantic.entity;

/**
 * 赋值
 */
public class AssignInstr extends InstrBase{

    private final IdInstr idInstr;
    private final InstrBase instr;

    public AssignInstr(IdInstr idInstr, InstrBase numberInstr) {
        super(InstrKind.Assign);
        this.idInstr = idInstr;
        this.instr = numberInstr;
    }

    public IdInstr getIdInstr() {
        return idInstr;
    }

    public InstrBase getInstr() {
        return instr;
    }
}
