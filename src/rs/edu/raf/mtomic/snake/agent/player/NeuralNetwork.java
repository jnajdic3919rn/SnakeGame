package rs.edu.raf.mtomic.snake.agent.player;
import rs.edu.raf.mtomic.snake.agent.player.Mat.MatFunc;

import java.util.List;


public class NeuralNetwork implements Cloneable{

    public final int inputNodes;
    public final int hiddenLayers;
    public final int hiddenNodes;
    public final int outputNodes;

    public Mat[] weights;
    public final Mat[] biases;

    private ActivationFunction activationFunction;
    private ActivationFunction activationFunctionOutput;

    private double learningRate;

    public NeuralNetwork(int inputNodes, int hiddenLayers, int hiddenNodes, int outputNodes, List<List<Double>> myWeights, List<List<Double>> myBiases)
    {
        this.inputNodes = inputNodes;
        this.hiddenLayers = hiddenLayers;
        this.hiddenNodes = hiddenNodes;
        this.outputNodes = outputNodes;


        weights = new Mat[hiddenLayers + 1];
        for(int i = 0; i < hiddenLayers + 1; i++)
        {
            if(i == 0)
            {
                weights[i] = new Mat(hiddenNodes, inputNodes, i, myWeights);
            }
            else if(i == hiddenLayers)
            {
                weights[i] = new Mat(outputNodes, hiddenNodes, i, myWeights);
            }
            else
            {
                weights[i] = new Mat(hiddenNodes, hiddenNodes, i, myWeights);
            }

        }

        biases = new Mat[hiddenLayers + 1];
        for(int i = 0; i < hiddenLayers + 1; i++)
        {
            if(i == hiddenLayers)
            {
                biases[i] = new Mat(outputNodes, 1, i, myBiases);
            }
            else
            {
                biases[i] = new Mat(hiddenNodes, 1, i, myBiases);
            }

        }

        learningRate = 0.01;
        activationFunction = new ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE);
        activationFunctionOutput = new ActivationFunction(Mat.SIGMOID, Mat.SIGMOID_DERIVATIVE);

        ///activationFunction = new ActivationFunction(Mat.ReLu, Mat.SIGMOID_DERIVATIVE);
        ///activationFunctionOutput = new ActivationFunction(Mat.ReLu, Mat.SIGMOID_DERIVATIVE);
    }

    public NeuralNetwork(NeuralNetwork copy)
    {
        this.inputNodes = copy.inputNodes;
        this.hiddenLayers = copy.hiddenLayers;
        this.hiddenNodes = copy.hiddenNodes;
        this.outputNodes = copy.outputNodes;

        weights = new Mat[copy.weights.length];
        for(int i = 0; i < weights.length; i++)
        {
            weights[i] = copy.weights[i].clone();
        }

        biases = new Mat[copy.biases.length];
        for(int i = 0; i < biases.length; i++)
        {
            biases[i] = copy.biases[i].clone();
        }
        learningRate = copy.learningRate;
        activationFunction = copy.activationFunction;
        activationFunctionOutput=copy.activationFunctionOutput;
    }

    public double[] process(double[] inputArray)
    {
        if(inputArray.length != inputNodes) throw new IllegalArgumentException("Input must have " + inputNodes + " element" + (inputNodes == 1 ? "" : "s"));

        Mat input = Mat.fromArray(inputArray);

        for(int i = 0; i < hiddenLayers + 1; i++)
        {

            /*System.out.println("\nMAT INTERATION");
            System.out.println(weights[i].rows);
            System.out.println(weights[i].cols);
            System.out.println(weights[i].data);
            System.out.println("GOTOV WEIGHTS");
            System.out.println(biases[i]);
            System.out.println("GOTOV BIAS");
            System.out.println(input);
            System.out.println("GOTOV input1");*/


            if(i == hiddenLayers) {
                input = weights[i].mult(input).add(biases[i]);
                ///System.out.println(input);
                ///System.out.println("GOTOV input2");
                input=input.map(activationFunctionOutput.function);
            }
            else
                input = weights[i].mult(input).add(biases[i]).map(activationFunction.function);


            ///input = weights[i].mult(input).add(biases[i]);

            /*System.out.println(input);
            System.out.println("GOTOV input3");*/
        }

        return input.toArray();
    }

    public void train(double[] inputArray, double[] correct)
    {
        if(inputArray.length != inputNodes) throw new IllegalArgumentException("Input must have " + inputNodes + " element" + (inputNodes == 1 ? "" : "s"));
        if(correct.length != outputNodes) throw new IllegalArgumentException("Output must have " + outputNodes + " element" + (outputNodes == 1 ? "" : "s"));

        Mat input = Mat.fromArray(inputArray);
        Mat[] layers = new Mat[hiddenLayers + 2];
        layers[0] = input;

        for(int i = 1; i < hiddenLayers + 2; i++)
        {
            input = weights[i - 1].mult(input).add(biases[i - 1]).map(activationFunction.function);
            layers[i] = input;
        }

        Mat target = Mat.fromArray(correct);
        for(int i = hiddenLayers + 1; i > 0; i--)
        {
            // Calculate Error
            Mat error = target.subtract(layers[i]);

            // Calculate Gradient
            Mat gradient = layers[i].map(activationFunction.derivative);
            gradient = gradient.elementMult(error);
            gradient = gradient.mult(learningRate);

            // Calculate Delta
            Mat delta = gradient.mult(layers[i - 1].transpose());

            // Adjust weights and biases
            biases[i - 1] = biases[i - 1].add(gradient);
            weights[i - 1] = weights[i - 1].add(delta);

            // Reset target for next loop
            target = weights[i - 1].transpose().mult(error).add(layers[i - 1]);
        }
    }

    public double getLearningRate()
    {
        return learningRate;
    }

    public NeuralNetwork setLearningRate(double learningRate)
    {
        this.learningRate = learningRate;
        return this;
    }

    public ActivationFunction getActivationFunction()
    {
        return activationFunction;
    }

    public NeuralNetwork setActivationFunction(ActivationFunction activationFunction)
    {
        this.activationFunction = activationFunction;
        return this;
    }

    public void setActivationFunctionOutput(ActivationFunction activationFunctionOutput) {
        this.activationFunctionOutput = activationFunctionOutput;
    }

    public NeuralNetwork quoteBreedUnquote(final NeuralNetwork other)
    {
        if(inputNodes != other.inputNodes || hiddenLayers != other.hiddenLayers || hiddenNodes != other.hiddenNodes || outputNodes != other.outputNodes)
        {
            throw new IllegalArgumentException("These neural networks aren't compatible");
        }

        NeuralNetwork nn = clone();
        for(int i = 0; i < hiddenLayers + 1; i++)
        {
            Mat weight = nn.weights[i];

            final int indx = i;
            weight.map(new MatFunc()
            {
                @Override
                public double perform(double val, int r, int c)
                {
                    return Math.random() >= 0.5 ? val : other.weights[indx].data[r][c];
                }
            });
        }
        return nn;
    }


    public NeuralNetwork clone()
    {
        return new NeuralNetwork(this);
    }

    public static class ActivationFunction
    {
        public final MatFunc function, derivative;

        public ActivationFunction(MatFunc function, MatFunc derivative)
        {
            this.function = function;
            this.derivative = derivative;
        }
    }

}
