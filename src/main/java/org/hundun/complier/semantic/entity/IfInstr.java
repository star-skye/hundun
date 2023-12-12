package org.hundun.complier.semantic.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * if条件
 */
public class IfInstr extends InstrBase{

    private final ConditionInstr condition;

    private final ElseInstr elseInstr;

    private final List<InstrBase> instr = new ArrayList<>();

    public IfInstr(ConditionInstr condition, ElseInstr elseInstr,  AssignInstr... assign) {
        super(InstrKind.If);
        this.condition = condition;
        this.elseInstr = elseInstr;
        this.instr.addAll(Arrays.asList(assign)) ;
    }

    public IfInstr(InstrKind kind) {
        super(kind);
        this.condition = null;
        this.elseInstr = null;
    }

    public ConditionInstr getCondition() {
        return condition;
    }

    public ElseInstr getElseInstr() {
        return elseInstr;
    }

    public List<InstrBase> getInstr() {
        return instr;
    }
}
