package rs.edu.raf.mtomic.snake;

import rs.edu.raf.mtomic.snake.agent.player.Mat;
import rs.edu.raf.mtomic.snake.agent.player.SnakePlayer;

import java.util.*;

public class MyRunSimul {

    public static Random r = new Random(70);

    public static void main(String[] args) throws InterruptedException {


        /// meta data
        int numInput = 32, numOutput = 4, numOfLayers = 2, numNodesInLayer = 20;
        int numOfGen = 500;
        int population = 500;
        int cut = 10;
        int crossover = population/2;

        List<SnakePlayer> players = initPopulation(population, cut, numInput, numOutput, numOfLayers, numNodesInLayer);
        // Ovo je u redu
        for(List<Double> w : players.get(0).getWeights()){
            System.out.println(w);
        }

        for (SnakePlayer sp : players) {
            SnakeLike snakeLike = new SnakeLike(sp);
            snakeLike.join();
            sp.setEatenFruit(snakeLike.getTotalPoints());
            sp.setScore(sp.fitness());
        }

        Collections.sort(players);

        System.out.println("SCOREEEEE");
        for(SnakePlayer sp : players){
            System.out.println(sp.getScore() + " eaten " + sp.getEatenFruit());
        }
        System.out.println("SCOREEEEE");



        for(int gen = 0; gen < numOfGen; gen++) {

            /// cut
            removeElements(players, population-cut);

            List<Pair<Integer, Integer>> pairsForCross = new ArrayList<>();
            for (int i = 0; i < crossover / 2; i++) {
                /**
                int pair1 = tournamentSelection(players, 4);
                int pair2 = tournamentSelection(players, 4);
                */
                int pair1 = rouletteWheel(players, players.size());
                int pair2 = rouletteWheel(players, players.size());
                pairsForCross.add(new Pair(pair1, pair2));
            }

            /// crossover
            List<SnakePlayer> newPlayers = new ArrayList<>();
            double beta = 0.5;
            for (Pair<Integer, Integer> pair : pairsForCross) {
                List<List<Double>> newPlayer1 = cross(players, pair.getL(), pair.getR(), beta);
                List<List<Double>> newPlayer2 = cross(players, pair.getR(), pair.getL(), beta);
                List<List<Double>> newPlayer1Bias = crossBias(players, pair.getL(), pair.getR(), beta);
                List<List<Double>> newPlayer2Bias = crossBias(players, pair.getR(), pair.getL(), beta);
                newPlayers.add(new SnakePlayer(null, numInput, numOutput, numOfLayers, numNodesInLayer, newPlayer1, newPlayer1Bias));
                newPlayers.add(new SnakePlayer(null, numInput, numOutput, numOfLayers, numNodesInLayer, newPlayer2, newPlayer2Bias));
            }

            /// mutation
            double prob = 0.08;
            /// Random r = new Random();
            for (SnakePlayer player : newPlayers) {
               /// if (0.45 > r.nextDouble()) {
                    for (int i = 0; i < player.getWeights().size(); i++) {
                        for (int j = 0; j < player.getWeights().get(i).size(); j++) {
                            if (prob > r.nextDouble())
                                player.getWeights().get(i).set(j, r.nextDouble());
                        }
                    }
               /// }
            }

            /// calculate score
            for(SnakePlayer snakePlayer : newPlayers) {
                SnakeLike snakeLike = new SnakeLike(snakePlayer);
                snakeLike.join();
                snakePlayer.setEatenFruit(snakeLike.getTotalPoints());
                ///snakePlayer.setScore(snakePlayer.fitness());
                snakePlayer.setScore(snakePlayer.fitness());
            }

            players.addAll(newPlayers);
            Collections.sort(players);


                /*System.out.println("SCORE");
                for(SnakePlayer sp : players)
                    System.out.println(sp.getScore());
                System.out.println("SCORE END");*/


            /// cut to population 50
            removeElements(players, population);

            System.out.println("Best  after generations num " + gen + " " + players.get(0).getEatenFruit());
            if(players.get(0).getEatenFruit()>=3){
                ///SnakeLike.RENDER=true;
                ///SnakeLike snakeLike = new SnakeLike(players.get(0));
                ///snakeLike.join();
                ///SnakeLike.RENDER=false;
            }

        }

        /*SnakeLike.RENDER=true;
        SnakeLike snakeLike = new SnakeLike(players.get(0));
        snakeLike.join();*/

    }

    private static void removeElements(List<SnakePlayer> players, int num){
        Iterator<SnakePlayer> spIterator = players.iterator();
        int count = 0;
        while (spIterator.hasNext()) {
            SnakePlayer sp = spIterator.next();
            count++;
            if (count > num)
                spIterator.remove();
        }
    }

