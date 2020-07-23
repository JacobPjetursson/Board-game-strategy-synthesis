package sim;

import fftlib.*;


import static misc.Globals.*;

public class Main {

    public static void main(String[] args) {
        CURRENT_GAME = SIM;
        Node n = new Node();
        GameSpecifics specs = new GameSpecifics();
        FFTManager.initialize(specs);
        FFTAutoGen.synthesize();

    }
}
