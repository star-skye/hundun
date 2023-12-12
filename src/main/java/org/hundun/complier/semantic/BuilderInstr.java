package org.hundun.complier.semantic;

import org.hundun.complier.lr1.util.Rule;
import org.hundun.complier.semantic.entity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;

public class BuilderInstr {
    static Map<String, BiFunction<Rule, Stack<StackItem2>, Entry>> map  = new HashMap<>();
    static {
        map.put("S",((rule, stackItem) -> {
            return stackItem.pop().entry;
        }));
        map.put("D",((rule, stackItem) -> {
            stackItem.pop();
            StackItem2 id= stackItem.pop();
            StackItem2 type  = stackItem.pop();
            return new Entry(new DeclarationInstr((TypeInstr) type.entry.value, (IdInstr) id.entry.value),null, null);
        }));
        map.put("E",((rule, stackItem) -> {
            if (rule.getRightSide().length == 1) {
                return stackItem.pop().entry;
            }
            StackItem2 op1= stackItem.pop();
            StackItem2 op  = stackItem.pop();
            StackItem2 op2  = stackItem.pop();
            return new Entry(new ConditionInstr((IdInstr) op1.entry.value, (IdInstr) op2.entry.value, op.symbol), op1.entry, op2.entry);
        }));
        map.put("IFELSE",((rule, stackItem) -> {
            StackItem2 else_= stackItem.pop();
            StackItem2 if_  = stackItem.pop();
            return new Entry(new IfInstr(InstrKind.IF_Else), if_.entry, else_.entry);
        }));
        map.put("L",((rule, stackItem) -> {
            stackItem.pop();
            StackItem2 number = stackItem.pop();
            stackItem.pop();
            StackItem2 id = stackItem.pop();
            return new Entry(new AssignInstr((IdInstr) id.entry.value,  number.entry.value), null, null);
        }));
        map.put("IF",((rule, stackItem) -> {
            stackItem.pop();
            StackItem2 assign = stackItem.pop();
            stackItem.pop();
            stackItem.pop();
            StackItem2 condition = stackItem.pop();
            stackItem.pop();
            stackItem.pop();
            return new Entry(new IfInstr((ConditionInstr) condition.entry.value,null, (AssignInstr) assign.entry.value), null, null);
        }));
        map.put("ELSE",((rule, stackItem) -> {
            stackItem.pop();
            AssignInstr instr = (AssignInstr) stackItem.pop().entry.value;
            stackItem.pop();
            stackItem.pop();
            return  new Entry(new ElseInstr(instr), null, null);
        }));
        map.put("ID",((rule, stackItem) ->{
            StackItem2 pop = stackItem.pop();
            return  new Entry(new IdInstr(pop.symbol), null, null);
        }));
        map.put("T",((rule, stackItem) -> new Entry(new TypeInstr(stackItem.pop().symbol), null, null)));
        map.put("N",((rule, stackItem) ->  new Entry(new NumberInstr(Integer.parseInt(stackItem.pop().symbol)), null, null)));
    }

    public static Entry build(Rule rule, Stack<StackItem2> stack) {
        return map.get(rule.getLeftSide()).apply(rule,stack);

    }
}
