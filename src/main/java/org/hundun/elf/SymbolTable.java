package org.hundun.elf;

public class SymbolTable {
    int st_value;
    int st_name;
    int st_size;
    char st_info;
    char st_other;
    short st_shndx;

    public int getSt_name() {
        return st_name;
    }

    public void setSt_name(int st_name) {
        this.st_name = st_name;
    }

    public int getSt_value() {
        return st_value;
    }

    public void setSt_value(int st_value) {
        this.st_value = st_value;
    }

    public int getSt_size() {
        return st_size;
    }

    public void setSt_size(int st_size) {
        this.st_size = st_size;
    }

    public char getSt_info() {
        return st_info;
    }

    public void setSt_info(char st_info) {
        this.st_info = st_info;
    }

    public char getSt_other() {
        return st_other;
    }

    public void setSt_other(char st_other) {
        this.st_other = st_other;
    }

    public short getSt_shndx() {
        return st_shndx;
    }

    public void setSt_shndx(short st_shndx) {
        this.st_shndx = st_shndx;
    }
}
