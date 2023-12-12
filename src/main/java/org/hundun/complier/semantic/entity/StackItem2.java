package org.hundun.complier.semantic.entity;

public class StackItem2 {
    public int operand;
    public String symbol;
    public String value;

    public Entry entry;

    public StackItem2(int operand, String symbol, String value) {
        this.operand = operand;
        this.symbol = symbol;
        this.value = value;
    }


}
