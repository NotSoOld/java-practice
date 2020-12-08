package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class ConvolutionLayer implements Serializable {
    private static final long serialVersionUID = 187604289977964733L;

    private List<double[]> filters;
    private transient BufferedImage original;
    private transient List<BufferedImage> convolutionResult;

    public ConvolutionLayer(ConvolutionNeuralNetworkContainer container) {
        generateFilters(container.getFiltersCnt());
    }

    public List<BufferedImage> forward(BufferedImage originalCardImage) {
        original = originalCardImage;
        convolutionResult = new ArrayList<>();
        for (double[] kernel: filters) {
            BufferedImage convolutedImage = new BufferedImage(originalCardImage.getWidth(), originalCardImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int i = 1; i < originalCardImage.getWidth() - 1; i++) {
                for (int j = 1; j < originalCardImage.getHeight() - 1; j++) {
                    int convResult = Math.abs(convolute(kernel, originalCardImage.getRGB(i - 1, j - 1, 3, 3, null, 0, 3))) / 3;
                    convolutedImage.setRGB(i, j, (convResult + (convResult << 8) + (convResult << 16)));
                }
            }
            convolutionResult.add(convolutedImage);
        }
        return convolutionResult;
    }

    public void backprop(double[][][] poolLayerGradient, double learnRate) {
        for (int i = 1; i < original.getWidth() - 1; i++) {
            for (int j = 1; j < original.getHeight() - 1; j++) {
                int[] imgChunk = original.getRGB(i - 1, j - 1, 3, 3, null, 0, 3);
                for (int filterIdx = 0; filterIdx < filters.size(); filterIdx++) {
                    double[] filter = filters.get(filterIdx);
                    for (int w = 0; w < 3; w++) {
                        for (int h = 0; h < 3; h++) {
                            for (int x = 0; x < filter.length; x++) {
                                filter[x] -= learnRate * poolLayerGradient[filterIdx][(i - 1) + w][(j - 1) + h] * ((imgChunk[w * 3 + h] & 0xffffff) / 16777216.0);
                            }
                        }
                    }
                }
            }
        }
    }

    public List<BufferedImage> getConvolutionResult() {
        return convolutionResult;
    }

    private int convolute(double[] kernel, int[] imageChunk) {
        int ret = 0;
        for (int i = 0; i < imageChunk.length; i++) {
            ret += rgbToHardBW(imageChunk[i]) * kernel[i];
        }
        return ret;
    }

    /**
     * Generates integer filters based on gaussian normal distribution.
     * @param quantity number of filters to be generated and used later for the convolution
     */
    private void generateFilters(int quantity) {
        this.filters = new ArrayList<>();
        this.filters.add(new double[] { 0.5, 1, 0.5, 0, 0, 0, -0.5, -1, -0.5 });
        this.filters.add(new double[] { -0.5, 0, 0.5, -1, 0, 1, -0.5, 0, 0.5 });
        for (int i = 2; i < quantity; i++) {
            this.filters.add(new double[9]);
            for (int j = 0; j < 9; j++) {
                this.filters.get(i)[j] = ThreadLocalRandom.current().nextGaussian() / 4;
            }
        }
    }
}
