package org.hundun.elf;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class Main {

    public static void main(String[] args) throws IOException {
        Elf elf = new Elf(new File(Main.class.getResource("/share_file.so").getFile()));
        for (SectionHeader header : elf.sectionHeaders) {
            if (header.type == SectionType.SYMTAB) {
                System.out.println();
            }
        }
    }
}
