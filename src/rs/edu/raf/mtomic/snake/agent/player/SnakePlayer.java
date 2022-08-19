package rs.edu.raf.mtomic.snake.agent.player;

import javafx.util.Pair;
import rs.edu.raf.mtomic.snake.Direction;
import rs.edu.raf.mtomic.snake.GameState;

import java.util.ArrayList;
import java.util.List;

import static rs.edu.raf.mtomic.snake.FieldState.BLOCKED;
import static rs.edu.raf.mtomic.snake.FieldState.PELLET;
import static rs.edu.raf.mtomic.snake.MathUtils.nextLeftGridX;
import static rs.edu.raf.mtomic.snake.MathUtils.nextRightGridX;

public class SnakePlayer extends Player implements Comparable<SnakePlayer>{

    private List<List<Double>> weights;
    private List<List<Double>> biases;
   /// private int score;
    private double score;
    private int inputNodes;
    private int hiddenLayers;
    private int hiddenNodes;
    private int outputNodes;
    private String lastMove;
    private int numOfMoves;
    private int eatenFruit;

    public SnakePlayer(GameState gameState) {
        super(gameState);
    }

    public SnakePlayer(GameState gameState, int inputNodes, int outputNodes, int hiddenLayers, int hiddenNodes, List<List<Double>> weights, List<List<Double>> biases) {
        super(gameState);
        this.weights = weights;
        this.inputNodes = inputNodes;
        this.hiddenLayers = hiddenLayers;
        this.hiddenNodes = hiddenNodes;
        this.outputNodes = outputNodes;
        this.biases = biases;
        this.lastMove = "NO";
    }

    int max_moves_without_fruit=200;

    @Override
    protected Runnable generateNextMove() {

        if(this.numOfMoves>max_moves_without_fruit){
            return this::goUp;
        }

        NeuralNetwork brain = new NeuralNetwork(this.inputNodes, this.hiddenLayers, this.hiddenNodes, this.outputNodes, this.weights, this.biases);

        ///brain.setActivationFunction(new NeuralNetwork.ActivationFunction(Mat.ReLu,Mat.ReLu));


        // x direction of the snake
        int posX = this.getGridX();
        // y direction of the Snake
        int posY = this.getGridY();
        // pellet postiton X
        int fruitPosX = this.gameState.getPelletPosition()[0];
        // pellet postiton Y
        int fruitPosY = this.gameState.getPelletPosition()[1];



        /// Is there a part of snake?
        int snakeLeft = 0, snakeRight = 0, snakeUp = 0, snakeDown = 0;
        int positionsSize = this.getUsedGridPositions().size();
        Pair<Integer, Integer> prevTail = new Pair<>(this.getGridY(), this.getGridX());
        Pair<Integer, Integer> tail = null;
        int count = 0;

        for(Pair<Integer, Integer> pos : this.getUsedGridPositions()) {

            if(count == positionsSize - 2)
                prevTail = pos;
            if(count == positionsSize - 1)
                tail = pos;
            count++;
        }

        /// Snake direction
        int dirD = 0, dirR = 0, dirU = 0, dirL = 0;
        if(this.getCurrentDirection().equals(Direction.LEFT))
            dirL = 1;
        else if(this.getCurrentDirection().equals(Direction.DOWN))
            dirD = 1;
        else if(this.getCurrentDirection().equals(Direction.UP))
            dirU = 1;
        else if(this.getCurrentDirection().equals(Direction.RIGHT))
            dirR = 1;

        /// Tail direction
        int taleDirD = 0, taleDirR = 0, taleDirU = 0, taleDirL = 0;
        if(positionsSize > 0) {
            if (tail.getKey() == prevTail.getKey() && tail.getValue() < prevTail.getValue())
                taleDirD = 1;
            else if (tail.getKey() == prevTail.getKey() && tail.getValue() > prevTail.getValue())
                taleDirU = 1;
            else if (tail.getKey() < prevTail.getKey() && tail.getValue() == prevTail.getValue())
                taleDirR = 1;
            else if (tail.getKey() > prevTail.getKey() && tail.getValue() == prevTail.getValue())
                taleDirL = 1;
        }

        ArrayList<Double>list_input=new ArrayList<>();

        list_input.add((double)dirU);
        list_input.add((double)dirD);
        list_input.add((double)dirL);
        list_input.add((double)dirR);
        list_input.add((double)taleDirU);
        list_input.add((double)taleDirD);
        list_input.add((double)taleDirL);
        list_input.add((double)taleDirR);

        int dirs=8;
        int[] dx={1,1,1,0,0,-1,-1,-1};
        int[] dy={-1,0,1,1,-1,-1,0,1};

        for(int i=0;i<dirs;i++){

            double pom=get_pellet_dir(posX,posY,dx[i],dy[i]);
            list_input.add(pom);

            pom=get_tail_dir(posX,posY,dx[i],dy[i]);
            list_input.add(pom);

            pom=get_wall_dir(posX,posY,dx[i],dy[i]);
            pom=normalize(pom,0,29);
            list_input.add(pom);

        }

        double[] inputs=new double[list_input.size()];
        for(int i=0;i<list_input.size();i++){
            inputs[i]=list_input.get(i);
        }

        /*System.out.println("INPUTS " + inputs.length);
        for(int i = 0; i<inputs.length; i++)
           System.out.println(inputs[i]);
        System.out.println("END IN");*/

        /// ORDER - UP - 0 LEFT - 1 DOWN - 2 RIGHT - 3
        double[] outputs = brain.process(inputs);

        this.numOfMoves++;

        int max = 0;

        for(int i = 0 ; i<outputs.length; i++){
             //System.out.println(Double.toString(outputs[i]) + " " + Double.toString(outputs2[i]) );
            ///System.out.println(Double.toString(outputs[i]));
            if(outputs[i] > outputs[max])
                max = i;
        }

        if(max == 0) {/// up

            if(this.gameState.getFields()[posX][posY - 1].equals(PELLET)) {
                this.score += 2;
            }

            return this::goUp;
        }
        else if(max == 1) {/// left

            if(this.gameState.getFields()[posX-1][posY].equals(PELLET)) {
                this.score += 2;
            }

            return this::goLeft;
        }
        else if(max == 2) {/// down

            if(this.gameState.getFields()[posX][posY + 1].equals(PELLET)) {
                this.score += 2;
            }

            return this::goDown;
        }
        else {/// right

            if(this.gameState.getFields()[posX+1][posY].equals(PELLET)) {
                this.score += 2;
            }

            return this::goRight;
        }

    }

