package org.hundun.complier.lr1.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public class Grammar {

    private ArrayList<Rule> rules;
    private HashSet<String> terminals;
    private HashSet<String> variables;
    private String startVariable;
    private HashMap<String, HashSet<String>> firstSets;
    private HashMap<String, HashSet<String>> fallowSets;

    public Grammar(String s) {
        System.out.println(s);
        //规则表 推导公式
        rules = new ArrayList<>();
        //终结符
        terminals = new HashSet<>();
        //非终结符
        variables = new HashSet<>();
        int line = 0;
        //每行一个公式
        for(String st : s.split("\n")){
            //sides左边为非终结符
            String[] sides = st.split("->");
            String leftSide = sides[0].trim();
            variables.add(leftSide);
            //右边则是推导公式 而推导公式允许存在多个一|分割
            String[] rulesRightSide = sides[1].trim().split("\\|");
            for (String rule : rulesRightSide) {
                //遍历每个推导式 以空格隔开作为每个解析符号 在前文算法中使用char作为解析符号而此处使用空格作为标记
                String[] rightSide = rule.trim().split("\\s+");
                List<String> newRightSide = new ArrayList<>();
                String fun = null;
                for (String terminal : rightSide) {
                    //ε 将非ε 的符号加入到终结符中 那么代表除 ε 全部加入
                    if (!terminal.equals("epsilon")&&!Pattern.matches("\\{.*?\\}",terminal)) {
                        terminals.add(terminal);
                        newRightSide.add(terminal);
                    }
                    if (Pattern.matches("\\{.*?\\}",terminal)){
                        fun = terminal;
                    }
                }
                //初始化第一行
                if (line == 0) {
                    //起始符号使用拓广文法 加入S' 而S'的推导式子则是第一行的左边非终结符号
                    startVariable = leftSide;
                    rules.add(new Rule("S'", new String[]{startVariable}));
                }

                //将解析的推导非终结符与终结符组合加入到规则中也即 S -> ab S是left  ab为right
                rules.add(new Rule(leftSide, newRightSide.toArray(new String[0]),fun));
                line++;
            }
        }
        //前面将终结符与非终结符都加入到终结符列表中,此处则将非终结符移除
        for (String variable : variables) {
            terminals.remove(variable);
        }
        //打印信息
        System.out.println("Rules: ");
        for (int i=0 ; i<rules.size() ; i++) {
            System.out.println(i+" : " +rules.get(i));
        }

        //构建first_s集
        computeFirstSets();
        //构建follow集
        computeFollowSet();
    }

    public ArrayList<Rule> getRules() {
        return rules;
    }
   
    public int findRuleIndex(Rule rule){
        for(int i=0 ; i<rules.size();i++){
            if(rules.get(i).equals(rule)){
                return i;
            }
        }
        return -1;
    }
    public HashSet<String> getVariables() {
        return variables;
    }

    public String getStartVariable() {
        return startVariable;
    }

    private void computeFirstSets() {
        firstSets = new HashMap<>();
        //给每个非终结符构建一个空set
        for (String s : variables) {
            HashSet<String> temp = new HashSet<>();
            firstSets.put(s, temp);
        }
        while (true) {
            //是否发生改变若未发生则跳出循环
            boolean isChanged = false;
            for (String variable : variables) {
                HashSet<String> firstSet = new HashSet<>();
                //遍历所以规则
                for (Rule rule : rules) {
                    //若规则左边为当前非终结符
                    if (rule.getLeftSide().equals(variable)) {
                        //则构建其非终结符的first_s集
                        HashSet<String> addAll = computeFirst(rule.getRightSide(), 0);
                        //将构建结果加入到set中
                        firstSet.addAll(addAll);
                    }
                }
                //判断当前非终结符的first是否已经存在 若不存在则添加并且设置修改为true
                if (!firstSets.get(variable).containsAll(firstSet)) {
                    isChanged = true;
                    firstSets.get(variable).addAll(firstSet);
                }
            }
            if (!isChanged) {
                break;
            }
        }

        firstSets.put("S'", firstSets.get(startVariable));
    }

    private void computeFollowSet() {
        fallowSets = new HashMap<>();
        //构建每个非终结符的follow集
        for (String s : variables) {
            HashSet<String> temp = new HashSet<>();
            fallowSets.put(s, temp);
        }
        HashSet<String> start = new HashSet<>();
        start.add("$");
        fallowSets.put("S'", start);

        while (true) {
            boolean isChange = false;
            for (String variable : variables) {
                for (Rule rule : rules) {
                    //遍历所有规则获取到规则中当前非终结符的位置
                    for (int i = 0; i < rule.getRightSide().length; i++) {
                        //判断当前位置是否为当前非终结符
                        if (rule.getRightSide()[i].equals(variable)) {
                            HashSet<String> first;
                            //若是则判断是否为最后一个
                            if (i == rule.getRightSide().length - 1) {
                                //若是那么代表他后面可以跟随当前非终结符的first集
                                first = fallowSets.get(rule.leftSide);
                            } else {
                                //否则计算下一个字符的first集
                                first = computeFirst(rule.getRightSide(), i + 1);
                                //若允许为空则加入当前规则的follow集,因为若下一个字符允许为空则需要将其传递上来
                                if (first.contains("epsilon")) {
                                    first.remove("epsilon");
                                    first.addAll(fallowSets.get(rule.leftSide));
                                }
                            }
                            if (!fallowSets.get(variable).containsAll(first)) {
                                isChange = true;
                                fallowSets.get(variable).addAll(first);
                            }
                        }
                    }
                }
            }
            if (!isChange) {
                break;
            }
        }
    }

    public HashSet<String> computeFirst(String[] string, int index) {
        HashSet<String> first = new HashSet<>();
        //此处递归解析所以index==length则返回
        if (index == string.length) {
            return first;
        }
        //若当前第一个就是终结符或者ε则加入set直接返回
        if (terminals.contains(string[index]) || string[index].equals("epsilon")) {
            first.add(string[index]);
            return first;
        }
        //若是非终结符则获取非终结符的first集
        if (variables.contains(string[index])) {
            for (String str : firstSets.get(string[index])) {
                first.add(str);
            }
        }
        //若这个非终结符允许为空也就是ε那么将继续遍历下一个字符
        if (first.contains("epsilon")) {
            if (index != string.length - 1) {
                //移除ε
                first.remove("epsilon");
                first.addAll(computeFirst(string, index + 1));
            }
        }
        //最终保证了非终结符的first的完整集
        return first;
    }

    public HashSet<Rule> getRuledByLeftVariable(String variable) {
        HashSet<Rule> variableRules = new HashSet<>();
        for (Rule rule : rules) {
            if (rule.getLeftSide().equals(variable)) {
                variableRules.add(rule);
            }
        }
        return variableRules;
    }

    public boolean isVariable(String s) {
        return variables.contains(s);
    }

    public HashMap<String, HashSet<String>> getFirstSets() {
        return firstSets;
    }

    public HashMap<String, HashSet<String>> getFallowSets() {
        return fallowSets;
    }

    public HashSet<String> getTerminals() {
        return terminals;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.rules);
        hash = 37 * hash + Objects.hashCode(this.terminals);
        hash = 37 * hash + Objects.hashCode(this.variables);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Grammar other = (Grammar) obj;
        if (!Objects.equals(this.rules, other.rules)) {
            return false;
        }
        if (!Objects.equals(this.terminals, other.terminals)) {
            return false;
        }
        if (!Objects.equals(this.variables, other.variables)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for(Rule rule: rules){
            str += rule + "\n";
        }
        return str;
    }
}
