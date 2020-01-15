package fftlib.GGPAutogen;

import fftlib.FFTManager;
import org.ggp.base.util.files.FileUtils;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlPool;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;
import org.ggp.base.validator.StaticValidator;
import org.ggp.base.validator.ValidatorException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static misc.Config.PLAYER1;
import static misc.Config.PLAYER2;

public class GGPManager {

    private static StateMachine sm;
    public static Role p1role;
    public static Role p2role;
    public static Move noop;

    public static void loadGDL(String filepath) {
        System.out.println(filepath);
        String rawSheet = FileUtils.readFileAsString(new File(filepath));
        Game theGame = Game.createEphemeralGame(Game.preprocessRulesheet(rawSheet));
        if (theGame.getRules() == null || theGame.getRules().size() == 0) {
            System.err.println("Problem reading the file " + filepath + " or parsing the GDL.");
            return;
        }

        try {
            new StaticValidator().checkValidity(theGame);
        } catch (ValidatorException e) {
            System.err.println("GDL validation error: " + e.toString());
            return;
        }
        List<Gdl> rules = theGame.getRules();
        initialize(rules);
    }

    private static void initialize(List<Gdl> rules) {

        sm = new ProverStateMachine();
        sm.initialize(rules);
        p1role = sm.getRoles().get(0);
        p2role = sm.getRoles().get(1);
        noop = new Move(GdlPool.getConstant("noop"));
        FFTManager.MAX_PRECONS = sm.getInitialState().getContents().size();
    }

    public static MachineState getInitialState() {
        return sm.getInitialState();
    }

    public static MachineState getNextState(MachineState ms, Move move) throws TransitionDefinitionException {
        Role r = getRole(ms);
        int roleIdx = sm.getRoleIndices().get(r);
        Move[] moveList = new Move[] {noop, noop};
        moveList[roleIdx] = move;
        return sm.getNextState(ms, Arrays.asList(moveList));
    }

    public static List<MachineState> getNextStates(MachineState ms) throws TransitionDefinitionException, MoveDefinitionException {
        return sm.getNextStates(ms);
    }

    public static List<Role> getRoles() {
        return sm.getRoles();
    }

    public static int roleToPlayer(Role r) {
        return r.equals(p1role) ? PLAYER1 : PLAYER2;
    }

    /** Return all roles if draw, otherwise return role that won the game. Assumes turn-based game **/
    public static List<Role> getWinners(MachineState ms) throws GoalDefinitionException {
        if (!sm.isTerminal(ms)) // TODO - catch exception and suppress output
            return null;

        List<Integer> goals = sm.getGoals(ms); // same order as getRoles()

        int max = Collections.max(goals);

        List<Role> roles = sm.getRoles();
        List<Role> winners = new ArrayList<>();
        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i) == max) {
                winners.add(roles.get(i));
            }
        }
        return winners;
    }

    public static boolean isTerminal(MachineState ms) {
        return sm.isTerminal(ms);
    }

    public static List<Integer> getPlayerWinners(MachineState ms) throws GoalDefinitionException {
        List<Role> roleWinners = getWinners(ms);
        if (roleWinners == null)
            return null;
        List<Integer> playerWinners = new ArrayList<>();
        for (Role r : roleWinners) {
            playerWinners.add(roleToPlayer(r));
        }
        return playerWinners;
    }

    public static Role getRole(MachineState ms) {
        try {
            List<Move> moves = getLegalMoves(ms, p1role);
            return (moves.size() == 1 && moves.get(0).equals(noop)) ? p2role : p1role;
        } catch (MoveDefinitionException e) {
            return p2role;
        }
    }

    public static List<Move> getLegalMoves(MachineState ms, Role r) throws MoveDefinitionException {
        return sm.getLegalMoves(ms, r);
    }
}
