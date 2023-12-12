package org.hundun.complier;

import java.util.Stack;

/**
 * 自顶向下分析算法
 */
public class TopDownAnalysisMethod {
    public static int process(Object[] tokens) {
        Stack<Stack<Object>> record = new Stack<>();
        Stack<Object> stack = new Stack<>();
        //名词列表
        Object[] N = {"小明", "作业", "小胖", "篮球" };
        //动词列表
        Object[] V = {"打", "做" };
        //起始节点&语法
        Object[] S = {N, V, N};
        //规则公式
        Object[][] P = {N, V, S};
        //计算索引 当i==tokens.length时代表匹配成功
        int i = 0;
        //先压入其实节点
        stack.push(S);
        while (!stack.empty()) {
            //若栈顶数据为String 则代表要与token进行匹配
            if (stack.peek() instanceof String) {
                //匹配token
                if (stack.peek().equals(tokens[i++])) {
                    //若成功则弹出匹配
                    stack.pop();
                    //并且将后序未参与匹配的名词存入新栈用于回滚
                    Stack<Object> temp = new Stack<>();
                    while (!stack.empty() && !(stack.peek() instanceof Object[])) {
                        temp.push(stack.pop());
                    }
                    if (stack.empty()) continue;
                    //保存下一个要解析的Object[]到栈底，因为若要回溯代表当前Object[]
                    //已经被展开所以回溯也需要还原当前正在解析的Object[]，而当前Object[]就是上一个匹配字符的peek
                    temp.push(stack.peek());
                    //然后填入未匹配信息
                    record.push(temp);
                } else {
                    //若匹配失败则回溯token index
                    i--;
                    //移除未匹配字符
                    stack.pop();
                    //若已经是最后一个匹配字符，那么将需要回溯
                    if (!stack.empty() && stack.peek() instanceof Object[]) {
                        //继续回溯token index
                        i--;
                        //若没有回溯信息代表当前是第一个Object[]则直接返回
                        if (record.empty()) {
                            return -1;
                        }
                        //否则拿出记录栈
                        Stack<Object> temp = record.pop();
                        //反向填充到stack中
                        for (int i1 = temp.size() - 1; i1 >= 0; i1--) {
                            stack.push(temp.get(i1));
                        }
                    }
                }
                //代表字符已配结束，解析Object[]
            } else if (stack.peek() instanceof Object[]) {
                //获取栈顶 并且倒叙插入到stack中进行上方的字符匹配
                Object[] pop = (Object[]) stack.pop();
                for (int i1 = pop.length - 1; i1 >= 0; i1--) {
                    stack.push(pop[i1]);
                }
            }
        }
        return i;
    }

    public static void main(String[] args) {
        Object[] tokens = {"小明", "打2", "篮球" };
        System.out.println(process(tokens) == tokens.length ? "匹配成功" : "匹配失败");
        Object[] tokens2 = {"小明", "打", "篮球" };
        System.out.println(process(tokens2) == tokens2.length ? "匹配成功" : "匹配失败");
    }
}
