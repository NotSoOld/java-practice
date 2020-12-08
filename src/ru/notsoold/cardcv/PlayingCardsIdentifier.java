package ru.notsoold.cardcv;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class PlayingCardsIdentifier implements Serializable {
    private static final long serialVersionUID = 1879428424962345L;

    private ConvolutionLayer convolutionLayer;
    private transient PoolLayer[] poolLayers;
    private FullyConnectedLayer fullyConnectedLayer;

    // CNN parameters
    private String cnnId;
    private int filtersCnt;
    private int poolLayersCnt;
    private int origImgWidth;
    private int origImgHeight;
    private int fclChoicesCnt;
    private int fclInputImgWidth;
    private int fclInputImgHeight;

    public PlayingCardsIdentifier(String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt) {
        this.cnnId = cnnId;
        this.filtersCnt = filtersCnt;
        this.poolLayersCnt = poolLayersCnt;
        this.origImgWidth = origImgWidth;
        this.origImgHeight = origImgHeight;
        this.fclChoicesCnt = fclChoicesCnt;

        this.fclInputImgWidth = (int)(origImgWidth / (Math.pow(2, poolLayersCnt)));
        this.fclInputImgHeight = (int)(origImgHeight / (Math.pow(2, poolLayersCnt)));

        this.convolutionLayer = new ConvolutionLayer(this);
        this.poolLayers = new PoolLayer[poolLayersCnt];
        this.fullyConnectedLayer = new FullyConnectedLayer(this);
    }

    public static void main(String[] args) throws Exception {
        if (Files.exists(Paths.get("PlayingCardsIdentifier.jobj"))) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream("PlayingCardsIdentifier.jobj"));
            ((PlayingCardsIdentifier)ois.readObject()).main();
        } else {
            new PlayingCardsIdentifier("PlayingCardsIdentifier", 4, 2, 70, 100, 52).main();
        }
    }

    public void main() throws Exception {
        AtomicInteger correctGuesses = new AtomicInteger();
        List<Path> trainingPlayingCards = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).filter(Files::isRegularFile).collect(Collectors.toList());
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(trainingPlayingCards);
            trainingPlayingCards.forEach(imageFile -> {
                try {
                    BufferedImage originalCardImage = ImageIO.read(imageFile.toFile());
                    List<BufferedImage> result = convolutionLayer.forward(originalCardImage);
                    for (int j = 0; j < poolLayersCnt; j++) {
                        poolLayers[j] = new PoolLayer(result);
                        result = poolLayers[j].forward();
                    }
                    double[] softmaxResults = fullyConnectedLayer.forward(result, this);
                    int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
                    String expectedCard = imageFile.getParent().getFileName().toString();
                    if (expectedCard.equals(cardsMapping.get(maxProbabilityIndex))) {
                        System.out.println(expectedCard);
                        correctGuesses.incrementAndGet();
                    }
                    // System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                    // System.out.println("; it was " + expectedCard + (expectedCard.equals(cardsMapping.get(maxProbabilityIndex)) ? " - correct!" : ""));

                    double[][][] backpropResults = fullyConnectedLayer.backprop(cardsMapping.indexOf(expectedCard), 0.1, this);
                    for (int j = poolLayersCnt - 1; j >= 0; j--) {
                        backpropResults = poolLayers[j].backprop(backpropResults, this);
                    }
                    convolutionLayer.backprop(backpropResults, 10000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("PlayingCardsIdentifier.jobj"));
            oos.writeObject(this);
            oos.flush();
            System.out.println("Wrote CNN to the filesystem; correctGuesses = " + ((double)correctGuesses.get() / trainingPlayingCards.size()) * 100);
            correctGuesses.set(0);
        }
    }

    int getFiltersCnt() { return filtersCnt; }
    int getOrigImgWidth() { return origImgWidth; }
    int getOrigImgHeight() { return origImgHeight; }
    int getPoolLayersCnt() { return poolLayersCnt; }
    int getFclChoicesCnt() { return fclChoicesCnt; }
    int getFclInputImgWidth() { return fclInputImgWidth; }
    int getFclInputImgHeight() { return fclInputImgHeight; }
}
