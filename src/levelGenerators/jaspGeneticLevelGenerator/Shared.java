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

    //the minimum size (width and height) of a level
    public static final int MIN_SIZE = 6;

    //the maximum size (width and height) of a level
    public static final int MAX_SIZE = 18;
}
