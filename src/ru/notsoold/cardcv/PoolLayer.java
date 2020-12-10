package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Dmitriy "NotSoOld" Somov (1000lop@gmail.com)
 * at 7 Dec 2020. All rights reserved.
 */
public class PoolLayer {

    private List<BufferedImage> originals;
    private List<BufferedImage> resultPooledImages;

    public PoolLayer(List<BufferedImage> originals) {
        this.originals = originals;
    }

    public List<BufferedImage> forward() {
        resultPooledImages = new ArrayList<>();
        for (BufferedImage original: originals) {
            BufferedImage resultPooledImage = new BufferedImage(original.getWidth() / 2, original.getHeight() / 2, BufferedImage.TYPE_INT_RGB);
            for (int i = 0; i < original.getWidth() - 1; i += 2) {
                for (int j = 0; j < original.getHeight() - 1; j += 2) {
                    resultPooledImage.setRGB(i / 2, j / 2, getMaxRgb2x2(original.getRGB(i, j, 2, 2, null, 0, 2)));
                }
            }
            resultPooledImages.add(resultPooledImage);
        }
        return resultPooledImages;
    }

    public double[][][] backprop(double[][][] fclGradient, ConvolutionNeuralNetworkContainer container) {
        double[][][] ret = new double[container.getFiltersCnt()][originals.get(0).getWidth()][originals.get(0).getHeight()];
        for (int filterIdx = 0; filterIdx < originals.size(); filterIdx++) {
            BufferedImage original = originals.get(filterIdx);
            for (int w = 0; w < original.getWidth() - 1; w += 2) {
                for (int h = 0; h < original.getHeight() - 1; h += 2) {
                    int maxRgb = getMaxRgb2x2(original.getRGB(w, h, 2, 2, null, 0, 2));
                    for (int i = 0; i < 2; i++) {
                        for (int j = 0; j < 2; j++) {
                            if (original.getRGB(i, j) == maxRgb) {
                                ret[filterIdx][w][h] = fclGradient[filterIdx][w / 2][h / 2];
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }

    public List<BufferedImage> getResultPooledImages() { return resultPooledImages; }

    private int getMaxRgb2x2(int[] imageChunk) { return Arrays.stream(imageChunk).max().getAsInt(); }
}
