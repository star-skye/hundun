package org.hundun.complier;

import org.hundun.complier.lr1.lr1.LR1Parser;
import org.hundun.complier.lr1.util.Grammar;
import org.hundun.complier.semantic.entity.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 控制流图
 */
public class ControlFlowDiagram {

    static AtomicInteger index = new AtomicInteger();

    /**
     * 这两个构建方法非常的简单，只为讲清楚其原理，而真实场景需要通过不动点算法去构建控制流图
     * 不动点算法：以某个集合作为条件 遍历直到他不在变动为止 前文中大量使用 LR0中 while (!queue.isEmpty()) 便是不动点算法的一种
     */
    /**
     * 抽象语法树构建
     */
    public static void genGraph() {
        Grammar grammar = new Grammar("S-> D  | L | IF | IFELSE | S  \n" +
                //变量赋值
                "L->ID = N ;\n" +
                //变量声明
                "D->T ID ;\n" +
                //变量名
                "ID->a | b  \n" +
                //变量类型
                "T->int | char \n" +
                //允许操作式子
                "E->E + E  | N  | ID > ID \n" +
                //IF判断
                "IF->if ( E )  { L } \n" +
                //ELSE
                "ELSE-> else { L } \n" +

                "IFELSE-> IF ELSE \n" +
                //常量定义
                "N->1 |2 |3 |4 |5  ");
        List<List<String>> tokens = new ArrayList<>();
        tokens.add(new ArrayList<>(Arrays.asList("int", "a", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("char", "b", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("a", "=", "5", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("b", "=", "5", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("if", "(", "b", ">", "a", ")",
                "{", "a", "=", "1", ";", "}",
                "else",
                "{", "a", "=", "1", ";", "}")));
        tokens.add(new ArrayList<>(Arrays.asList("b", "=", "2", ";")));
        LR1Parser parser = new LR1Parser(grammar);
        parser.parseCLR1();
        List<Entry> entries = new ArrayList<>();
        for (List<String> token : tokens) {
            Entry entry = parser.accept3(token);
            if (entry == null) {
                System.out.println("语句不符合规范：" + token.stream().collect(Collectors.joining()));
                continue;
            }

            entries.add(entry);
        }

        List<Block> all = new ArrayList<>();
        Block block = new Block();
        block.name = "Label_" + index.getAndIncrement();
        Block forms = block;
        for (Entry entry : entries) {
            block.codes.add(entry);
            if (entry.value.getKind() == InstrKind.IF_Else) {
                all.add(block);
                block = new Block();
                block.name = "if";
                block.codes.add(entry.left);
                block.forms.add(forms);
                forms.tos.add(block);
                Block if_ = block;
                all.add(block);

                block = new Block();
                block.name = "else";
                block.codes.add(entry.right);
                block.forms.add(forms);
                forms.tos.add(block);
                Block else_ = block;
                all.add(block);

                block = new Block();
                block.forms.add(if_);
                block.forms.add(else_);
                if_.tos.add(block);
                else_.tos.add(block);
            }

        }
        if (block.name == null) {
            block.name = "Label_" + index.getAndIncrement();
        }
        all.add(block);

    }

    /**
     * 三地值码构建
     */
    public static void genGraph2() {
        String code_ = "label_start:\n" +
                "int a;\n" +
                "int b;\n" +
                "mov a,1;\n" +
                "mov b,2;\n" +
                "sub b,b,a;\n" +
                "jg b,Label_2;\n" +

                "Label_1:\n" +
                "jump label;\n" +

                "mov a,3;\n" +
                "jump Label_0;\n" +

                "Label_2:\n" +
                "mov a,4;\n" +

                "Label_0:\n" +
                "mov b,5;\n" +

                "label:\n" +
                "mov b,6;";
        List<String> codes = new ArrayList<>(Arrays.asList(code_.split("\n")));
        List<Block> blocks = new ArrayList<>();
        Block block = new Block();
        blocks.add(block);
        for (String code : codes) {
            if (code.contains(":")) {
                if (block.name != null) {
                    block = new Block();
                    blocks.add(block);
                }
                block.name = code.replace(":", "");

            } else if (code.contains("jg") || code.contains("jump")) {
                block.jump = code;
                block = new Block();
                blocks.add(block);
            } else {
                block.codes2.add(code);
            }
        }
        System.out.println();
    }


    public static void main(String[] args) {
        genGraph2();
    }

    public static class Block {
        //共用
        String name;
        //三地值码使用
        String jump;
        //抽象语法树使用
        List<Entry> codes = new ArrayList<>();
        //三地值码使用
        List<String> codes2 = new ArrayList<>();
        //抽象语法树使用
        List<Block> tos = new ArrayList<>();
        //抽象语法树使用
        List<Block> forms = new ArrayList<>();

        public Block() {
        }

        public Block(String name) {
            this.name = name;
        }
    }


}
