package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;
import tools.GameAnalyzer;

import java.util.Random;

/**
 * Created by Jonas on 28-Mar-17.
 */
public class Shared {

    //random number generator
    public static Random random;

    //game description for the game the level is being generated for
    public static GameDescription game;

    //game analyzer for more helpful information about the current game
    public static GameAnalyzer gameAnalyzer;

    //the minimum size (width or height) of a level
    public static final int MIN_SIZE = 6;

    //the maximum size (width or height) of a level
    public static final int MAX_SIZE = 18;

    public static final int POPULATION_SIZE = 50;

    //the number of best individuals to keep
    public static final int ELITE_SIZE = 2;

    //the percentage of the level to be filled on initialization
    public static final double RANDOM_FILL_FACTOR = 0.20;

    //the probability of a mutation happening
    public static final double MUTATION_PROB = 0.05;

    //the probability of a crossover happening
    public static final double CROSSOVER_PROB = 0.5;

    //the maximum allowed time for the step controller to evaluate a step (in ms)
    public static final long MAX_STEP_TIME = 50;

    //the maximum allowed time for evaluating fitness (in ms)
    public static final long EVALUATION_TIME = 10000;

    //the amount of times the test controllers play the level
    public static final int REPETITION_AMOUNT = 50;

    //the minimum solution length of the level
    public static final int MIN_SOLUTION_LENGTH = 150;

    //the minimum amount of steps the doNothing agent must be able to survive
    public static final int MIN_DO_NOTHING_STEPS = 50;

    //the minimum percentage of the level to be covered
    public static final double MIN_COVER_PERCENTAGE = 0.1;

    //the maximum percentage of the level to be covered
    public static final double MAX_COVER_PERCENTAGE = 0.4;

    //a small constant value
    public static final double EPSILON = 1.0e-9;

    //width of the level being generated
    public static int width;

    //height of the level being generated
    public static int height;
}
