package levelGenerators.jaspGeneticLevelGenerator;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;

import static levelGenerators.jaspGeneticLevelGenerator.Shared.*;

public class Individual implements Comparable<Individual> {
    private char[][] level;

    public Individual(int height, int width) {
        level = new char[height][width];
    }

    public void initializeRandom() {
        throw new NotImplementedException();
    }

    //randomly mutate a single tile in the level
    public void mutate() {
        int row = random.nextInt(height);
        int col = random.nextInt(width);
        char c = getRandomCharFromLevelMapping();

        level[row][col] = c;
        constrainLevel();
    }

    private char getRandomCharFromLevelMapping() {
        Character[] mappingChars = (Character[]) game.getLevelMapping().keySet().toArray();
        int c = random.nextInt(mappingChars.length);
        return mappingChars[c];
    }

    //do a crossover around a random row or column
    public Iterable<Individual> crossOver(Individual partner) {
        ArrayList<Individual> children = new ArrayList<>(2);
        Individual child1 = new Individual(height, width);
        Individual child2 = new Individual(height, width);

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

    //make sure the level well-formed
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

    public int fitness() {
        throw new NotImplementedException();
    }

    public char[][] getLevel() { return level; }

    @Override
    public int compareTo(Individual that) {
        return this.fitness() - that.fitness();
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

        //trim off the trailing newline
        return stringBuilder.toString().trim();
    }
}
