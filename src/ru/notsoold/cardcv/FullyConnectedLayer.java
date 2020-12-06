package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class FullyConnectedLayer {

    private double[][] weights;

    public FullyConnectedLayer(int weightsSize) {
        this.weights = new double[weightsSize][52];
        for (int i = 0; i < weightsSize; i++) {
            for (int j = 0; j < 52; j++) {
                weights[i][j] = ThreadLocalRandom.current().nextDouble() / 52;
            }
        }
    }

    public double[] forward(List<BufferedImage> pooledImages) {
        return softmax(propagateForward(normalizeInputImages(flattenInputImages(pooledImages))));
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
        }
        return dotProductResult;
    }

    private double[] softmax(double[] x) {
        double[] expX = Arrays.stream(x).map(Math::exp).toArray();
        double expsSum = Arrays.stream(expX).sum();
        return Arrays.stream(expX).map(expXi -> expXi / expsSum).toArray();
    }

}
