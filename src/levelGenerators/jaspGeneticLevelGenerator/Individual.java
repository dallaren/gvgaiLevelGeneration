package levelGenerators.jaspGeneticLevelGenerator;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;

import tools.ElapsedCpuTimer;
import tools.StepController;

import java.awt.*;
import java.util.ArrayList;
import java.util.Set;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Individual implements Comparable<Individual> {
    private char[][] level;
    private boolean calculated;
    private double fitness;

    private AbstractPlayer doNothingController;
    private AbstractPlayer oneStepLookAheadController;
    private AbstractPlayer bestController;

    private StateObservation stateObservation;

    public Individual() {
        level = new char[height][width];
    }

    public void initializeRandom() {
        initControllers();

        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {
                if (random.nextDouble() < RANDOM_FILL_FACTOR) {
                    level[row][col] = getRandomCharFromLevelMapping();
                } else {
                    level[row][col] = ' ';
                }
            }
        }
    }

    //randomly mutate a single tile in the level
    //TODO add ' ' to possible chars
    public void mutate() {
        int row = random.nextInt(height);
        int col = random.nextInt(width);

        level[row][col] = getRandomCharFromLevelMapping();
        constrainLevel();
    }

    private char getRandomCharFromLevelMapping() {
        //Character[] mappingChars = (Character[]) game.getLevelMapping().keySet().toArray();
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        //mappingChars.add(' ');
        Character[] chars = mappingChars.toArray(new Character[mappingChars.size()]);

        int c = random.nextInt(chars.length);
        return chars[c];
    }

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
        //TODO change controller to be the best, not the one step look-ahead
        StepController bestController = new StepController(oneStepLookAheadController, MAX_STEP_TIME);
        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        timer.setMaxTimeMillis(maxTime);
        bestController.playGame(stateObservation.copy(), timer);

        ArrayList<Types.ACTIONS> bestSolution = bestController.getSolution();
        StateObservation bestState = bestController.getFinalState();


        StateObservation doNothingState;
        int minDoNothingSteps = Integer.MAX_VALUE;
        for (int i = 0; i < REPETITION_AMOUNT; i++) {
            StateObservation tempState = stateObservation.copy();
            int steps = getControllerSteps(doNothingController, tempState, bestSolution.size());
            if (steps < minDoNothingSteps) {
                minDoNothingSteps = steps;
                doNothingState = tempState;
            }
        }



        calculated = true;
        return 1;
    }

    private int getControllerSteps(AbstractPlayer controller, StateObservation state, int maxSteps) {
        int step;
        for (step = 0; step < maxSteps; step++) {
            if (state.isGameOver()) {
                break;
            }
            state.advance(controller.act(state, null));
        }
        return step;
    }

    private void initControllers() {
        doNothingController = new controllers.singlePlayer.doNothing.
                Agent(getStateObservation().copy(), null);
        oneStepLookAheadController = new controllers.singlePlayer.sampleonesteplookahead.
                Agent(getStateObservation().copy(), null);
        bestController = null; //TODO find a good controller
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
