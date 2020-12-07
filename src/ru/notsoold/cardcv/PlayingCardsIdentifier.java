package ru.notsoold.cardcv;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class PlayingCardsIdentifier {

    public static final int CONVOLUTION_LAYER_FILTERS_QUANTITY = 4;
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
        convolutionLayer.loadFilters(Arrays.asList(
            new double[] { 0.24999998650052319, 0.49999998650053584, 0.24999998650052319, -1.3499462963680682E-8, -1.3499462963680682E-8, -1.3499462963680682E-8, -0.25000001349946027, -0.5000000134994691, -0.25000001349946027 },
            new double[] { -0.24999995556415258, 4.443583534029135E-8, 0.25000004443583224, -0.49999995556416266, 4.443583534029135E-8, 0.5000000444357382, -0.24999995556415258, 4.443583534029135E-8, 0.25000004443583224 },
            new double[] { 0.2265447164308277, 0.1810084973627225, 0.2564798507043256, -0.22268678018785065, 0.15622584558083524, -0.5610998132611525, -0.043453725748682386, 0.017209813667148948, 0.019847924597848777 },
            new double[] { -0.16656780791253037, 0.1832610802500179, -0.1991671183564203, 0.026665624942783746, 0.18338728553635195, -0.04971963427098031, -0.19483672523857967, -0.18420335507318855, -0.16206899039012743 }
        ));
        List<Path> trainingPlayingCards = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).filter(Files::isRegularFile).collect(Collectors.toList());
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(trainingPlayingCards);
            trainingPlayingCards.forEach(imageFile -> {
                try {
                    BufferedImage originalCardImage = ImageIO.read(imageFile.toFile());
                    List<BufferedImage> imageConvolutedByDifferentFilters = convolutionLayer.forward(originalCardImage);
                    poolLayer1 = new PoolLayer(imageConvolutedByDifferentFilters);
                    poolLayer2 = new PoolLayer(poolLayer1.forward());
                    double[] softmaxResults = fullyConnectedLayer.forward(poolLayer2.forward());
                    int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
                    String expectedCard = imageFile.getParent().getFileName().toString();
                    if (expectedCard.equals(cardsMapping.get(maxProbabilityIndex))) {
                        System.out.println(expectedCard);
                    }
                    // System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                    // System.out.println("; it was " + expectedCard + (expectedCard.equals(cardsMapping.get(maxProbabilityIndex)) ? " - correct!" : ""));

                    double[][][] backpropResults = fullyConnectedLayer.backprop(cardsMapping.indexOf(expectedCard), 0.1);
                    backpropResults = poolLayer2.backprop(backpropResults);
                    backpropResults = poolLayer1.backprop(backpropResults);
                    convolutionLayer.backprop(backpropResults, 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            List<double[]> filters = convolutionLayer.saveFilters();
            for (double[] filter: filters) {
                System.out.print("{");
                for(double d: filter) {
                    System.out.print(", " + d);
                }
                System.out.println(" }");
            }
        }
    }
}
