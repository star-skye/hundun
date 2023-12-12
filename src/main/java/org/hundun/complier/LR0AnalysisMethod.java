package org.hundun.complier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * LR0分析算法
 */
public class LR0AnalysisMethod {
    //非终结符对应公式表
    static Map<String, List<Character[]>> F = new LinkedHashMap<>();
    //非中终结符表
    static LinkedHashSet<String> N = new LinkedHashSet<>();
    //终结符表
    static Set<String> E = new LinkedHashSet<>();
    //添加点位
    static Character POINT = '·';

    //闭包计算传入项目集计算每个项目中点位后续的非终结符的闭包公式
    public static List<Character[]> closure(Collection<Character[]> queue) {
        List<Character[]> temp = new ArrayList<>(queue);
        List<Character[]> it = new ArrayList<>();
        int size;
        do {
            size = temp.size();
            it.clear();
            it.addAll(temp);
            for (Character[] characters : it) {
                int point = point(characters);
                //计算当前项目是否已经完成,点位的引入是为了通过移动点位来计算当前项目是否已被处理完成
                if (point == characters.length - 1) {
                    if (characters.length == 2 && N.contains(characters[point - 1] + "")) {
                        //通过遍历该非终结符得到他所有的闭包公式
                        for (Character[] characters1 : F.get(characters[point - 1] + "")) {
                            if (!temp.contains(characters1)) temp.add(characters1);
                        }

                    }
                    continue;
                }
                //判断点位的下一个符号是否为非终结符
                if (N.contains(characters[point + 1] + "")) {
                    //通过遍历该非终结符得到他所有的闭包公式
                    for (Character[] characters1 : F.get(characters[point + 1] + "")) {
                        if (!temp.contains(characters1)) temp.add(characters1);
                    }

                }
            }

        } while (size != temp.size());

        return temp;
    }

    //移动点位
    public static void movePoint(Character[] str, int point) {
        str[point] = str[point + 1];
        str[point + 1] = POINT;
    }

    //传入一个项目得到它移动结束的最终字符串
    public static String getLastPoint(Character[] str) {
        Character[] temp = new Character[str.length];
        int index = 0;
        for (Character character : str) {
            if (character != POINT) {
                temp[index++] = character;
            }
        }
        temp[index] = POINT;
        return Arrays.stream(temp).map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }

    static class Ref {
        //当前ref 的值
        public Character datas;
        //ref所引用的状态 GOTO/ACTION表的一维数组下标
        public int index;
        //当前值的前一个值内容 用于过滤筛选
        public Character before;
        //当前符号后续可以引用出那些符号
        public List<Character[]> refs;
        //当前符号所在的分支
        public Ref parent;
        //通过引用符号计算出的具体引用项,因为有些是循环或者在别处已然完成推算那么将不继续推算所以refList是refs的补集
        public List<Ref> refList = new ArrayList<>();

        public Ref(Character datas, Character before, List<Character[]> refs) {
            this.datas = datas;
            this.refs = refs;
            this.before = before;
        }

        public Ref(Character datas, Character before, List<Character[]> refs, int index) {
            this.datas = datas;
            this.refs = refs;
            this.before = before;
            this.index = index;
        }

        public Ref(Character datas, Character before, List<Character[]> refs, int index, Ref parent) {
            this.datas = datas;
            this.refs = refs;
            this.before = before;
            this.parent = parent;
            this.index = index;
        }

        public void add(Ref ref) {
            refList.add(ref);
        }


        @Override
        public String toString() {
            return "Ref{" +
                    "index=" + index +
                    ", before=" + before +
                    ", datas=" + datas +
                    ", refs=" + refs.stream().map(item -> Arrays.stream(item).map(String::valueOf).collect(Collectors.joining(",", "[", "]"))).collect(Collectors.toList()) +
                    '}';
        }
    }

    //根据ref定制的集合方便查询与去重
    static class LinkedHashSetD extends LinkedHashSet<LR0AnalysisMethod.Ref> {
        @Override
        public boolean add(Ref ref) {
            //防止重复添加
            if (contains(ref)) {
                return false;
            }
            return super.add(ref);
        }

