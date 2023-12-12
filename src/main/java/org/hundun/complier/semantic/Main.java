package org.hundun.complier.semantic;

import org.hundun.complier.lr0.LR0Parser;
import org.hundun.complier.lr0.StackItem;
import org.hundun.complier.lr1.lr1.LR1Parser;
import org.hundun.complier.lr1.util.Grammar;
import org.hundun.complier.semantic.entity.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
//        Grammar grammar = new Grammar("S->E $\n" +
//                "E->L = R\n" +
//                "E->R\n" +
//                "L->* R\n" +
//                "L->id\n" +
//                "R->L");
        Grammar grammar = new Grammar("S-> D  | L | IF | IFELSE | S  \n" +
                //变量赋值
                "L->ID = N ; | ID = ID ;\n" +
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
                //IFELSE
                "IFELSE-> IF ELSE \n" +
                //常量定义
                "N->1 |2 |3 |4 |5  ");
        List<List<String>> tokens = new ArrayList<>();
        tokens.add(new ArrayList<>(Arrays.asList("int", "a", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("char", "b", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("a", "=", "5", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("b", "=", "5", ";")));
        tokens.add(new ArrayList<>(Arrays.asList("b", ">", "a")));
        tokens.add(new ArrayList<>(Arrays.asList("if", "(", "b", ">", "a", ")",
                "{", "a", "=", "b", ";","}",
                "else",
                "{", "a", "=", "1", ";", "}")));
        LR1Parser parser = new LR1Parser(grammar);
        parser.parseCLR1();
        StringBuilder sb = new StringBuilder();
        for (List<String> token : tokens) {
            Entry entry = parser.accept3(token);
            if (entry == null) {
                System.out.println("语句不符合规范：" + token.stream().collect(Collectors.joining()));
                continue;
            }
            sb.append(genCode(entry.value, entry));
        }
        System.out.println(sb.toString());
    }

    static AtomicInteger index = new AtomicInteger(0);


    private static String genCode(InstrBase base, Entry entry) {
        StringBuilder sb = new StringBuilder();
        switch (base.getKind()) {
            case Declaration:
                DeclarationInstr declarationInstr = (DeclarationInstr) base;
                sb.append(declarationInstr.getType().getType());
                sb.append(" ");
                sb.append(declarationInstr.getId().getName());
                sb.append(";\n");
                break;
            case If:
                IfInstr ifInstr = (IfInstr) base;
                sb.append(genOp(ifInstr.getCondition()));
                sb.append("Label_");
                sb.append(index.get());
                sb.append(":\n");
                for (InstrBase instrBase : ifInstr.getInstr()) {
                    sb.append(genCode(instrBase, entry));
                }
                break;
            case Else:
                ElseInstr elseInstr = (ElseInstr) base;
                sb.append("Label_");
                sb.append(index.get());
                sb.append(":\n");
                sb.append(genCode(elseInstr.getAssign(), entry));
                break;
            case IF_Else:
                int i = index.getAndIncrement();
                sb.append(genCode(entry.left.value, entry.left));
                sb.append("jump Label_");
                sb.append(i);
                sb.append(";\n");
                index.getAndIncrement();
                sb.append(genCode(entry.right.value, entry.right));
                sb.append("Label_");
                sb.append(i);
                sb.append(":\n");
                break;
            case Assign:
                AssignInstr assignInstr = (AssignInstr) base;
                sb.append("mov ");
                sb.append(assignInstr.getIdInstr().getName());
                sb.append(",");
                Object data = null;
                if (assignInstr.getInstr() instanceof NumberInstr){
                    data = ((NumberInstr) assignInstr.getInstr()).getNumber();
                } else {
                    data = ((IdInstr) assignInstr.getInstr()).getName();
                }
                sb.append(data);
                sb.append(";\n");
                break;
        }
        return sb.toString();
    }

    private static String genOp(ConditionInstr instr) {
        StringBuilder sb = new StringBuilder();
        sb.append("sub ");
        sb.append(instr.getOp1().getName());
        sb.append(",");
        sb.append(instr.getOp1().getName());
        sb.append(",");
        sb.append(instr.getOp2().getName());
        sb.append(";\n");
        switch (instr.getOperate()) {
            case ">":
                sb.append("jg ");
                sb.append(instr.getOp1().getName());
                sb.append(",");
                sb.append("Label_");
                sb.append(index.get()+1);
                sb.append(";\n");
                return sb.toString();
            case ">=":
                sb.append("jge ");
                sb.append(instr.getOp1().getName());
                sb.append(",");
                sb.append("Label_");
                sb.append(index.get());
                sb.append(";\n");
                return sb.toString();
            default:
                throw new RuntimeException("错误符号");
        }
    }
}
