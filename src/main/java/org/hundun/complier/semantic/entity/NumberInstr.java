package org.hundun.complier.semantic.entity;

/**
 * 常数
 */
public class NumberInstr extends InstrBase{
    private final Integer number;
    public NumberInstr(Integer number) {
        super(InstrKind.Number);
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }
}
