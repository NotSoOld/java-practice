package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static ru.notsoold.cardcv.PlayingCardsIdentifier.CONVOLUTION_LAYER_FILTERS_QUANTITY;

public class FullyConnectedLayer {

    private double[][] weights;
    private double[] biases;
    private double[] normFlattenedInput;
    private double[] totals;
    private double[] softmaxResults;

    public FullyConnectedLayer(int weightsSize) {
        this.weights = new double[weightsSize][52];
        for (int i = 0; i < weightsSize; i++) {
            for (int j = 0; j < 52; j++) {
                weights[i][j] = ThreadLocalRandom.current().nextGaussian() / 52;
            }
        }
        this.biases = new double[52];
    }

    public double[] forward(List<BufferedImage> pooledImages) {
        normFlattenedInput = normalizeInputImages(flattenInputImages(pooledImages));
        totals = propagateForward(normFlattenedInput);
        softmaxResults = softmax(totals);
        return softmaxResults;
    }

    public double[][][] backprop(int idxOfCorrectRslt, double learnRate) {
        double[] gradient = new double[52];
        gradient[idxOfCorrectRslt] = -1 / softmaxResults[idxOfCorrectRslt]; // -51
        // e^totals; size = 52
        double[] e_totals = Arrays.stream(totals).map(Math::exp).toArray();
        // Sum of e^totals
        double sum = Arrays.stream(e_totals).sum();
        // Gradients of out[i] against totals. // 52
        double[] d_out_d_t = Arrays.stream(e_totals).map(e_total -> (e_total * -e_totals[idxOfCorrectRslt]) / (sum * sum)).toArray();
        d_out_d_t[idxOfCorrectRslt] = e_totals[idxOfCorrectRslt] * (sum - e_totals[idxOfCorrectRslt]) / (sum * sum);

        // Gradients of totals against weights/biases/input
        double[] d_t_d_w = normFlattenedInput;  // FULLY_CONNECTED_LAYER_WEIGHTS_SIZE
        double d_t_d_b = 1;
        double[][] d_t_d_inputs = weights;

        // Gradients of loss against totals     // 52
        double[] d_L_d_t = Arrays.stream(d_out_d_t).map(d -> d * gradient[idxOfCorrectRslt]).toArray();
        // 52 x FULLY_CONNECTED_LAYER_WEIGHTS_SIZE
        double[][] d_L_d_w = Arrays.stream(d_t_d_w).mapToObj(d1 -> Arrays.stream(d_L_d_t).map(d2 -> d1 * d2).toArray()).toArray(double[][]::new);
        double[] d_L_d_b = Arrays.stream(d_L_d_t).map(d -> d * d_t_d_b).toArray();
        // d_L_d_inputs = d_t_d_inputs @ d_L_d_t;   length = FULLY_CONNECTED_LAYER_WEIGHTS_SIZE
        double[] d_L_d_inputs = Arrays.stream(d_t_d_inputs).map(dArr -> Arrays.stream(dArr)
                            .map(d1 -> Arrays.stream(d_L_d_t).map(d2 -> d1 * d2).sum()).sum()).mapToDouble(d -> d).toArray();

        // Update weights and biases.
        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[i].length; j++) {
                weights[i][j] -= learnRate * d_L_d_w[i][j] * 100;
            }
        }
        for (int i = 0; i < biases.length; i++) {
            biases[i] -= learnRate * d_L_d_b[i];
        }

        double[][][] d_L_d_inputsShaped = new double[CONVOLUTION_LAYER_FILTERS_QUANTITY][17][25];
        for (int i = 0; i < d_L_d_inputsShaped.length; i++) {
            for (int j = 0; j < d_L_d_inputsShaped[i].length; j++) {
                for (int k = 0; k < d_L_d_inputsShaped[i][j].length; k++) {
                    d_L_d_inputsShaped[i][j][k] = d_L_d_inputs[i * 17 * 25 + j * 25 + k];
                }
            }
        }
        return d_L_d_inputsShaped;
    }

    private int[] flattenInputImages(List<BufferedImage> inputImages) {
        int wholeImageOffset = inputImages.get(0).getWidth() * inputImages.get(0).getHeight();
        int[] result = new int[wholeImageOffset * inputImages.size()];
        for (int i = 0; i < inputImages.size(); i++) {
            BufferedImage inputImage = inputImages.get(i);
            inputImage.getRGB(0, 0, inputImage.getWidth(), inputImage.getHeight(), result, wholeImageOffset * i, inputImage.getWidth());
        }
        return result;
    }

    private double[] normalizeInputImages(int[] flattenedInput) {
        return Arrays.stream(flattenedInput).mapToDouble(rgb -> ((rgb & 0xffffff) / 16777216.0) / weights.length).toArray();
    }

    private double[] propagateForward(double[] normalizedImageInputs) {
        // imageInputs: weightsSize; weights: weightsSize x 52
        double[] dotProductResult = new double[52];
        for (int k = 0; k < 52; k++) {
            for (int i = 0; i < weights.length; i++) {
                for (int j = 0; j < weights.length; j++) {
                    dotProductResult[k] += normalizedImageInputs[i] * weights[j][k];
                }
            }
            dotProductResult[k] += biases[k];
        }
        return dotProductResult;
    }

    private double[] softmax(double[] x) {
        double[] expX = Arrays.stream(x).map(Math::exp).toArray();
        double expsSum = Arrays.stream(expX).sum();
        return Arrays.stream(expX).map(expXi -> expXi / expsSum).toArray();
    }

}
