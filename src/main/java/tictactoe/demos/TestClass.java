package tictactoe.demos;

import java.util.ArrayList;
import java.util.HashSet;

public class TestClass {
    private ArrayList<MutableInt> mutNumbers;

    TestClass() {
        mutNumbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mutNumbers.add(new MutableInt(i));
        }
    }

    public boolean printNumbers(int thread) {
        System.out.println("Starting thread: " + thread);
        for (MutableInt mut : mutNumbers) {
            System.out.println("thread: " + thread + " , num: " + mut.number);
            if (mut.number == 8)
                return true;
        }
        return false;
    }

    private class MutableInt {
        public int number;

        MutableInt(int num) {
            number = num;
        }
    }

}
