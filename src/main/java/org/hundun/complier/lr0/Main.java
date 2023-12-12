package org.hundun.complier.lr0;

import org.hundun.complier.lr1.util.Grammar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * https://github.com/amirhossein-hkh/LR-Parser
 * LR0的另一种实现
 * 使用此方法来实现语法制导翻译
 */
public class Main {
    public static void main(String[] args) {
//        Grammar grammar = new Grammar("S->E $\n" +
//                "E->L = R\n" +
//                "E->R\n" +
//                "L->* R\n" +
//                "L->id\n" +
//                "R->L");
        Grammar grammar = new Grammar("S->E $ {$$=$2}\n" +
                "E->E + E {$$=$1+$3} | N {$$=$1}\n" +
                "N->1 {$$=1}|2 {$$=2}|3 {$$=3}|4 {$$=4}|5 {$$=5}");
        LR0Parser parser = new LR0Parser(grammar);
        parser.parserLR0();
        System.out.println(parser.accept2(new ArrayList<>(Arrays.asList("2", "+", "2","+", "5", "$"))));
    }
}
