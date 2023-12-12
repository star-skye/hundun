/*
 * BinUtils - access various binary formats from Java
 *
 * (C) Copyright 2016 - JaWi - j.w.janssen@lxtreme.nl
 *
 * Licensed under Apache License v2.
 */
package org.hundun.elf;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents information about the various sections in an ELF object.
 */
public class SectionHeader {
    private final int nameOffset;
    private String name;

    public final SectionType type;
    public final long flags;
    public final long virtualAddress;
    public final long fileOffset;
    public final long size;
    public final int link;
    public final int info;
    public final long sectionAlignment;
    public final long entrySize;

    public List<SymbolTable> symbolTables;

    public SectionHeader(ElfClass elfClass, ByteBuffer buf, FileChannel fileChannel) throws IOException {
        nameOffset = buf.getInt();
        type = SectionType.valueOf(buf.getInt());

        if (elfClass == ElfClass.CLASS_32) {
            flags = buf.getInt() & 0xFFFFFFFFL;
            virtualAddress = buf.getInt() & 0xFFFFFFFFL;
            fileOffset = buf.getInt() & 0xFFFFFFFFL;
            size = buf.getInt() & 0xFFFFFFFFL;
        } else if (elfClass == ElfClass.CLASS_64) {
            flags = buf.getLong();
            virtualAddress = buf.getLong();
            fileOffset = buf.getLong();
            size = buf.getLong();
        } else {
            throw new IOException("Unhandled ELF-class!");
        }

        link = buf.getInt();
        info = buf.getInt();

        if (elfClass == ElfClass.CLASS_32) {
            sectionAlignment = buf.getInt() & 0xFFFFFFFFL;
            entrySize = buf.getInt() & 0xFFFFFFFFL;
        } else if (elfClass == ElfClass.CLASS_64) {
            sectionAlignment = buf.getLong();
            entrySize = buf.getLong();
        } else {
            throw new IOException("Unhandled ELF-class!");
        }
        if (type == SectionType.SYMTAB) {
            symbolTables = new ArrayList<>();
//            ByteBuffer buffer = ByteBuffer.allocateDirect((int) size);
//            fileChannel.position(fileOffset);
//            Elf.readFully(fileChannel, buffer, "Unable to read symbol tables entry");
//            for (long i = 0; i < entrySize; i++) {
//                SymbolTable symbolTable = new SymbolTable();
//                symbolTable.st_name = buffer.getInt();
//                symbolTable.st_value = buffer.getInt();
//                symbolTable.st_size = buffer.getInt();
//                symbolTable.st_info = (char) buffer.get();
//                symbolTable.st_other = (char) buffer.get();
//                symbolTable.st_shndx = buffer.getShort();
//                symbolTables.add(symbolTable);
//            }
        }
    }

    public String getName() {
        return name;
    }

    void setName(ByteBuffer buf) {
        if (nameOffset > 0) {
            byte[] array = buf.array();

            int end = nameOffset;
            while (end < array.length && array[end] != 0) {
                end++;
            }

            name = new String(array, nameOffset, end - nameOffset);
        }
    }
}
