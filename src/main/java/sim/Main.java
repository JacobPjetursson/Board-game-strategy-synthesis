package sim;

import sim.ai.LookupTableMinimax;

import static misc.Globals.PLAYER1;
import static misc.Globals.PLAYER2;

public class Main {

    public static void main(String[] args) {
        State s = new State();
        Move m = new Move(PLAYER1, new Line(0,0,1,0));
        Move m1 = new Move(PLAYER2, new Line(0, 0, 0, 1));
        Move m2 = new Move(PLAYER1, new Line(0, 0, 1, 1));
        Move m3 = new Move(PLAYER2, new Line(1, 1, 1, 2));
        Move m4 = new Move(PLAYER1, new Line(1, 1, 1, 0));


/*
        State s1 = s.getNextState(m);
        System.out.println(s1.hashCode());
        State s2 = s1.getNextState(m1);
        System.out.println(s2.hashCode());
        State s3 = s2.getNextState(m2);
        System.out.println(s3.hashCode());
        State s4 = s3.getNextState(m3);
        System.out.println(s4.hashCode());
        State s5 = s4.getNextState(m4);
        System.out.println(s5.hashCode());

        System.out.println();

        State ss1 = s.getNextState(m2);
        System.out.println(ss1.hashCode());
        State ss2 = ss1.getNextState(m1);
        System.out.println(ss2.hashCode());
        State ss3 = ss2.getNextState(m);
        System.out.println(ss3.hashCode());
        State ss4 = ss3.getNextState(m3);
        System.out.println(ss4.hashCode());
        State ss5 = ss4.getNextState(m4);
        System.out.println(ss5.hashCode());
*/



        new LookupTableMinimax(PLAYER1, s);
        /*

        GameSpecifics specs = new GameSpecifics();
        FFTManager fftManager = new FFTManager(specs);
        try {
            fftManager.autogenFFT();
        } catch (TransitionDefinitionException | MoveDefinitionException | GoalDefinitionException e) {
            e.printStackTrace();
        }

         */
    }
}
