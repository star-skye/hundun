package org.hundun.complier;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DFAgenJumpTableCode {
    //核心栈
    static Stack<Integer> stack = new Stack<>();

    static String chars = "af\0";
    static int index = 0;

    static char getChar() {
        return chars.charAt(index++);
    }

    static void rollback() {
        index--;
    }

    static void clear() {
        stack = new Stack<>();
    }

    static void push(int c) {
        stack.push(c);
    }
   static Function<AtomicInteger,Function<AtomicInteger,?>> q1 = null;
   static {
        q1 = (num)->{
            int state = num.get();
            char c = getChar();
            if (c == '\0') {
                return null;
            }
            if (state == 1) {
                clear();
            }
            push(state);
            if (c == 'b' || c == 'c' || c == 'd') {
                num.set(state);
                return q1;
            }
            rollback();
            return null;
        };
    }

    static Function<AtomicInteger,Function<AtomicInteger,?>> q0 = (num)->{
        int state = num.get();
        char c = getChar();
        if (c == '\0') {
            return null;
        }
        if (state == 1) {
            clear();
        }
        push(state);
        if (c == 'a') {
            num.set(1);
            return q1;
        }
        rollback();
        return null;
    };

    static int nextToken() {
        AtomicInteger state = new AtomicInteger(0);
        Function<AtomicInteger,Function<AtomicInteger,?>> temp1 = q0;
        for (; temp1!=null; ) {
            temp1 = ( Function<AtomicInteger,Function<AtomicInteger,?>>)temp1.apply(state);
        }
        return state.get();
    }

    public static void main(String[] args) {
        System.out.println(nextToken()+"---"+index);

    }
}
