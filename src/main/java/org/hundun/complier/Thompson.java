package org.hundun.complier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Thompson {
    static AtomicInteger masterIndex = new AtomicInteger(0);
    static List<Trans> transAll = new ArrayList<>();

    public static class Trans {

        public int index, state_to, state_from;
        public char trans_symbol;

        public Trans() {
            index = -2;
            stack.push(this);
        }

        public Trans(int to, int from, char sym) {
            this.index = masterIndex.getAndIncrement();
            this.state_to = to;
            this.state_from = from;
            this.trans_symbol = sym;
            transAll.add(this);
            if ('ε' == sym) {
                stack.push(this);
            }
        }

        public Trans(int to, int from, char sym, int index) {
            this.index = index;
            this.state_to = to;
            this.state_from = from;
            this.trans_symbol = sym;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public String toString() {
            return String.format("{index:%d,from:%d,to:%d,sym:%c}", index, state_from, state_to, trans_symbol);
        }
    }

    public static class TransNode extends Trans {
        public List<TransNode> childes;

        public TransNode() {
        }

        public TransNode(int to, int from, char sym) {
            super(to, from, sym);
        }

        public TransNode(Trans trans) {
            super(trans.state_to, trans.state_from, trans.trans_symbol, trans.index);
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    public static Stack<Trans> stack = new Stack<>();
    //括号使用
    public static Stack<Stack<Trans>> lpStack = new Stack<>();

    public static Trans find(int index) {
        return transAll.stream().filter(item -> item.index == index).findFirst().get();
    }

    public static boolean alpha(char c) {
        return c >= 'a' && c <= 'z';
    }

    public static boolean regexOperator(char c) {
        return c == '(' || c == ')' || c == '*' || c == '|';
    }

    public static Trans current() {
        if (lpStack.empty()) {
            //若当前不在合集中 则使用最后一个sym为字符的trans
            return find(masterIndex.get() - 1);
        }
        Stack<Trans> trans = lpStack.peek();
        Trans trans1 = find(trans.get(trans.size() - 2).state_to + 2);
        return trans1;
    }

    public static int process(char c, int index, char[] origin, List<Trans> result) {
        if (alpha(c)) {
            index++;
            result.add(new Trans(masterIndex.get() + 1, masterIndex.get() - 1, c));
            result.add(new Trans(masterIndex.get() + 1, masterIndex.get() - 1, 'ε'));
        } else if (c == '|') {
            index++;
            c = origin[index];
            //前一个已经不是有效位，因为|和前一个是并集则需要跳过他
            stack.pop();
            //因为(压入一个空的可能会导致获取错误所以此处处理
            Trans prev = stack.peek();
            if (prev.index == -2) {
                prev = stack.get(stack.size() - 2);
            }
            Trans current = current();
            Trans start = new Trans(masterIndex.get() + 1, prev.state_from, 'ε');
            result.add(start);
            List<Trans> sub = new ArrayList<>();
            index = process(c, index, origin, sub);
            //删除最后一个闭包ε 将其合并到第一个闭包ε
            masterIndex.decrementAndGet();
            sub.remove(sub.size() - 1);
            Trans end = sub.get(sub.size() - 1);
            end.state_to = current.index;
            result.addAll(sub);
        } else if (c == '*') {
            stack.pop();
            Trans prev = stack.peek();
            Trans trans = find(prev.state_from);
            result.add(new Trans(prev.index, trans.index, 'ε'));
            index++;
        } else if (c == '(') {
            index++;
            c = origin[index];
            //填一个prev 因为当前*返回prev会pop 而(本身也是一个prev
            result.add(new Trans(masterIndex.get() + 1, masterIndex.get() - 1, 'ε'));
            //进入合集将使用新的栈而且不能影响到合集解析后的解析
            Stack<Trans> save = stack;
            Stack<Trans> newStack = new Stack<>();
            newStack.addAll(stack);
            stack = newStack;

            List<Trans> sub = new ArrayList<>();
            index = process(c, index, origin, sub);
            lpStack.push(save);
            result.addAll(sub);
        } else if (c == ')') {
            index++;
            //还原栈
            stack = lpStack.pop();
        }
        return index;
    }

    public static void main(String[] args) {
        List<Trans> result = new ArrayList<>();
        String reg = "a(d)*";
        //以ε开始
        TransNode start = new TransNode(masterIndex.get() + 1, -1, 'ε');
        result.add(start);
        char[] chars = reg.toCharArray();
        int index = 0;
        while (index < chars.length) {
            char c = chars[index];
            if (!alpha(c) && !regexOperator(c)) {
                throw new RuntimeException("数据错误：" + c);
            }
            index = process(c, index, chars, result);
        }
        buildPath(start, result);
        List<Integer> indexs = result.stream().filter(item -> item.state_to > item.index).filter(item -> find(item.state_to).state_from < item.index).map(item -> item.index).collect(Collectors.toList());
        Optional<Trans> max = result.stream().max(Comparator.comparing(Trans::getIndex));
        fixTrans(start, indexs, max.get());
        printTrans(start);

    }
    //修复未指向最后一个转移的节点
    public static void fixTrans(TransNode node, List<Integer> indexs, Trans max) {
        if (indexs.contains(node.index)) {
            node.state_to = max.index;
        }
        for (TransNode tran : node.childes) {
            fixTrans(tran, indexs, max);
        }
    }

    public static void printTrans(TransNode node) {
        System.out.println(node);
        for (TransNode tran : node.childes) {
            printTrans(tran);
        }
    }

    public static List<TransNode> findTrans(int from, List<Trans> result) {
        return result.stream().filter(item -> item.state_from == from).map(TransNode::new).collect(Collectors.toList());
    }

   //构建树
    public static TransNode buildPath(TransNode node, List<Trans> result) {
        List<TransNode> trans = findTrans(node.index, result);
        node.childes = trans;
        for (TransNode tran : trans) {
            buildPath(tran, result);
        }
        return node;
    }
}
