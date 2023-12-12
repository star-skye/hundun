package org.hundun.complier;

import java.util.Stack;

/**
 *
 */
public class DFAgenStateTableCode {

    static int[][] table = new int[10][255];
    static {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 255; j++) {
                table[i][j] = -1;
            }
        }
        table[0]['a'] = 1;
        table[1]['b'] = 1;
        table[1]['c'] = 1;
        table[1]['d'] = 1;
    }

    //核心栈
    static Stack<Integer> stack = new Stack<>();

    static String chars = "abcd\0";
    static int index = 0;
    static char getChar() {
        return chars.charAt(index++);
    }
    static void rollback(){
        index--;
    }
    static void clear(){
        stack = new Stack<>();
    }
    static void push(int c){
        stack.push(c);
    }

    static int nextToken() {
        int state = 0;
        while (state != -1) {
            char c = getChar();
            if (c == '\0'){
                return state;
            }
            if (state == 1){
                clear();
            }
            push(state);
            state = table[state][c];
        }
        while (state != 1){
            if (stack.empty()){
                break;
            }
            state = stack.pop();
            rollback();
        }
        return state;
    }

    public static void main(String[] args) {
        System.out.println(nextToken());
        System.out.println(index);
    }
}