        @Override
        public boolean contains(Object o) {
            Ref temp = (Ref) o;
            Iterator<Ref> iterator = iterator();
            //重复条件
            i:
            while (iterator.hasNext()) {
                Ref next = iterator.next();
                if (next.datas == temp.datas &&
                        next.before == temp.before &&
                        next.refs.size() == temp.refs.size()) {
                    for (int i = 0; i < next.refs.size(); i++) {
                        if (!Arrays.equals(next.refs.get(i), temp.refs.get(i))) {
                            continue i;
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        //查询是否存在循环引用
        public Ref search(Ref ref, Ref parent) {
            r:
            for (Ref ref1 : this) {
                if (ref1.refs.size() == ref.refs.size()) {
                    for (int i = 0; i < ref1.refs.size(); i++) {
                        if (!getLastPoint(ref1.refs.get(i)).equals(getLastPoint(ref.refs.get(i)))) {
                            continue r;
                        }
                    }
                    if (ref1.datas == ref.datas && parent == ref1.parent)
                        return ref1;
                }
            }
            return null;
        }
    }

    //非重复公式集合
    static LinkedHashSetD set = new LinkedHashSetD();

    //获取点位
    public static int point(Character[] str) {
        for (int i = 0; i < str.length; i++) {
            if (str[i].equals(POINT)) {
                return i;
            }
        }
        throw new RuntimeException("错误 point");
    }

    //分配GOTO/ACTION中一维下标
    static AtomicInteger index = new AtomicInteger(0);

    //记录对应reduce下标,在reduce的时候通过index找到对应的公式进行reduce
    static class MapIndex {
        public int index;
        public String data;

        public MapIndex(int index, String data) {
            this.index = index;
            this.data = data;
        }
    }

    public static void main(String[] args) {
        //文法 S 在使用时因当固定 而传入的文法将使用E开头
        //这样做是为了保证最终归约时将是固定的S而不是用户自定义的
        //此方法为拓广文法在传入文法的基础上抽出最顶层
        String[] data = {
//                "S>E",
//                "E>aA|bB",
//                "A>cA|d",
//                "B>cB|d",

//                "S>E$",
//                "E>xxT",
//                "T>y",

                "S>E$",
                "E>aAcBe",
                "A>b|Ab",
                "B>d",
        };
        Map<String, MapIndex> map = new LinkedHashMap<>();
        int mapIndex = 0;
        //分析文法
        for (String datum : data) {
            String[] split = datum.split(">");
            String[] split1 = split[1].split("\\|");
            for (int i = 1; i <= split1.length; i++) {
                Character[] chars = new Character[split1[i - 1].toCharArray().length + 1];
                chars[0] = POINT;
                for (int i1 = 0; i1 < split1[i - 1].toCharArray().length; i1++) {
                    chars[i1 + 1] = split1[i - 1].charAt(i1);
                }
                map.put(split1[i - 1], new MapIndex(mapIndex++, split[0]));
                F.computeIfAbsent(split[0], k -> new ArrayList<>()).add(chars);
                for (char c : split1[i - 1].toCharArray()) {
                    if (!(c >= 'A' && c <= 'Z'))
                        E.add(c + "");
                }
            }
            N.add(split[0]);
        }
        Set<String> x = new LinkedHashSet<>();
        x.addAll(N);
        x.addAll(E);
        Queue<Ref> queue = new ArrayDeque<>();
        List<Character[]> c0 = closure(F.get("S"));
        //构建ACTION/GOTO表
        int[][] ACTION = new int[255][255];
        int[][] GOTO = new int[255][255];
        //设置初始化
        Ref s1 = new Ref('S', null, c0, index.getAndIncrement());
        set.add(s1);
        queue.add(s1);
        while (!queue.isEmpty()) {
            Ref ref = queue.poll();
            List<Character[]> c = ref.refs;
            List<Character[]> D = new ArrayList<>();
            AtomicReference<Character> X = new AtomicReference<>(null);
            AtomicReference<Character> before = new AtomicReference<>(null);
            //遍历所有符号看当前ref是否可以到达
            for (String s : x) {
                //通过goto找到到达列表
                D = goto_(c, s.charAt(0), X, before);
                if (D.isEmpty()) {
                    continue;
                }
                //构建当前到达列表的引用
                Ref ref1 = new Ref(X.get(), before.get(), D, index.getAndIncrement(), ref);
                //若是结束则设置action为A 代表accept 接受
                if (s.charAt(0) == '$') {
                    ACTION[ref.index][s.charAt(0)] = 'A' << 8;
                    //N则是需要进行跳转归约
                } else if (N.contains(X.get() + "")) {
                    GOTO[ref.index][s.charAt(0)] = ref1.index;
                } else {
                    //其他符号则是递进
                    ACTION[ref.index][s.charAt(0)] = ref1.index;
                }
                //将当前构建的到达列表添加到父中,也就是refList让其关联
                ref.add(ref1);
                //查询当前ref1是否已存在相同功能的ref
                Ref search = set.search(ref1, ref.parent);
                if (search != null) {
                    //若存在则修改index
                    ref1.index = search.index;
                    //重置增加
                    index.decrementAndGet();
                }
                //若D为1非常可能是到了最后一个即需要归约
                if (D.size() == 1) {
                    Character[] characters = D.get(0);
                    //再次判断 代表处理已经结束
                    if (characters[characters.length - 1] == POINT) {
                        //通过列表获取到归约公式也即前面初始化的MapIndex
                        String str = Arrays.stream(characters).filter(y -> y != POINT).map(String::valueOf).collect(Collectors.joining(""));
                        MapIndex mapIndex1 = map.get(str);
                        //若为0前面说过S一定是第一个所以如果为0那么就是接受也即 $,而$在前面已经处理过
                        if (mapIndex1.index == 0) {
                            // 0 代表接收前面已经处理
                        } else {
                            //否则其他填充数据位Rx,x代表归约公式
                            Arrays.fill(ACTION[ref1.index], 'R' << 8 | mapIndex1.index);
                        }
                    }
                }
                //最后判断当前ref1是否在别处依然处理过 若未处理那么set将能加入否则将入失败
                if (search == null && set.add(ref1)) {
                    queue.add(ref1);
                }
            }
        }
        //打印引用关系,可以通过此信息构建出GOTO/ACTION表(以图的形式)
        AtomicInteger index = new AtomicInteger(0);
        print(s1, index);
        System.out.println(actionTableStr(ACTION));
        //根据GOTO/ACTION 实现文法分析
        //此为工作栈记录工作状态 以$/0开头
        Stack<Object> stack = new Stack<>();
        stack.push('$');
        stack.push(0);

        //此处为方便递进使用了栈 也可使用别的方式
        Stack<Character> tokens = new Stack<>();
        tokens.push('$');
//        tokens.push('y');
//        tokens.push('x');
//        tokens.push('x');
        tokens.push('e');
        tokens.push('d');
        tokens.push('c');
        tokens.push('d');
        tokens.push('b');
        tokens.push('a');

        while (true) {
            //读取当前状态
            Integer state = (Integer) stack.peek();
            //获取token数据
            Character token = tokens.peek();
            //查询action表
            int i = ACTION[state][token];
            //若当前是纯数字那么代表为递进 当然也可以在设置处加入S <<8 用于标记递进,此处省略了该操作
            if (i != 0 && (i & 0xFF00) == 0) {
                //递进则将递进状态与以处理字符加入工作栈
                stack.push(token);
                stack.push(i & 0xFF);
                //从token中移除 代表已处理
                tokens.pop();
            } else if (i != 0 && (i >> 8) == 'R') {
                //若action为R代表reduce那么通过编号得到对应的推导式
                Map.Entry<String, MapIndex> entry1 = map.entrySet().stream().filter((entry) -> entry.getValue().index == (i & 0xFF)).findFirst().get();
                //根据推导式的长度进行弹出工作栈
                int length = entry1.getKey().length();
                for (int j = 0; j < length; j++) {
                    //因为工作栈中保存了状态和字符所以需要弹出两次
                    stack.pop();
                    stack.pop();
                }
                //推导式所归约的非终结符一定是一个所以此处获取charAt(0)
                char c = entry1.getValue().data.charAt(0);
                //通过当前状态与归约字符查询出归约后的下一次跳转状态并且push到工作表中
                state = (Integer) stack.peek();
                stack.push(c);
                stack.push(GOTO[state][c]);
            } else if (i != 0 && (i >> 8) == 'A') {
                //最终将会是A 接受 但是在接受前还需要处理下
                StringBuffer sb = new StringBuffer();
                //获取工作栈中所有的字符信息
                while (!stack.isEmpty()) {
                    stack.pop();
                    sb.append(stack.pop());
                }
                //若此信息对应的推导式是S的那么代表接受
                if (map.get(sb.toString()).data.equals("S")) {
                    System.out.println("ACCEPT");
                    break;
                } else {
                    throw new RuntimeException("错误解析");
                }
            } else {
                throw new RuntimeException("错误解析");
            }

        }

    }

    public static String actionTableStr(int[][] ACTION) {
        String str = "Action Table : \n";
        HashSet<String> terminals = new HashSet<>(E);
        terminals.add("$");
        str += "                ";
        for (String terminal : terminals) {
            str += String.format("%-10s", terminal);
        }
        str += "\n";

        for (int i = 0; i < index.get(); i++) {
            for (int j = 0; j < (terminals.size() + 1) * 10 + 2; j++) {
                str += "-";
            }
            str += "\n";
            str += String.format("|%-10s|", i);
            for (String terminal : terminals) {
                int[] ints = ACTION[i];
                char flag = (char) (ints[terminal.charAt(0)] >> 8);
                flag = (flag > 0 ? flag : 'S');
                str += String.format("%10s", (ints[terminal.charAt(0)] == 0 ? "|" :( String.valueOf(flag) + (ints[terminal.charAt(0)]&0xff) + "|")));
            }
            str += "\n";
        }
        for (int j = 0; j < (terminals.size() + 1) * 10 + 2; j++) {
            str += "-";
        }
        return str;
    }

    private static void print(Ref ref, AtomicInteger index) {
        int increment = index.get();
        for (int i = 0; i < increment; i++) {
            System.out.print("  ");
        }
        increment = index.incrementAndGet();
        System.out.println(ref.toString());
        for (Ref ref1 : ref.refList) {
            print(ref1, index);
            index.set(increment);
        }
    }

    //获取当前项目集所能跳转的项目集
    private static List<Character[]> goto_(List<Character[]> c, Character x, AtomicReference<Character> X, AtomicReference<Character> before) {
        Collection<Character[]> characters = new ArrayList<>();
        //遍历传入项目集
        for (Character[] characters1 : c) {
            //若点位已完成则跳过
            int point = point(characters1);
            if (point == characters1.length - 1) {
                continue;
            }
            //否则判断点位下一个字符是否为传入的字符 代表推导 在goto外是所有符号的递归,让其进行推导计算
            if (characters1[point + 1] == x) {
                //若得到则设置X为推导值
                X.set(x);
                //若不为0则代表有前字符则设置为before
                if (point != 0)
                    before.set(characters1[point - 1]);
                //构建一个新的项目
                Character[] temp = new Character[characters1.length];
                for (int i = 0; i < characters1.length; i++) {
                    temp[i] = characters1[i];
                }
                //让改项目移动 为了打印是查看对应的推导动作,所以此处使用了新的temp若正式使用可以直接移动
                movePoint(temp, point);
                //加入到项目集中
                characters.add(temp);
            }

        }
        //计算项目集的闭包
        return closure(characters);
    }
}
