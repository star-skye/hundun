package org.hundun.complier.lr0;

public class StackItem {
    public int operand;
    public String symbol;
    public String value;

    public Entry entry;

    public StackItem(int operand, String symbol, String value) {
        this.operand = operand;
        this.symbol = symbol;
        this.value = value;
    }


    public static Entry createEntry(String value, Entry left, Entry right) {
        return new Entry(value, left, right);
    }

    public static class Entry {
        public String value;
        public Entry left;
        public Entry right;

        public Entry(String value, Entry left, Entry right) {
            this.value = value;
            this.left = left;
            this.right = right;
        }
    }
}
