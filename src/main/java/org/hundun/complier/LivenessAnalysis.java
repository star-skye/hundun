package org.hundun.complier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 活性分析
 */
public class LivenessAnalysis {
    static class Definition {
        int index;
        String code;
        String def;
        List<String> uses = new ArrayList<>();
        Collection<String> ins;


        public Definition(int index, String code) {
            this.index = index;
            this.code = code;
        }

    }

    static class Node {
        String name;
        //创建节点的行
        int create_line;
        int end_line = -1;
        List<Node> lives = new ArrayList<>();

        public Node(String name) {
            this.name = name;
        }
    }

    static List<Node> graph = new ArrayList<>();

    /**
     * 倒序遍历 一定是先使用在定义 所以此处在use定义所存活的对象
     * @param index
     * @param definition
     */
    public static void addGraph(Integer index, Definition definition) {
        //遇到定义时 则此node可以结束
        if (definition.def != null){
            Node node = graph.stream().filter(item -> item.name.equals(definition.def) && item.end_line == -1).findFirst().orElse(null);
            if (node != null){
                //第一个定义作为end 由此构建区间
                node.end_line = index;
                node.lives.addAll(graph.stream().filter(item ->!item.name.equals(node.name) && item.end_line < node.create_line).collect(Collectors.toList()));
            }
        }
        List<String> uses = definition.uses;
        Map<String, Node> map = graph.stream().filter(item -> uses.contains(item.name)).collect(Collectors.toMap(item -> item.name, item -> item));
        for (String use : uses) {
            Node node = map.get(use);
            if (node == null || node.end_line != -1) {
                //第一次使用作为create
                Node node1 = new Node(use);
                node1.create_line = index;
                graph.add(node1);
            }
        }

    }

    public static void main(String[] args) {
        livenessAnalysisDefinition();
        System.out.println();
    }

    static Pattern pattern_add = Pattern.compile("add.*?(.),(.),(.);");
    static Pattern pattern_mov = Pattern.compile("mov.*?(.),(.);");
    static Pattern pattern_ret = Pattern.compile("ret.*?(.);");

    public static void livenessAnalysisDefinition() {
        String code =   "mov a,1;\n" +
                        "mov b,2;\n" +
                        "add c,a,b;\n" +
                        "mov b,2;\n" +
                        "ret c;";

        String[] split = code.split("\n");
        Collection<String> outs = new ArrayList<>();
        for (int i = split.length - 1; i >= 0; i--) {
            Definition definition = new Definition(i, split[i]);
            if (split[i].contains("ret")) {
                Matcher matcher = pattern_ret.matcher(split[i]);
                if (!matcher.find()) {
                    throw new RuntimeException("add 语法错误");
                }
                //简单处理
                definition.uses.add(matcher.group(1));
                //因为ret是最后语句所以 ins直接等于uses;
                definition.ins = definition.uses;
                outs = new ArrayList<>(definition.ins);
                addGraph(i, definition);
            } else if (split[i].contains("add")) {
                Matcher matcher = pattern_add.matcher(split[i]);
                if (!matcher.find()) {
                    throw new RuntimeException("add 语法错误");
                }
                definition.uses.add(matcher.group(2));
                definition.uses.add(matcher.group(3));
                definition.def = matcher.group(1);
                outs.remove(definition.def);
                outs.addAll(definition.uses);
                definition.ins = new ArrayList<>(outs);
                addGraph(i, definition);
            } else if (split[i].contains("mov")) {
                Matcher matcher = pattern_mov.matcher(split[i]);
                if (!matcher.find()) {
                    throw new RuntimeException("mov 语法错误");
                }
                definition.def = matcher.group(1);
                try {
                    Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) {
                    definition.uses.add(matcher.group(2));
                }
                outs.remove(definition.def);
                outs.addAll(definition.uses);
                definition.ins = new ArrayList<>(outs);
                addGraph(i, definition);
            }
        }
        System.out.println();
    }

}
