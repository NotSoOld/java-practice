package ru.notsoold.cardcv;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.notsoold.cardcv.CardCvUtils.*;

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

    public List<BufferedImage> getResultPooledImages() {
        return resultPooledImages;
    }

    private int getMaxRgb2x2(int[] imageChunk) {
        return Arrays.stream(imageChunk).max().getAsInt();
    }
}
