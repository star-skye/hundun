package org.hundun.complier.lr1.lr1;


import org.hundun.complier.lr1.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class LR1Parser extends LRParser {

    private ArrayList<LR1State> canonicalCollection;

    public LR1Parser(Grammar grammar){
        super(grammar);
    }

    protected void createStatesForCLR1() {
        canonicalCollection = new ArrayList<>();
        HashSet<LR1Item> start = new HashSet<>();
        //从第一条规则开始
        Rule startRule = grammar.getRules().get(0);
        //此为算法核心 在SLR基础上加入了向前看符号
        HashSet<String> startLockahead = new HashSet<>();
        startLockahead.add("$");
        //构建第一个项目 S' -> .S 0 $,0代表.的位置 $代表他的下一个符号
        start.add(new LR1Item(startRule.getLeftSide(),startRule.getRightSide(),0,startLockahead));
        //构建第一个项目集
        LR1State startState = new LR1State(grammar, start);
        //将第一个项目集加入到集合中
        canonicalCollection.add(startState);
        //遍历该集合
        for (int i = 0; i < canonicalCollection.size(); i++) {
            HashSet<String> stringWithDot = new HashSet<>();
            //获取项目集中未处理的符号信息 stringWithDot
            for (LR1Item item : canonicalCollection.get(i).getItems()) {
                //只有点位==length时返回null
                if (item.getCurrent() != null) {
                    stringWithDot.add(item.getCurrent());
                }
            }
            //遍历所有符号
            for (String str : stringWithDot) {
                //构建next项目集
                HashSet<LR1Item> nextStateItems = new HashSet<>();
                for (LR1Item item : canonicalCollection.get(i).getItems()) {
                    //查找到当前符号所对应的项目
                    if (item.getCurrent() != null && item.getCurrent().equals(str)) {
                        //通过项目信息移动点位后构建新的项目
                        LR1Item temp = new LR1Item(item.getLeftSide(),item.getRightSide(),item.getDotPointer()+1,item.getLookahead());
                        //加入next
                        nextStateItems.add(temp);
                    }
                }
                //通过计算集构建下一个项目集
                LR1State nextState = new LR1State(grammar, nextStateItems);
                boolean isExist = false;
                //判断当前项目集是否已经存在列表中
                for (int j = 0; j < canonicalCollection.size(); j++) {
                    //若存在则将查找到的项目集加入到当前遍历项目集的Transition中让其成为关系链,在构建表是用到
                    if (canonicalCollection.get(j).getItems().containsAll(nextState.getItems())
                            && nextState.getItems().containsAll(canonicalCollection.get(j).getItems())) {
                        isExist = true;
                        canonicalCollection.get(i).getTransition().put(str, canonicalCollection.get(j));
                    }
                }
                //否则不存在则将新的项目集加入到集合,并且将新的集和加入到当前遍历的Transition中
                if (!isExist) {
                    canonicalCollection.add(nextState);
                    canonicalCollection.get(i).getTransition().put(str, nextState);
                }
            }
        }

    }

    public boolean parseCLR1(){
        //构建状态表
        createStatesForCLR1();
        createGoToTable();
        return createActionTable();
    }

    public boolean parseLALR1(){
        createStatesForLALR1();
        createGoToTable();
        return createActionTable();
    }

    /**
     * LR1的加强版本 减少了空间损耗也即压缩LR1
     */
    public void createStatesForLALR1(){
        //首先构建LR1
        createStatesForCLR1();
        ArrayList<LR1State> temp = new ArrayList<>();
        for (int i = 0; i < canonicalCollection.size(); i++) {
            //构建新的集合
            HashSet<LR1ItemEquals> itemsi = new HashSet<>();
            for(LR1Item item:canonicalCollection.get(i).getItems()){
                itemsi.add(new LR1ItemEquals(item.getLeftSide(),item.getRightSide(),item.getDotPointer()));
            }
            //遍历除当前i之前的所执行的项目集
            for (int j = i+1; j < canonicalCollection.size(); j++) {
                //再次构建新的集合
                HashSet<LR1ItemEquals> itemsj = new HashSet<>();
                for(LR1Item item:canonicalCollection.get(j).getItems()){
                    itemsj.add(new LR1ItemEquals(item.getLeftSide(),item.getRightSide(),item.getDotPointer()));
                }
                //若当前i的新集合完全包含j 并且 j也包含i 则尝试合并 合并的核心便是向前看符号
                if(itemsi.containsAll(itemsj) && itemsj.containsAll(itemsi)){
                    for(LR1Item itemi : canonicalCollection.get(i).getItems()){
                        for(LR1Item itemj : canonicalCollection.get(j).getItems()){
                            if(itemi.equalLR0(itemj)){
                                //注意此处不在是itemsi与itemsj而是集合中的内容所以可以直接修改,itemsi与itemsj二者的使命就是比较包含
                                itemi.getLookahead().addAll(itemj.getLookahead());
                                break;
                            }
                        }
                    }
                    //合并
                    for (int k = 0; k < canonicalCollection.size(); k++) {
                        for(String s : canonicalCollection.get(k).getTransition().keySet()){
                            //若当前k的Transition包含所有i并且i包含所有的k那么代表当前s的Transition是可以合并的则设置s的Transition为i
                            if(canonicalCollection.get(k).getTransition().get(s).getItems().containsAll(canonicalCollection.get(i).getItems()) &&
                                    canonicalCollection.get(i).getItems().containsAll(canonicalCollection.get(k).getTransition().get(s).getItems())){
                                canonicalCollection.get(k).getTransition().put(s,canonicalCollection.get(i));
                            }
                        }
                    }
                    //移除j 因为已经和i合并
                    canonicalCollection.remove(j);
                    j--;
                }
            }
            //最终将相同的项目进行合并后加入到temp
            temp.add(canonicalCollection.get(i));
        }
        //然后修改全局集合 剩下与LR1相同构建goto/action表
        canonicalCollection = temp;
    }

    protected void createGoToTable() {
        //构建goto表其状态长度与集合相同
        goToTable = new HashMap[canonicalCollection.size()];
        //初始化每个状态的集合
        for (int i = 0; i < goToTable.length; i++) {
            goToTable[i] = new HashMap<>();
        }
        for (int i = 0; i < canonicalCollection.size(); i++) {
            //获取当前状态的跳转
            for (String s : canonicalCollection.get(i).getTransition().keySet()) {
                //此处只处理非终结符
                if (grammar.isVariable(s)) {
                    //当前状态的 i 非终结符 s 跳转到的状态index findStateIndex
                    goToTable[i].put(s, findStateIndex(canonicalCollection.get(i).getTransition().get(s)));
                }
            }
        }
    }

    private int findStateIndex(LR1State state) {
        //遍历获取指定状态的下标
        for (int i = 0; i < canonicalCollection.size(); i++) {
            if (canonicalCollection.get(i).equals(state)) {
                return i;
            }
        }
        return -1;
    }

    private boolean createActionTable() {
        actionTable = new HashMap[canonicalCollection.size()];
        for (int i = 0; i < actionTable.length; i++) {
            actionTable[i] = new HashMap<>();
        }
        //与goto处理相同 只不过此处是action行为,但是此处遍历的行为只是递进
        for (int i = 0; i < canonicalCollection.size(); i++) {
            for (String s : canonicalCollection.get(i).getTransition().keySet()) {
                //只有为终结符的才进行递进
                if (grammar.getTerminals().contains(s)) {
                    actionTable[i].put(s, new Action(ActionType.SHIFT, findStateIndex(canonicalCollection.get(i).getTransition().get(s))));
                }
            }
        }
        //处理reduce与accept
        for (int i = 0; i < canonicalCollection.size(); i++) {
            for (LR1Item item : canonicalCollection.get(i).getItems()) {
                //保证当前项目已经被处理完成 因为只有处理完成的才存在reduce
                if (item.getDotPointer() == item.getRightSide().length) {
                    //若当前项目是S那么将动作设置为accept
                    if (item.getLeftSide().equals("S'")) {
                        actionTable[i].put("$", new Action(ActionType.ACCEPT, 0));
                    } else {
                        //构建规则
                        Rule rule = new Rule(item.getLeftSide(), item.getRightSide().clone());
                        //查找归约规则 保证left与right完全相等
                        int index = grammar.findRuleIndex(rule);
                        //设置为reduce事件
                        Action action = new Action(ActionType.REDUCE, index);
                        //当前需要reduce那么就需要通过向前看来保证其的确定性,保证下一个此符号一定归约不会出错,所以使用遍历Lookahead的方式进行设置
                        for (String str : item.getLookahead()) {
                            if (actionTable[i].get(str) != null) {
                                System.out.println("it has a REDUCE-" + actionTable[i].get(str).getType() + " confilct in state " + i);
                               continue;
                            } else {
                                actionTable[i].put(str, action);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public String canonicalCollectionStr() {
        String str = "Canonical Collection : \n";
        for (int i = 0; i < canonicalCollection.size(); i++) {
            str += "State " + i + " : \n";
            str += canonicalCollection.get(i)+"\n";
        }
        return str;
    }

}
