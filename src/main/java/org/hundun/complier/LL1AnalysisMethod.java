package org.hundun.complier;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.util.*;
import java.util.function.Supplier;

/**
 * LL(1)驱动表分析算法
 */
public class LL1AnalysisMethod {
    //公式与非终结符的关系
    static Map<String, String> correlation = new LinkedHashMap<>();
    //推导公式
    static Map<String, String> P = new LinkedHashMap<>();
    //非中终结符
    static Set<String> N = new LinkedHashSet<>();
    //终结符
    static Set<String> E = new LinkedHashSet<>();
    //空表
    static Set<String> nullable = new LinkedHashSet<>();
    //非终结符的first集
    public static Map<String, Set<String>> first = new LinkedHashMap<>();
    //跟随集 每个非终结符后面都能跟那些符号
    public static Map<String, Set<String>> follow = new LinkedHashMap<>();
    //每个推导公式的first集
    public static Map<String, Set<String>> first_s = new LinkedHashMap<>();


    public static void nullable() {
        boolean change = false;
        do {
            change = false;
            i:
            for (Map.Entry<String, String> entry : P.entrySet()) {
                if (entry.getValue().equals("ε")) {
                    //若当前符号允许空则将其加入空列表中
                    change = nullable.add(correlation.get(entry.getKey())) || change;
                } else if (entry.getValue().length() > 1 || (entry.getValue().charAt(0) >= 'A' && entry.getValue().charAt(0) <= 'Z')) {
                    //若是非终结符则判断当前式子中所有非终结符是否都允许为空若是则当前非终结符也加入到集合中
                    for (char c : entry.getValue().toCharArray()) {
                        if (c >= 'A' && c <= 'Z') {
                            if (!nullable.contains(c + "")) {
                                continue i;
                            }
                        } else {
                            //若存在一位终结符那么就不允许为null
                            continue i;
                        }
                    }
                    //若全部非终结符都允许为null那么便将其加入到null集合中
                    change = nullable.add(correlation.get(entry.getKey())) || change;
                }
            }

        } while (change);
    }

    public static void first() {
        boolean change = false;
        for (String entry : N) {
            //初始化非终结符的集合
            first.put(entry, new LinkedHashSet<>());
        }
        do {
            change = false;
            i:
            for (Map.Entry<String, String> entry : P.entrySet()) {
                //获取当前推导式子的非终结符集合
                Set<String> strings = first.get(correlation.get(entry.getKey()));
                //遍历当前式子中的符号
                for (char c : entry.getValue().toCharArray()) {
                    //若当前符号为非终结符
                    if (c >= 'A' && c <= 'Z') {
                        //那么将其加入到集合中
                        change = strings.addAll(first.get(c + "")) || change;
                        //若当前非终结符允许为null则需要继续计算集合内容，因为first获取的是式子中的第一组符合内容（有哪些符号可以作为式子开头）
                        //若非终结符允许为空那么他的下一个符号便可以作为式子开头所以需要继续计算
                        if (nullable.contains(c + "")) {
                            continue;
                        }
                        //否则进行下一个式子的推导
                        continue i;
                    } else {
                        if (c == 'ε')
                            continue;
                        //将终结符加入到集合中
                        change = strings.add(c + "") || change;
                        continue i;
                    }
                }
            }
        } while (change);
    }

    public static void follow(String start) {
        boolean change = false;
        //初始化
        for (String entry : N) {
            follow.put(entry, new LinkedHashSet<>());
            if (start.equals(entry)) {
                //若是start则设置它的跟随为结束符$
                follow.get(entry).add("$");
            }
        }
        do {
            change = false;
            //遍历所有式子
            for (Map.Entry<String, String> entry : P.entrySet()) {
                char[] chars = entry.getValue().toCharArray();
                //若只推导一个元素则没有follow集
                if (chars.length == 1) continue;
                Set<String> temp = new LinkedHashSet<>();
                //倒叙计算因为最后一个字符的follow因当是$
                for (int i = chars.length - 1; i >= 0; i--) {
                    //若是终结符
                    if (!(chars[i] >= 'A' && chars[i] <= 'Z')) {
                        //终结符不能传递所以需要再次清除
                        temp.clear();
                        //则直接加入到set中
                        temp.add(chars[i] + "");
                        //若是非终结符
                    } else if (chars[i] >= 'A' && chars[i] <= 'Z') {
                        //则将已有的集合加入到此非终结符的集合中
                        change = follow.get(chars[i] + "").addAll(temp) || change;
                        //若当前终结符允许为null则需要将此temp集合的内容继续传递给前一个符号，否则清空集合不在传递
                        if (!nullable.contains(chars[i] + "")) {
                            temp.clear();
                        }
                        //而当前非终结符的first便是前一个符号的follow
                        temp.addAll(first.get(chars[i] + ""));
                    }

                }
            }
        } while (change);
    }

