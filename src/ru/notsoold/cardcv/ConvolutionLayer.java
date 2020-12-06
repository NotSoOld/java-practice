package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class ConvolutionLayer {

    private List<double[]> filters;
    private List<BufferedImage> convolutionResult;

    public ConvolutionLayer(int filtersQuantity) {
        generateFilters(filtersQuantity);
    }

    public List<BufferedImage> forward(BufferedImage originalCardImage) {
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
        this.filters.add(new double[] { 0.25, 0.5, 0.25, 0, 0, 0, -0.25, -0.5, -0.25 });
        this.filters.add(new double[] { -0.25, 0, 0.25, -0.5, 0, 0.5, -0.25, 0, 0.25 });
        for (int i = 2; i < quantity; i++) {
            this.filters.add(new double[9]);
            for (int j = 0; j < 9; j++) {
                this.filters.get(i)[j] = ThreadLocalRandom.current().nextGaussian() / 4;
            }
        }
    }

    public void loadFilters(List<double[]> filters) { this.filters = filters; }
    public List<double[]> saveFilters() { return this.filters; }
}
