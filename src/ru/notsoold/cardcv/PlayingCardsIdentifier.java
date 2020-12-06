package ru.notsoold.cardcv;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class PlayingCardsIdentifier {

    private static final int CONVOLUTION_LAYER_FILTERS_QUANTITY = 4;
    /** Depends on card size defined in CardsCutter (70 x 100),
     * the number of consecutively used PoolLayers (each of these divides card size by 2)
     * and the number of filters used in ConvolutionLayer ({@link PlayingCardsIdentifier#CONVOLUTION_LAYER_FILTERS_QUANTITY}). */
    private static final int FULLY_CONNECTED_LAYER_WEIGHTS_SIZE = 17 * 25 * CONVOLUTION_LAYER_FILTERS_QUANTITY;

    private ConvolutionLayer convolutionLayer = new ConvolutionLayer(CONVOLUTION_LAYER_FILTERS_QUANTITY);
    private PoolLayer poolLayer1, poolLayer2;
    private FullyConnectedLayer fullyConnectedLayer = new FullyConnectedLayer(FULLY_CONNECTED_LAYER_WEIGHTS_SIZE);

    public static void main(String[] args) throws Exception {
        new PlayingCardsIdentifier().main();
    }

    public void main() throws Exception {
        List<Path> trainingPlayingCards = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).filter(Files::isRegularFile).collect(Collectors.toList());
        Collections.shuffle(trainingPlayingCards);
        trainingPlayingCards.forEach(imageFile -> {
            try {
                BufferedImage originalCardImage = ImageIO.read(imageFile.toFile());
                List<BufferedImage> imageConvolutedByDifferentFilters = convolutionLayer.forward(originalCardImage);
                poolLayer1 = new PoolLayer(imageConvolutedByDifferentFilters);
                poolLayer2 = new PoolLayer(poolLayer1.forward());
                double[] softmaxResults = fullyConnectedLayer.forward(poolLayer2.forward());
                int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
                System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                System.out.println("; it was " + imageFile.getParent().getFileName().toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