    double get_pellet_dir(int x,int y,int dx,int dy){

        while(1==1){

            x+=dx;
            y+=dy;
            if(x<1 || y<1 || x>26 || y>29)return 0;

            if(gameState.getFields()[x][y].equals(PELLET))return 1;
        }

    }
    double get_tail_dir(int x,int y,int dx,int dy){

        while(1==1){

            x+=dx;
            y+=dy;
            if(x<1 || y<1 || x>26 || y>29)return 0;

            if(gameState.getFields()[x][y].equals(BLOCKED))return 1;
        }

    }
    double get_wall_dir(int x,int y,int dx,int dy){

        double ret=0;
        while(1==1){

            x+=dx;
            y+=dy;
            ret+=1;
            if(x<1 || y<1 || x>26 || y>29)break;

        }

        return ret;
    }


    private double normalize(double val,double minn,double maxx){
        return (val-minn)/(maxx-minn);
    }

    private double norm(double p, int low, int high){
        return (p - low)/(high - low);
    }

    private int manhattanDis(int x1, int y1, int x2, int y2){
        int xAbs=Math.abs(x1-x2);
        int yAbs=Math.abs(y1-y2);
        return xAbs+yAbs;
    }

    private int euclidianDis(int x1, int y1, int x2, int y2) {
        int xAbs = Math.abs(x1 - x2);
        int yAbs = Math.abs(y1 - y2);

        return (int) Math.sqrt(xAbs * xAbs + yAbs * yAbs);
    }

    public double fitness(){
        this.numOfMoves = this.numOfMoves/2;
        return numOfMoves + (Math.pow(2, eatenFruit) + Math.pow(eatenFruit, 2.1)*500) - (Math.pow(eatenFruit, 1.2)*Math.pow(0.25*numOfMoves, 1.3));
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public List<List<Double>> getWeights() {
        return weights;
    }

    public List<List<Double>> getBiases() {
        return biases;
    }

    public int getNumOfMoves() {
        return numOfMoves;
    }

    public int getEatenFruit() {
        return eatenFruit;
    }

    public void setEatenFruit(int eatenFruit) {
        this.eatenFruit = eatenFruit;
    }

    @Override
    public int compareTo(SnakePlayer o) {
        if(o.score > this.score)
            return 1;
        else if(o.score < this.score)
            return -1;
        else
            return 0;
    }
}
