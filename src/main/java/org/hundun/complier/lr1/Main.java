package org.hundun.complier.lr1;

import org.hundun.complier.lr1.lr1.LR1Parser;
import org.hundun.complier.lr1.util.Grammar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * https://github.com/amirhossein-hkh/LR-Parser
 * LR1 修改了SLR的结构不便在其基础上在进行修改,故而使用上方链接使用开源源码 进行注释讲解
 */
public class Main {
    public static void main(String[] args) {
        Grammar grammar = new Grammar("S->E $\n"+
                "E->a A c B e\n"+
                "A->b | A b\n"+
                "B->d");
        //Grammar 中只构建了规则 first_s集 follow集 将其放入parser继续构建
        LR1Parser parser = new LR1Parser(grammar);
        //解析grammar 并且构建 action与goto表
        parser.parseLALR1();
        System.out.println(parser.goToTableStr());
        System.out.println(parser.actionTableStr());
        System.out.println(parser.accept(new ArrayList<>(Arrays.asList("a", "b", "b", "c", "d", "e","$"))));
    }
}
