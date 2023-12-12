package org.hundun.complier;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定义可达性
 */
public class ReachingDefinition {
    static class Definition {
        int index;
        String code;
        String name;
        String value;
        Collection<String> uses = new ArrayList<>();
        Collection<Definition> ins;
        Collection<Definition> outs;

        public Definition(int index, String code) {
            this.index = index;
            this.code = code;
        }

    }

    public static class Block {
        //三地值码使用
        Integer jump;
        //三地值码使用
        List<Integer> codes = new ArrayList<>();
        //block 级别的in out
        Collection<Definition> ins = new LinkedHashSet<Definition>();
        Collection<Definition> outs = new LinkedHashSet<Definition>();

    }

    //jump 1; 1不再是label 而是代码编号
    public static List<Block> reachingDefinitionGraph(String code_) {
        List<String> codes = new ArrayList<>(Arrays.asList(code_.split("\n")));
        List<Block> blocks = new ArrayList<>();
        Map<Integer, Block> map = new HashMap<>();
        Block block = new Block();
        blocks.add(block);
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            if (code.contains("je")) {
                Matcher matcher = Pattern.compile("je (.),(.);").matcher(code);
                matcher.find();
                Integer jump = Integer.parseInt(matcher.group(2));
                //代表往前跳 分割block
                if (jump < i) {
                    Block block1 = map.get(jump);
                    List<Integer> split = new ArrayList<>();
                    for (Integer integer : block1.codes) {
                        if (integer >= jump) {
                            split.add(integer);
                        }
                    }
                    block1.codes.removeAll(split);
                    Block newB = new Block();
                    newB.jump = blocks.indexOf(block1) + 1;
                    newB.codes.addAll(split);
                    newB.codes.add(i);
                    blocks.add(newB);
                }
                block = new Block();
                blocks.add(block);

            } else {
                block.codes.add(i);
                map.put(i, block);
            }
        }

        return blocks;
    }

    /**
     * 定义可达性线性
     */
    public static void reachingDefinition() {
        String code = "mov a,1;\n" +
                "mov b,1;\n" +
                "mov a,2;\n" +
                "mov a,3;\n" +
                "mov b,3;\n";
        Collection<Definition> set = new ArrayList<>();
        List<Definition> all = new ArrayList<>();
        String[] split = code.split("\n");
        for (int i = 0; i < split.length; i++) {
            Definition definition = new Definition(i, split[i]);
            definition.ins = set;
            definition.outs = killer(set, definition);
            set = definition.outs;
            all.add(definition);
        }
    }

    public static void reachingDefinitionBlock() {
        String code = "mov a,1;\n" +
                "mov b,1;\n" +
                "mov a,2;\n" +
                "mov a,b;\n" +
                "je a,1;\n" +
                "mov b,3;\n";
        String[] split = code.split("\n");
        Collection<Definition> set = new ArrayList<>();
        List<Definition> all = new ArrayList<>();
        List<Block> blocks = reachingDefinitionGraph(code);
        for (int i = 0; i < split.length; i++) {
            Definition definition = new Definition(i, split[i]);
            definition.ins = set;
            definition.outs = killer(set, definition);
            set = definition.outs;
            all.add(definition);
        }
        set.clear();
        for (Block block : blocks) {
            block.ins.addAll(all.get(block.codes.get(0)).ins);
            block.outs.addAll(all.get(block.codes.get(block.codes.size() - 1)).outs);
            if (block.jump != null) {
                Block block1 = blocks.get(block.jump);
                block1.ins.addAll(block.outs);
            }
        }
        for (Definition def : all) {
            for (String use : def.uses) {
                int uses = 0;
                Definition temp = null;
                for (Definition in : def.ins) {
                    if (in.name.equals(use)) {
                        uses++;
                        temp = in;
                    }
                }
                if (temp != null && uses == 1) {
                    try {
                        Integer.parseInt(temp.value);
                        def.code = def.code.replace(temp.name, temp.value);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
        reachingDefinitionBlock();
        System.out.println();
    }

    static Pattern pattern = Pattern.compile("mov (.),(.);");

    public static Collection<Definition> killer(Collection<Definition> ins, Definition code) {
        Matcher matcher = pattern.matcher(code.code);
        if (!matcher.find()) {
            return ins;
        }
        Matcher matcher2 = matcher;
        String name = matcher.group(1);
        code.name = name;
        code.value = matcher2.group(2);
        List<Definition> out = new ArrayList<>();
        for (Definition in : ins) {
            matcher = pattern.matcher(in.code);
            if (matcher.find()) {
                if (matcher.group(1).equals(name)) {
                    continue;
                }
            }
            out.add(in);
        }
        out.add(code);
        try {
            Integer.parseInt(code.value);
        } catch (NumberFormatException e) {
            code.uses.add(code.value);
        }
        return out;
    }
}
