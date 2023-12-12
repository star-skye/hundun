package org.hundun.complier.semantic.entity;

/**
 * 条件
 */
public class ConditionInstr  extends InstrBase{
    private final IdInstr op1;
    private final IdInstr op2;
    private final String operate;
    public ConditionInstr(IdInstr op1, IdInstr op2, String operate) {
        super(InstrKind.Condition);
        this.op1 = op1;
        this.op2 = op2;
        this.operate = operate;
    }

    public IdInstr getOp1() {
        return op1;
    }

    public IdInstr getOp2() {
        return op2;
    }

    public String getOperate() {
        return operate;
    }
}
