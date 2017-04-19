package levelGenerators.jaspGeneticLevelGenerator;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import levelGenerators.constraints.CombinedConstraints;
import ontology.Types;

import tools.ElapsedCpuTimer;
import tools.StepController;

import java.awt.*;
import java.util.*;

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
    }

    //TODO make this mutate instead
    public void initializeRandom() {
        initializeLevel();

        for (int i = 0; i < INITIAL_MUTATION_AMOUNT; i++) {
            mutate();
        }

        initializeControllers();
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

    //get the mapping char for a solid sprite from the game
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

    //randomly mutate a single tile in the level by either adding a sprite
    //to a tile, swapping two tiles, or deleting a sprite from a tile
    public void mutate() {

        double prob = random.nextDouble();
        if (prob < MUTATION_ADD_PROB) {
            mutateAddSprite();
        } else if (prob < MUTATION_SWAP_PROB + MUTATION_ADD_PROB) {
            mutateSwapSprites();
        } else {
            mutateDeleteSprite();
        }

        constrainLevel();
    }

    private void mutateAddSprite() {
        Point tile = getRandomTile();
        level[tile.y][tile.x] = getRandomCharFromLevelMapping();
    }

    private void mutateSwapSprites() {
        Point firstTile = getRandomTile();
        Point secondTile = getRandomTile();

        //make sure that the second tile is different from the first tile
        while (secondTile == firstTile) {
            secondTile = getRandomTile();
        }

        char tempSprite = level[firstTile.y][firstTile.x];

        level[firstTile.y][firstTile.x] = level[secondTile.y][secondTile.x];
        level[secondTile.y][secondTile.x] = tempSprite;
    }

    private void mutateDeleteSprite() {
        Point tile = getRandomTile();
        level[tile.y][tile.x] = '.';
    }

    private Point getRandomTile() {
        int x = random.nextInt(height - 2*borderThickness) + borderThickness;
        int y = random.nextInt(height - 2*borderThickness) + borderThickness;

        return new Point(x, y);
    }

    private char getRandomCharFromLevelMapping() {
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        Character[] chars = mappingChars.toArray(new Character[mappingChars.size()]);

        int c = random.nextInt(chars.length);
        return chars[c];
    }

    //TODO make it around a point, not a row/col
    //do a crossover around a random row or column
    public ArrayList<Individual> crossOver(Individual partner) {
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
        ArrayList<Point> avatarPositions = getAvatarPositions();

        if (avatarPositions.size() == 0) {
            addAvatarToRandomTile();
        }

        if (avatarPositions.size() > 1) {
            removeExcessAvatars(avatarPositions);
        }
    }

    private ArrayList<Point> getAvatarPositions() {
        ArrayList<Point> avatarPositions = new ArrayList<>(width*height);

        //find the positions of all avatars in the level
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (level[row][col] == 'A') {
                    avatarPositions.add(new Point(col,row));
                }
            }
        }
        return avatarPositions;
    }

    private void addAvatarToRandomTile() {
        int avatarCol = random.nextInt(width - 2*borderThickness) + borderThickness;
        int avatarRow = random.nextInt(height - 2*borderThickness) + borderThickness;
        level[avatarRow][avatarCol] = 'A';
    }

    //picks a random avatar to keep and removes the others
    private void removeExcessAvatars(ArrayList<Point> avatarPositions) {
        int indexToKeep = random.nextInt(avatarPositions.size());
        Point avatarToKeep = avatarPositions.get(indexToKeep);

        for (Point avatarPosition : avatarPositions) {
            if (!avatarPosition.equals(avatarToKeep)) {
                int avatarRow = avatarPosition.y;
                int avatarCol = avatarPosition.x;
                level[avatarRow][avatarCol] = '.';
            }
        }
    }

    public double fitness() {
        if (calculated) {
            return fitness;
        }

        return calculateFitness();
    }

    private double calculateFitness() {
        //play a single game with the best controller
        //TODO slow this guy down a little bit, a tad too inhuman
        StepController bestController = new StepController(MaastController, MAX_STEP_TIME);
        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        timer.setMaxTimeMillis(EVALUATION_TIME);
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


        cleanUpControllers();

        calculated = true;
        return fitness;
    }

    private void cleanUpControllers() {
        doNothingController = null;
        oneStepLookAheadController = null;
        MaastController = null;
    }

    private double getCoverPercentage() {
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        double sprites = 0;
        char sprite;

        for (int row = borderThickness; row < (height - borderThickness); row++) {
            for (int col = borderThickness; col < (width - borderThickness); col++) {
                sprite = level[row][col];
                if (mappingChars.contains(sprite) && sprite != '.') {
                    sprites++;
                }
            }
        }

        return sprites / ((width-borderThickness)*(height-borderThickness));
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

    private void initializeControllers() {
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

    public Individual clone() {
        Individual clone = new Individual();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                clone.level[row][col] = this.level[row][col];
            }
        }

        clone.initializeControllers();
        return clone;
    }

    @Override
    //TODO make more accurate (casting issue)
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
