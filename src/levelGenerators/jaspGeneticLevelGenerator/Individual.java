package levelGenerators.jaspGeneticLevelGenerator;

import core.game.GameDescription;
import core.game.StateObservation;
import core.player.AbstractPlayer;
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

    public void initializeRandom() {
        initializeLevel();

        for (int i = 0; i < INITIAL_MUTATION_AMOUNT; i++) {
            mutate();
        }

        //initializeControllers();
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
        int x = random.nextInt(width - 2*borderThickness) + borderThickness;
        int y = random.nextInt(height - 2*borderThickness) + borderThickness;

        return new Point(x, y);
    }

    private char getRandomCharFromLevelMapping() {
        Set<Character> mappingChars = game.getLevelMapping().keySet();
        Character[] chars = mappingChars.toArray(new Character[mappingChars.size()]);

        int c = random.nextInt(chars.length);
        return chars[c];
    }

    //do a 1-point crossover with a partner, spawning two children
    public ArrayList<Individual> crossover(Individual partner) {
        ArrayList<Individual> children = new ArrayList<>(2);
        Individual child1 = new Individual();
        Individual child2 = new Individual();

        Point crossoverPoint = getRandomTile();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if ( (row < crossoverPoint.y) || (row == crossoverPoint.y && col < crossoverPoint.x) ) {
                    //on one side of the crossover point
                    child1.level[row][col] = this.level[row][col];
                    child2.level[row][col] = partner.level[row][col];
                } else {
                    //on the other side of the crossover point
                    child2.level[row][col] = this.level[row][col];
                    child1.level[row][col] = partner.level[row][col];
                }
            }
        }

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

        initializeControllers();

        StepController bestController = getBestControllerResults();

        ArrayList<Types.ACTIONS> bestSolution = bestController.getSolution();
        StateObservation bestState = bestController.getFinalState();

        StateObservation doNothingState = getDoNothingControllerState(bestSolution.size());
        int doNothingSteps = doNothingState.getGameTick();

        //the best controller MUST win and the doNothing controller MUST not win
        if (bestPlayerWins(bestState) && doNothingPlayerDoesNotWin(doNothingState)) {

            double oneStepLookAheadScore = getOneStepLookAheadControllerScore(bestSolution.size());
            double scoreDifference = bestState.getGameScore() - oneStepLookAheadScore;

            fitness = 1 + (1 + scoreDifference)*coverageConstraint()*doNothingConstraint(doNothingSteps)*solutionLengthConstraint(bestSolution.size());

            System.out.println("SolutionLength:" + bestSolution.size() + " doNothingSteps:" + doNothingSteps + " coverPercentage:" + getCoverPercentage() + " bestPlayer:" + bestState.getGameWinner() + " fitness: " + fitness);
            System.out.print("scoreDiff:" + scoreDifference);
            System.out.println(" coverage:" + coverageConstraint() + " doNoth:" + doNothingConstraint(doNothingSteps) + " solution:" + solutionLengthConstraint(bestSolution.size()));
        } else {
            fitness = coverageConstraint()*terminationSetConstraint();
        }

        cleanUpControllers();

        calculated = true;
        return fitness;
    }

    private StepController getBestControllerResults() {
        StepController bestController = new StepController(MaastController, MAX_STEP_TIME);

        ElapsedCpuTimer timer = new ElapsedCpuTimer();
        timer.setMaxTimeMillis(EVALUATION_TIME);

        bestController.playGame(stateObservation.copy(), timer);

        return bestController;
    }

    private StateObservation getDoNothingControllerState(int maxSteps) {
        StateObservation controllerState = null;

        int minDoNothingSteps = Integer.MAX_VALUE;
        //play the game a number of times with the given agent
        for (int i = 0; i < REPETITION_AMOUNT; i++) {
            StateObservation tempState = stateObservation.copy();
            int steps = getControllerSteps(doNothingController, tempState, maxSteps);
            if (steps < minDoNothingSteps) {
                minDoNothingSteps = steps;
                controllerState = tempState;
            }
        }

        return controllerState;
    }

    private double getOneStepLookAheadControllerScore(int maxSteps) {
        StateObservation oneStepLookAheadState = null;
        for (int i = 0; i < REPETITION_AMOUNT; i++) {
            StateObservation tempState = stateObservation.copy();
            getControllerSteps(oneStepLookAheadController, tempState, maxSteps);
            if (oneStepLookAheadState == null || tempState.getGameScore() > oneStepLookAheadState.getGameScore()) {
                oneStepLookAheadState = tempState;
            }
        }

        return oneStepLookAheadState.getGameScore();
    }

    private double coverageConstraint() {
        double coverPercentage = getCoverPercentage();

        if (MIN_COVER_PERCENTAGE <= coverPercentage && coverPercentage <= MAX_COVER_PERCENTAGE) {
            return 1.0;
        }

        if (MAX_COVER_PERCENTAGE < coverPercentage) {
            return 1.0 - Math.sqrt(coverPercentage - MAX_COVER_PERCENTAGE);
        }

        return 0.0;
    }

    //heavily punishes, but does not exclude, levels that do not adhere to the doNothing constraint
    private double doNothingConstraint(int steps) {
        if (steps >= MIN_DO_NOTHING_STEPS) {
            return 1.0;
        } else {
            return 1.0/(MIN_DO_NOTHING_STEPS - steps);
        }
    }

    private double solutionLengthConstraint(int solutionLength) {
        if (solutionLength >= MIN_SOLUTION_LENGTH) {
            return 1.0;
        } else {
            return (double)solutionLength / MIN_SOLUTION_LENGTH;
        }
    }

    private double terminationSetConstraint() {
        ArrayList<GameDescription.TerminationData> terminationData = game.getTerminationConditions();
        HashSet<String> terminationSpriteSet = new HashSet<>();

        for (GameDescription.TerminationData terminationCondition : terminationData) {
            terminationSpriteSet.addAll(terminationCondition.sprites);
        }

        HashSet<Character> gameSpriteSet = new HashSet<>();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                char sprite = level[row][col];
                if (sprite != '.') {
                    gameSpriteSet.add(sprite);
                }
            }
        }

        int spritesInTerminationSet = terminationSpriteSet.size();
        double spritesFromTerminationSetInLevel = 0.0;
        for (Map.Entry<Character, ArrayList<String>> entry : game.getLevelMapping().entrySet()) {
            if (gameSpriteSet.contains(entry.getKey())) {
                for (String sprite : entry.getValue()) {
                    if (terminationSpriteSet.contains(sprite)) {
                        spritesFromTerminationSetInLevel++;
                        terminationSpriteSet.remove(sprite);
                    }
                }
            }
        }

        return spritesFromTerminationSetInLevel / spritesInTerminationSet;
    }

    private boolean bestPlayerWins(StateObservation bestState) {
        return bestState.getGameWinner() == Types.WINNER.PLAYER_WINS;
    }

    private boolean doNothingPlayerDoesNotWin(StateObservation doNothingState) {
        return doNothingState.getGameWinner() != Types.WINNER.PLAYER_WINS;
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

        return sprites / ((width - 2*borderThickness)*(height - 2*borderThickness));
    }

    private int getControllerSteps(AbstractPlayer controller, StateObservation state, int maxSteps) {
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

    public String getLevelString() {
        return toString();
    }

    @Override
    public Individual clone() {
        Individual clone = new Individual();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                clone.level[row][col] = this.level[row][col];
            }
        }

        return clone;
    }

    @Override
    public int compareTo(Individual that) {

        if (Math.abs(this.fitness() - that.fitness()) < EPSILON) {
            return 0;
        } else if (this.fitness() > that.fitness()) {
            return 1;
        } else {
            return -1;
        }
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