    private static List<List<Double>> crossBias(List<SnakePlayer> players, Integer lp, Integer rp, double beta) {
        SnakePlayer firstParent = players.get(lp);
        SnakePlayer secondParent = players.get(rp);

        List<List<Double>> biases = new ArrayList<>();
        for(int i = 0; i<firstParent.getBiases().size(); i++){
            List<Double> list = new ArrayList<>();
            for(int j = 0; j<firstParent.getBiases().get(i).size(); j++){
                beta=r.nextDouble();
                double w = beta*firstParent.getBiases().get(i).get(j) + (1-beta)*secondParent.getBiases().get(i).get(j);
                /// System.out.println(firstParent.getBiases().get(i).get(j) + "   " + secondParent.getBiases().get(i).get(j) + "   " + w);
                list.add(w);
            }
            biases.add(list);
        }
        return biases;
    }

    private static List<List<Double>> cross(List<SnakePlayer> players, Integer lp, Integer rp, double beta) {
        SnakePlayer firstParent = players.get(lp);
        SnakePlayer secondParent = players.get(rp);

        List<List<Double>> weights = new ArrayList<>();
        for(int i = 0; i<firstParent.getWeights().size(); i++){
            List<Double> list = new ArrayList<>();
            for(int j = 0; j<firstParent.getWeights().get(i).size(); j++){
                beta=r.nextDouble();
                double w = beta*firstParent.getWeights().get(i).get(j) + (1-beta)*secondParent.getWeights().get(i).get(j);
                /// System.out.println(firstParent.getWeights().get(i).get(j) + "   " + secondParent.getWeights().get(i).get(j) + "   " + w);
                list.add(w);
            }
            weights.add(list);
        }
        return weights;
    }

    private static List<SnakePlayer> initPopulation(int population, int cut, int numInput, int numOutput, int numOfLayers, int numNodesInLayer) {
            List<SnakePlayer> list = new ArrayList<>();
            for(int i = 0; i<population; i++){
                list.add(new SnakePlayer(null, numInput, numOutput, numOfLayers, numNodesInLayer, getWeights(numInput, numOutput, numOfLayers, numNodesInLayer), getBiases(numOutput, numOfLayers, numNodesInLayer)));
            }
            return list;
    }

    private static List<List<Double>> getWeights(int numInput, int numOutput, int numOfLayers, int numNodesInLayer) {
        List<List<Double>> weights = new ArrayList<>();
        /// Random r = new Random();

        for(int i = 0; i<numOfLayers + 1; i++){
            List<Double> list = new ArrayList<>();
            if(i == 0){
                for(int j = 0; j<numNodesInLayer; j++) {
                    for (int k = 0; k < numInput; k++) {
                        double w = r.nextDouble();
                        list.add(w);
                    }
                }
                weights.add(list);
            }
            else if(i == numOfLayers){
                for(int j = 0; j<numOutput; j++) {
                    for (int k = 0; k < numNodesInLayer; k++) {
                        double w = r.nextDouble();
                        list.add(w);
                    }
                }
                weights.add(list);
            }
            else{
                for(int j = 0; j<numNodesInLayer; j++) {
                    for (int k = 0; k < numNodesInLayer; k++) {
                        double w = r.nextDouble();
                        list.add(w);
                    }
                }
                weights.add(list);
            }
        }
        return weights;
    }

    private static List<List<Double>> getBiases(int numOutput, int numOfLayers, int numNodesInLayer){
        List<List<Double>> biases = new ArrayList<>();
        /// Random r = new Random();

        for(int i = 0; i<numOfLayers + 1; i++){
            List<Double> list = new ArrayList<>();
            if(i == numOfLayers){
                for(int j = 0; j<numOutput; j++){
                    list.add(r.nextDouble());
                }
                biases.add(list);
            }
            else if(i == 0){
                for(int j = 0; j<numNodesInLayer; j++){
                    ///list.add(0.0);
                    list.add(r.nextDouble());
                }
                biases.add(list);
            }
            else{
                for(int j = 0; j<numNodesInLayer; j++){
                    list.add(r.nextDouble());
                }
                biases.add(list);
            }
        }

        return biases;

    }

    static int rouletteWheel(List<SnakePlayer> players, int size){

        /// Random r = new Random();
        double sum_f = 0.0;

        for (int i=0; i < size; i++){
            sum_f += players.get(i).getScore();
        }

        // Random num in interval from 0.0 to fintess sum
        double random_num = sum_f * r.nextDouble();

        int index=0;
        double partial_sum = players.get(0).getScore();

        while (random_num > partial_sum)
        {
            index++;
            partial_sum += players.get(index).getScore();
        }

        return index;
    }

    static int tournamentSelection(List<SnakePlayer> players, int size) {
        /// Random r = new Random();
        List<SnakePlayer> chosen = new ArrayList<>();

        for(int i = 0; i<size; i++){
            int ind = r.nextInt(players.size());
            chosen.add(players.get(ind));
            ///System.out.println(ind);
        }

        int chosenInd = 0;
        for(int i = 1; i<size; i++){
            if(chosen.get(i).getScore() > chosen.get(chosenInd).getScore())
                chosenInd = i;
        }
        return chosenInd;
    }

}
