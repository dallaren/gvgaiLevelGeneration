package levelGenerators.jaspGeneticLevelGenerator;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import levelGenerators.constraints.CombinedConstraints;
import ontology.Types;

import tools.ElapsedCpuTimer;
import tools.StepController;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Individual implements Comparable<Individual> {
    private char[][] level;
    private boolean calculated;
    private double fitness;
    private int borderThickness;

    private AbstractPlayer doNothingController;
    private AbstractPlayer oneStepLookAheadController;
    private AbstractPlayer MaastController;

    private StateObservation stateObservation;

    public Individual() {
        level = new char[height][width];
        initializeLevel();
    }

    private void initializeLevel() {
        borderThickness = 0;
        if (hasSolidSprites()) {
            addBorder();
            borderThickness = 1;
        }
    }

    //ask the game analyzer if the game has any solid sprites to use as a border
    private boolean hasSolidSprites() {
        return !gameAnalyzer.getSolidSprites().isEmpty();
    }

    //add a solid border around the level with a thickness of 1
    private void addBorder() {
        char solidSprite = getSolidSpriteChar();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row == 0 || row == height-1 || col == 0 || col == width-1) {
                    level[row][col] = solidSprite;
                } else {
                    level[row][col] = '.';
                }
            }
        }
    }

    //get the mapping for a solid sprite from the game
    private char getSolidSpriteChar() {
        String solidSpriteString = gameAnalyzer.getSolidSprites().get(0);
        char solidSpriteChar = '.';

        for (Map.Entry<Character, ArrayList<String>> entry : game.getLevelMapping().entrySet()) {
            char charMapping = entry.getKey();
            ArrayList<String> spriteStrings = entry.getValue();

            for (int i = 0; i < spriteStrings.size(); i++) {
                if (spriteStrings.get(i).equals(solidSpriteString)) {
                    solidSpriteChar = charMapping;
                    break;
                }
            }
        }
        return solidSpriteChar;
    }

    //TODO make this mutate instead
    public void initializeRandom() {

        for (int row = borderThickness; row < (width - borderThickness); row++) {
            for (int col = borderThickness; col < (height - borderThickness); col++) {
                if (random.nextDouble() < RANDOM_FILL_FACTOR) {
                    level[row][col] = getRandomCharFromLevelMapping();
                } else {
                    level[row][col] = '.';
                }
            }
        }
        initControllers();
    }

    //TODO add other mutation outcomes (delete, swap)
    //randomly mutate a single tile in the level
    public void mutate() {
        int row = random.nextInt(height - 2*borderThickness) + borderThickness;
        int col = random.nextInt(width - 2*borderThickness) + borderThickness;

        level[row][col] = getRandomCharFromLevelMapping();
        constrainLevel();
    }

    private char getRandomCharFromLevelMapping() {
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        Character[] chars = mappingChars.toArray(new Character[mappingChars.size()]);

        int c = random.nextInt(chars.length);
        return chars[c];
    }

    //TODO make it around a point, not a row/col
    //do a crossover around a random row or column
    public Iterable<Individual> crossOver(Individual partner) {
        ArrayList<Individual> children = new ArrayList<>(2);
        Individual child1 = new Individual();
        Individual child2 = new Individual();

        //decide whether a horizontal or vertical split is to be made
        boolean splitHorizontal = random.nextBoolean();
        int crossOverPoint = splitHorizontal ? random.nextInt(height) : random.nextInt(width);

        //child1 inherits everything above the crossOverPoint from this parent, the rest from the partner
        //child2 inherits everything above the crossOverPoint from the partner, the rest from this parent
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (splitHorizontal) {
                    if (row > crossOverPoint) {
                        child1.getLevel()[row][col] = this.level[row][col];
                        child2.getLevel()[row][col] = partner.getLevel()[row][col];
                    } else {
                        child2.getLevel()[row][col] = this.level[row][col];
                        child1.getLevel()[row][col] = partner.getLevel()[row][col];
                    }
                } else {
                    if (col > crossOverPoint) {
                        child1.getLevel()[row][col] = this.level[row][col];
                        child2.getLevel()[row][col] = partner.getLevel()[row][col];
                    } else {
                        child2.getLevel()[row][col] = this.level[row][col];
                        child1.getLevel()[row][col] = partner.getLevel()[row][col];
                    }
                }
            }
        }

        //make sure the children are well-formed
        child1.constrainLevel();
        child2.constrainLevel();

        children.add(child1);
        children.add(child2);

        return children;
    }

    //make sure the level is well-formed
    private void constrainLevel() {
        constrainAvatar();
    }

    //make sure the level has at most 1 avatar
    private void constrainAvatar() {
        ArrayList<Point> avatarPositions = new ArrayList<>(width*height);

        //find the positions of all avatars in the level
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (level[row][col] == 'A') {
                    avatarPositions.add(new Point(col,row));
                }
            }
        }

        //if there is more than 1 avatar, choose a random one to remove
        if (avatarPositions.size() > 1) {
            int index = random.nextInt(avatarPositions.size());
            Point avatarPosition = avatarPositions.get(index);
            int avatarRow = (int) avatarPosition.getY();
            int avatarCol = (int) avatarPosition.getX();
            level[avatarRow][avatarCol] = ' ';
        }
    }

    public double fitness() {

        if (calculated) {
            return fitness;
        }

        return calculateFitness(EVALUATION_TIME);
    }

    private double calculateFitness(long maxTime) {
        //play a single game with the best controller
        //TODO slow this guy down a little bit, a tad too inhuman
        StepController bestController = new StepController(MaastController, MAX_STEP_TIME);
        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        timer.setMaxTimeMillis(maxTime);
        bestController.playGame(stateObservation.copy(), timer);

        ArrayList<Types.ACTIONS> bestSolution = bestController.getSolution();
        StateObservation bestState = bestController.getFinalState();

        StateObservation doNothingState = null;
        int minDoNothingSteps = Integer.MAX_VALUE;
        //play the game a number of times with the doNothing agent
        for (int i = 0; i < REPETITION_AMOUNT; i++) {
            StateObservation tempState = stateObservation.copy();
            int steps = getControllerResult(doNothingController, tempState, bestSolution.size());
            if (steps < minDoNothingSteps) {
                minDoNothingSteps = steps;
                doNothingState = tempState;
            }
        }

        HashMap<String, Object> constraintParameters = new HashMap<>();
        constraintParameters.put("minSolutionLength", MIN_SOLUTION_LENGTH);
        constraintParameters.put("solutionLength", bestSolution.size());
        constraintParameters.put("bestPlayer", bestState.getGameWinner());
        constraintParameters.put("minDoNothingSteps", MIN_DO_NOTHING_STEPS);
        constraintParameters.put("doNothingSteps", minDoNothingSteps);
        constraintParameters.put("doNothingState", doNothingState.getGameWinner());
        constraintParameters.put("minCoverPercentage", MIN_COVER_PERCENTAGE);
        constraintParameters.put("maxCoverPercentage", MAX_COVER_PERCENTAGE);

        double coverPercentage = getCoverPercentage();
        constraintParameters.put("coverPercentage", coverPercentage);

        CombinedConstraints combinedConstraints = new CombinedConstraints();

        String[] constraints = new String[] {
                "SolutionLengthConstraint",
                "WinConstraint",
                "DeathConstraint",
                "CoverPercentageConstraint"
        };

        combinedConstraints.addConstraints(constraints);
        combinedConstraints.setParameters(constraintParameters);

        double constraintFitness = combinedConstraints.checkConstraint();
        System.out.println("SolutionLength:" + bestSolution.size() + " doNothingSteps:" + minDoNothingSteps + " coverPercentage:" + coverPercentage + " bestPlayer:" + bestState.getGameWinner());

        if (constraintFitness >= 1) {
            StateObservation oneStepLookAheadState = null;
            for (int i = 0; i < REPETITION_AMOUNT; i++) {
                StateObservation tempState = stateObservation.copy();
                getControllerResult(oneStepLookAheadController, tempState, bestSolution.size());
                if (oneStepLookAheadState == null || tempState.getGameScore() > oneStepLookAheadState.getGameScore()) {
                    oneStepLookAheadState = tempState;
                }
            }

            //score difference
            fitness = bestState.getGameScore() - oneStepLookAheadState.getGameScore();
        }

        calculated = true;
        return fitness;
    }

    //TODO check if this actually works or if it does reference checking (probably not)
    private double getCoverPercentage() {
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        int sprites = 0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (mappingChars.contains(level[row][col])) {
                    sprites++;
                }
            }
        }

        return sprites / (width*height);
    }

    private int getControllerResult(AbstractPlayer controller, StateObservation state, int maxSteps) {
        int steps;
        for (steps = 0; steps < maxSteps; steps++) {
            if (state.isGameOver()) {
                break;
            }
            state.advance(controller.act(state, null));
        }
        return steps;
    }

    private void initControllers() {
        doNothingController = new controllers.singlePlayer.doNothing.
                Agent(getStateObservation().copy(), null);
        oneStepLookAheadController = new controllers.singlePlayer.sampleonesteplookahead.
                Agent(getStateObservation().copy(), null);
        MaastController = new MaastCTS2.Agent(getStateObservation().copy(), new ElapsedCpuTimer());
    }

    private StateObservation getStateObservation() {
        if (stateObservation != null) {
            return stateObservation;
        }

        stateObservation = game.testLevel(getLevelString());
        return stateObservation;
    }

    public char[][] getLevel() { return level; }

    public String getLevelString() {
        return toString();
    }

    @Override
    public int compareTo(Individual that) {
        return (int)(this.fitness() - that.fitness());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                stringBuilder.append(level[row][col]);
            }
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void print() {
        System.out.println(toString());
    }
}