    private static void first_s() {
        //推导式子的所有字符串集合
        for (Map.Entry<String, String> entry : P.entrySet()) {
            first_s.put(entry.getKey(), new LinkedHashSet<>());
        }
        boolean change;
        do {
            change = false;
            i:
            for (Map.Entry<String, String> entry : P.entrySet()) {
                char[] chars = entry.getValue().toCharArray();
                for (char c : chars) {
                    //若当前是终结符则直接加入到当前非终结符的集合中
                    if (!(c <= 'Z' && c >= 'A')) {
                        change = first_s.get(entry.getKey()).add(c + "") || change;
                        continue i;
                    } else if (c <= 'Z' && c >= 'A') {
                        //否则将非终结符的first集加入到字符串集中，代表当前式子允许开头的符号有哪些
                        change = first_s.get(entry.getKey()).addAll(first.get(c + "")) || change;
                        //若当前非终结符不存在null中则代表计算完成否则需要计算下一个符号为null代表当前符号可以不存在所以需要计算下一个
                        if (!nullable.contains(c + "")) {
                            continue i;
                        }
                    }
                }
                //若所有符号都计算完毕，那么就需要获取当前推导式子所对应的非终结符的follow集加入
                change = first_s.get(entry.getKey()).addAll(follow.get(correlation.get(entry.getKey()))) || change;
            }
        } while (change);

    }

    static int[][][] ints = new int[1024][1024][1];

    public static void init(String[] data) {


        for (String datum : data) {
            String[] split = datum.split(">");
            String[] split1 = split[1].split("\\|");
            for (int i = 1; i <= split1.length; i++) {
                P.put(split[0] + i, split1[i - 1]);
                correlation.put(split[0] + i, split[0]);
                for (char c : split1[i - 1].toCharArray()) {
                    if (!(c >= 'A' && c <= 'Z'))
                        E.add(c + "");
                }
            }
            N.add(split[0]);
        }
        nullable();
        first();
        follow("S");
        first_s();
        //初始化
        for (int i = 0; i < ints.length; i++) {
            for (int i1 = 0; i1 < ints[i].length; i1++) {
                Arrays.fill(ints[i][i1], -1);
            }
        }
        int i = 0;
        //构建分析表 通过first_s的内容构建
        for (Map.Entry<String, Set<String>> entry : first_s.entrySet()) {
            String e = correlation.get(entry.getKey());
            d:
            for (String n : entry.getValue()) {
                for (int i1 = 0; i1 < ints[e.charAt(0)][n.charAt(0)].length; i1++) {
                    if (ints[e.charAt(0)][n.charAt(0)][i1] == -1) {
                        ints[e.charAt(0)][n.charAt(0)][i1] = i;
                        continue d;
                    }
                }
            }
            i++;
        }
    }

    private static boolean process(String[] tokens) {
        //文法
//        String[] data = {
//                "Z>d|XYZ",
//                "Y>c|ε",
//                "X>Y|a",
//        };
        //        String[] data = {
//                "E>E+T|T",
//                "T>T*F|F",
//                "F>n",
//        };
        String[] data = {
                "E>TR",
                "R>+TR|ε",
                "T>FY",
                "Y>*FY|ε",
                "F>n",
                "D>RTm"
        };
        init(data);
        System.out.println(first_s);

        int i = 0;
        Stack<String> stack = new Stack<>();
        //计算索引 当i==tokens.length时代表匹配成功
        i = 0;
        //先压入起始节点
        stack.push("E");
        while (!stack.empty() && i < tokens.length) {
            //若栈顶数据为String 则代表要与token进行匹配
            if (E.contains(stack.peek())) {
                //匹配token
                if (stack.peek().equals(tokens[i++])) {
                    //若成功则弹出匹配
                    stack.pop();
                } else {
                    //此处将不在是回溯而是直接抛出异常
//                    throw new RuntimeException("解析错误");
                    return false;
                }
                //代表字符已配结束，解析Object[]
            } else if (N.contains(stack.peek())) {
                String token = tokens[i];
                //获取栈顶 并且倒叙插入到stack中进行上方的字符匹配
                String pop = stack.pop();
                if (ints[pop.charAt(0)][token.charAt(0)][0] == -1) {
                    //只有允许为null的才可以跳过否则必须匹配
                    if (nullable.contains(pop.charAt(0) + ""))
                        continue;
//                    throw new RuntimeException("解析错误");
                    return false;
                }
                //由之前的每个式子中元素匹配改为了通过表查询出指定的式子匹配
                String set = (String) P.values().toArray()[ints[pop.charAt(0)][token.charAt(0)][0]];
                for (int i1 = set.toCharArray().length - 1; i1 >= 0; i1--) {
                    stack.push(set.charAt(i1) + "");
                }
            }
        }
        //方法补偿 若匹配式子成功 但是栈中还存在数据那么就代表式子未匹配完成，故而检查剩余匹配内容是否允许为空若是则跳过否则匹配失败
        String token;
        while (!stack.isEmpty() && (token = stack.pop()) != null) {
            if (!nullable.contains(token)) {
//                    throw new RuntimeException("解析错误");
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        String tokens[] = {"n", "+", "n"};
        System.out.println(process(tokens));

    }
}
