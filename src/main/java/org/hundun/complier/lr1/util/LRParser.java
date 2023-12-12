package org.hundun.complier.lr1.util;

import org.hundun.complier.lr0.StackItem;
import org.hundun.complier.semantic.BuilderInstr;
import org.hundun.complier.semantic.entity.Entry;
import org.hundun.complier.semantic.entity.StackItem2;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class LRParser {

    protected HashMap<String, Integer>[] goToTable;
    protected HashMap<String, Action>[] actionTable;
    protected Grammar grammar;

    public LRParser(Grammar grammar) {
        this.grammar = grammar;
    }

    protected abstract void createGoToTable();

    //语法解析 返回结果为是否正确解析
    public boolean accept(List<String> inputs) {
        inputs.add("$");
        int index = 0;
        Stack<String> stack = new Stack<>();
        //初始化工作栈为0
        stack.add("0");
        while (index < inputs.size()) {
            //获取栈顶状态为0
            int state = Integer.valueOf(stack.peek());
            //获取当前解析符号
            String nextInput = inputs.get(index);
            //获取动作
            Action action = actionTable[state].get(nextInput);
            //为null则解析失败
            if (action == null) {
                return false;
                //递进
            } else if (action.getType() == ActionType.SHIFT) {
                //将符号与状态加入到工作栈中
                stack.push(nextInput);
                stack.push(action.getOperand() + "");
                //递进输入符号
                index++;
                //归约操作
            } else if (action.getType() == ActionType.REDUCE) {
                int ruleIndex = action.getOperand();
                //获取到归约规则
                Rule rule = grammar.getRules().get(ruleIndex);
                String leftSide = rule.getLeftSide();
                int rightSideLength = rule.getRightSide().length;
                //根据归约规则弹出工作栈
                for (int i = 0; i < 2 * rightSideLength; i++) {
                    stack.pop();
                }
                //获取当前归约后的状态也即书中的回滚状态
                int nextState = Integer.valueOf(stack.peek());
                //压入当前归约非终结符
                stack.push(leftSide);
                //查询goto表
                int variableState = goToTable[nextState].get(leftSide);
                //压入当前状态
                stack.push(variableState + "");
            } else if (action.getType() == ActionType.ACCEPT) {
                //接受
                return true;
            }
        }
        return false;
    }

    public StackItem accept2(List<String> inputs) {
        inputs.add("$");
        int index = 0;
        Stack<StackItem> stack = new Stack<>();
        //初始化工作栈为0
        stack.add(new StackItem(0, "$", null));
        while (index < inputs.size()) {
            //获取栈顶状态为0
            int state = stack.peek().operand;
            //获取当前解析符号
            String nextInput = inputs.get(index);
            //获取动作
            Action action = actionTable[state].get(nextInput);
            //为null则解析失败
            if (action == null) {
                return null;
                //递进
            } else if (action.getType() == ActionType.SHIFT) {
                //将符号与状态加入到工作栈中
                stack.push(new StackItem(action.getOperand(), nextInput, null));
                //递进输入符号
                index++;
                //归约操作
            } else if (action.getType() == ActionType.REDUCE) {
                int ruleIndex = action.getOperand();
                //获取到归约规则
                Rule rule = grammar.getRules().get(ruleIndex);
                String leftSide = rule.getLeftSide();
                int rightSideLength = rule.getRightSide().length;
                StackItem.Entry value = null;
                if (rule.fun != null) {
                    value = fun2(rule.fun,stack);
                }
                //根据归约规则弹出工作栈
                for (int i = 0; i < rightSideLength; i++) {
                    stack.pop();
                }
                //获取当前归约后的状态也即书中的回滚状态
                int nextState = stack.peek().operand;
                //查询goto表
                int variableState = goToTable[nextState].get(leftSide);
                //压入当前状态
                StackItem stackItem = new StackItem(variableState, leftSide, null);
                stackItem.entry = value;
                stack.push(stackItem);
            } else if (action.getType() == ActionType.ACCEPT) {
                //接受
                return stack.pop();
            }
        }
        return null;
    }

    public Entry accept3(List<String> inputs) {
        inputs.add("$");
        int index = 0;
        Stack<StackItem2> stack = new Stack<>();
        //初始化工作栈为0
        stack.add(new StackItem2(0, "$", null));
        while (index < inputs.size()) {
            //获取栈顶状态为0
            int state = stack.peek().operand;
            //获取当前解析符号
            String nextInput = inputs.get(index);
            //获取动作
            Action action = actionTable[state].get(nextInput);
            //为null则解析失败
            if (action == null) {
                return null;
                //递进
            } else if (action.getType() == ActionType.SHIFT) {
                //将符号与状态加入到工作栈中
                stack.push(new StackItem2(action.getOperand(), nextInput, null));
                //递进输入符号
                index++;
                //归约操作
            } else if (action.getType() == ActionType.REDUCE) {
                int ruleIndex = action.getOperand();
                //获取到归约规则
                Rule rule = grammar.getRules().get(ruleIndex);
                String leftSide = rule.getLeftSide();
//                int rightSideLength = rule.getRightSide().length;
                Entry value = BuilderInstr.build(rule,stack);
                //根据归约规则弹出工作栈
//                for (int i = 0; i < rightSideLength; i++) {
//                    stack.pop();
//                }
                //获取当前归约后的状态也即书中的回滚状态
                int nextState = stack.peek().operand;
                //查询goto表
                int variableState = goToTable[nextState].get(leftSide);
                //压入当前状态
                StackItem2 stackItem = new StackItem2(variableState, leftSide, null);
                stackItem.entry = value;
                stack.push(stackItem);
            } else if (action.getType() == ActionType.ACCEPT) {
                //接受
                return stack.pop().entry;
            }
        }
        return null;
    }

    /**
     * 执行fun
     *stackItem = {StackItem2@786}
     * @param fun
     * @return
     */
    public String fun(String fun, Stack<StackItem> stack) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(fun);
        if (!matcher.find()) {
            throw new RuntimeException("fun execv error");
        }
        String group = matcher.group(1);
        String[] split = group.split("=");
        //处理数字
        try {
            Integer.parseInt(split[1]);
            return split[1];
        } catch (Exception e) {
            //忽略
        }
        ArrayList<StackItem> list = new ArrayList<>(stack);
        String[] split1 = split[1].split("\\+");
        //处理值类型
        if (split1.length == 1) {
            int parseInt = Integer.parseInt(split1[0].replace("$", "")) - 1;
            StackItem stackItem = list.get(list.size() - 1 - parseInt);
            return stackItem.value;
        } else {
            //处理加法运算
            int parseInt1 = Integer.parseInt(split1[0].replace("$", "")) - 1;
            int parseInt2 = Integer.parseInt(split1[1].replace("$", "")) - 1;
            return Integer.parseInt(list.get(list.size() - 1 - parseInt1).value) + Integer.parseInt(list.get(list.size() - 1 - parseInt2).value) + "";
        }
    }
    public StackItem.Entry
    fun2(String fun, Stack<StackItem> stack) {
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(fun);
        if (!matcher.find()) {
            throw new RuntimeException("fun execv error");
        }
        String group = matcher.group(1);
        String[] split = group.split("=");
        //处理数字
        try {
            Integer.parseInt(split[1]);
            return StackItem.createEntry(split[1],null,null);
        } catch (Exception e) {
            //忽略
        }
        ArrayList<StackItem> list = new ArrayList<>(stack);
        String[] split1 = split[1].split("\\+");
        //处理值类型
        if (split1.length == 1) {
            int parseInt = Integer.parseInt(split1[0].replace("$", "")) - 1;
            StackItem stackItem = list.get(list.size() - 1 - parseInt);
            return stackItem.entry;
        } else {
            //处理加法运算
            int parseInt1 = Integer.parseInt(split1[0].replace("$", "")) - 1;
            int parseInt2 = Integer.parseInt(split1[1].replace("$", "")) - 1;
            return StackItem.createEntry("+",list.get(list.size() - 1 - parseInt2).entry,list.get(list.size() - 1 - parseInt1).entry);
        }
    }

    public String goToTableStr() {
        String str = "Go TO Table : \n";
        str += "          ";
        for (String variable : grammar.getVariables()) {
            str += String.format("%-6s", variable);
        }
        str += "\n";

        for (int i = 0; i < goToTable.length; i++) {
            for (int j = 0; j < (grammar.getVariables().size() + 1) * 6 + 2; j++) {
                str += "-";
            }
            str += "\n";
            str += String.format("|%-6s|", i);
            for (String variable : grammar.getVariables()) {
                str += String.format("%6s", (goToTable[i].get(variable) == null ? "|" : goToTable[i].get(variable) + "|"));
            }
            str += "\n";
        }
        for (int j = 0; j < (grammar.getVariables().size() + 1) * 6 + 2; j++) {
            str += "-";
        }
        return str;
    }

    public String actionTableStr() {
        String str = "Action Table : \n";
        HashSet<String> terminals = new HashSet<>(grammar.getTerminals());
        terminals.add("$");
        str += "                ";
        for (String terminal : terminals) {
            str += String.format("%-10s", terminal);
        }
        str += "\n";

        for (int i = 0; i < actionTable.length; i++) {
            for (int j = 0; j < (terminals.size() + 1) * 10 + 2; j++) {
                str += "-";
            }
            str += "\n";
            str += String.format("|%-10s|", i);
            for (String terminal : terminals) {
                str += String.format("%10s", (actionTable[i].get(terminal) == null ? "|" : actionTable[i].get(terminal) + "|"));
            }
            str += "\n";
        }
        for (int j = 0; j < (terminals.size() + 1) * 10 + 2; j++) {
            str += "-";
        }
        return str;
    }

    public Grammar getGrammar() {
        return grammar;
    }
}
