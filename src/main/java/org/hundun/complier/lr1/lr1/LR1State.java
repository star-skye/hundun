package org.hundun.complier.lr1.lr1;


import org.hundun.complier.lr1.util.Grammar;
import org.hundun.complier.lr1.util.Rule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class LR1State {
    private LinkedHashSet<LR1Item> items;
    private HashMap<String,LR1State> transition;

    public LR1State(Grammar grammar, HashSet<LR1Item> coreItems){
        items = new LinkedHashSet<>(coreItems);
        transition = new HashMap<>();
        //计算该项目集的闭包
        closure(grammar);
    }

    private void closure(Grammar grammar) {
        boolean changeFlag = false;
        do {
            changeFlag = false;
            //遍历项目集
            for(LR1Item item : items){
                //若未解析完成.符号不是length 并且当前解析符号是非终结符,此处.为虚拟存在初始化为0同时0也是下一个需要解析的符号
                if(item.getDotPointer() != item.getRightSide().length && grammar.isVariable(item.getCurrent())){
                    //lookahead 是LR1的核心 用来得到前看信息
                    HashSet<String> lookahead = new HashSet<>();
                    //若解析到最后一个则将当前item的前看符号配置其中
                    if(item.getDotPointer() == item.getRightSide().length - 1){
                        lookahead.addAll(item.getLookahead());
                    }else{
                        //否则计算下一个符号的first集
                        HashSet<String> firstSet = grammar.computeFirst(item.getRightSide(),item.getDotPointer()+1);
                        //若first允许为空则前看符号也允许是他本身
                        if(firstSet.contains("epsilon")){
                            firstSet.remove("epsilon");
                            firstSet.addAll(item.getLookahead());
                        }
                        lookahead.addAll(firstSet);
                    }
                    //获取当前非终结符的规则
                    HashSet<Rule> rules = grammar.getRuledByLeftVariable(item.getCurrent());
                    for(Rule rule : rules){
                        String[] rhs = rule.getRightSide();
                        int finished = 0;
                        //若当前规则是为空 则设置为结束态
                        if (rhs.length == 1 && rhs[0].equals("epsilon")) {
                            finished = 1;
                        }
                        //每个规则都将使用计算出的前看符号
                        HashSet<String> newLA = new HashSet<String>(lookahead);
                        //根据当前规则构建新的item
                        LR1Item newItem = new LR1Item(rule.getLeftSide(),rhs,finished,newLA);
                        // merge lookaheads with existing item
                        boolean found = false;
                        //查找item是否重复
                        for (LR1Item existingItem : items) {
                            if (newItem.equalLR0(existingItem)) {
                                HashSet<String> existLA = existingItem.getLookahead();
                                //若重复需要查看当前新的前看列表在原有基础上是否存在
                                if (!existLA.containsAll(newLA)) {
                                    // changing the lookahead will change the hash code
                                    // of the item, which means it must be re-added.
                                    //若存在则先移除当前项 为了重新计算hash
                                    items.remove(existingItem);
                                    //然后加入新的列表
                                    existLA.addAll(newLA);
                                    //加入items
                                    items.add(existingItem);
                                    //设置修改
                                    changeFlag = true;
                                }
                                //同时设置为查询到
                                found = true;
                                break;
                            }
                        }
                        //若未查询到则加入items 设置为修改状态
                        if (!found) {
                            items.add(newItem);
                            changeFlag = true;
                        }
                    }
                    //发生修改则需要重新遍历所以此处break
                    if (changeFlag) {
                        break;
                    }
                }
            }
        } while (changeFlag);

    }

    public HashMap<String, LR1State> getTransition() {
        return transition;
    }

    public LinkedHashSet<LR1Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        String s = "";
        for(LR1Item item:items){
            s += item + "\n";
        }
        return s;
    }

}
