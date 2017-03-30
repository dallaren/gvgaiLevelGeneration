package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;

import java.util.Random;

/**
 * Created by Jonas on 28-Mar-17.
 */
public class Shared {

    //random number generator
    public static Random random;

    //game description for the game the level is being generated for
    public static GameDescription game;

    //the minimum size (width or height) of a level
    public static final int MIN_SIZE = 6;

    //the maximum size (width or height) of a level
    public static final int MAX_SIZE = 18;

    //the probability of a mutation happening
    public static final double PROB_MUTATION = 0.05;

    //the probability of a crossover happening
    public static final double PROB_CROSSOVER = 0.5;

    //the maximum allowed time for the step controller to evaluate a step (in ms)
    public static final long MAX_STEP_TIME = 50;

    public static final int REPETITION_AMOUNT = 50;

    //width of the level being generated
    public static int width;

    //height of the level being generated
    public static int height;
}
