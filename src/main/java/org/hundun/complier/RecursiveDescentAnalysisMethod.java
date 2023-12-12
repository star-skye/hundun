package org.hundun.complier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 递归下降分析算法
 */
public class RecursiveDescentAnalysisMethod {
    static List<String> datas = new ArrayList<>();

    private static void processF(String[] tokens, AtomicInteger index) {
        datas.add("F");
        System.out.println(Integer.parseInt(tokens[index.getAndIncrement()]));
        datas.add(tokens[index.get() - 1]);
    }

    private static void processM(String[] tokens, AtomicInteger index) {
        datas.add("M");
        processF(tokens, index);
        if (index.get() == tokens.length) {
            return;
        }
        String token = tokens[index.get()];
        while (token.equals("*")) {
            datas.add("*");
            index.getAndIncrement();
            processF(tokens, index);
            if (index.get() == tokens.length) {
                return;
            }
            token = tokens[index.get()];
        }
    }

    private static void processE(String[] tokens, AtomicInteger index) {
        datas.add("E");
        processM(tokens, index);
        String token = tokens[index.get()];
        while (token.equals("+")) {
            datas.add("+");
            index.getAndIncrement();
            processM(tokens, index);
            if (index.get() == tokens.length) {
                return;
            }
            token = tokens[index.get()];
        }
    }

    public static boolean process(String[] tokens) {
        AtomicInteger index = new AtomicInteger(0);
        try {
            processE(tokens, index);
        } catch (Exception e) {
            return false;
        }
        return index.get() == tokens.length;
    }

    public static void main(String[] args) {
        String[] tokens = {"5", "+", "6", "*", "7" };
        System.out.println(process(tokens));
    }
}
