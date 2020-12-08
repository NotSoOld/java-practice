package ru.notsoold.cardcv;

import javafx.util.Pair;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static ru.notsoold.cardcv.CardCvUtils.*;

public class ConvolutionNeuralNetworkContainer implements Serializable, Runnable {
    private static final long serialVersionUID = 1879428424962345L;

    private ConvolutionLayer convolutionLayer;
    private transient PoolLayer[] poolLayers;
    private FullyConnectedLayer fullyConnectedLayer;

    // CNN parameters
    private String cnnId;
    private int filtersCnt;
    private int poolLayersCnt;
    private int fclChoicesCnt;
    private int fclInputImgWidth;
    private int fclInputImgHeight;
    private transient List<Pair<BufferedImage, String>> trainingSet;
    private transient List<String> mapping;

    public ConvolutionNeuralNetworkContainer(String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt) {
        this.cnnId = cnnId;
        this.filtersCnt = filtersCnt;
        this.poolLayersCnt = poolLayersCnt;
        this.fclChoicesCnt = fclChoicesCnt;

        this.fclInputImgWidth = (int)(origImgWidth / (Math.pow(2, poolLayersCnt)));
        this.fclInputImgHeight = (int)(origImgHeight / (Math.pow(2, poolLayersCnt)));

        this.convolutionLayer = new ConvolutionLayer(this);
        this.fullyConnectedLayer = new FullyConnectedLayer(this);
    }

    public void setTrainingSet(List<Pair<BufferedImage, String>> trainingSet) {
        this.trainingSet = trainingSet;
    }

    public void setMapping(List<String> mapping) {
        this.mapping = mapping;
    }

    public void run() {
        System.out.println("started to train " + cnnId);
        AtomicInteger correctGuesses = new AtomicInteger();
        poolLayers = new PoolLayer[poolLayersCnt];

        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(trainingSet);
            trainingSet.forEach(pairOfImageAndValue -> {
                try {
                    List<BufferedImage> result = convolutionLayer.forward(pairOfImageAndValue.getKey());
                    for (int j = 0; j < poolLayersCnt; j++) {
                        poolLayers[j] = new PoolLayer(result);
                        result = poolLayers[j].forward();
                    }
                    double[] softmaxResults = fullyConnectedLayer.forward(result, this);
                    int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
                    String expectedCard = pairOfImageAndValue.getValue();
                    if (expectedCard.equals(mapping.get(maxProbabilityIndex))) {
                        correctGuesses.incrementAndGet();
                    }
                    // System.out.print(maxProbabilityIndex != -1 ? ("I guess it's " + cardsMapping.get(maxProbabilityIndex)) : "no maximum");
                    // System.out.println("; it was " + expectedCard + (expectedCard.equals(cardsMapping.get(maxProbabilityIndex)) ? " - correct!" : ""));

                    double[][][] backpropResults = fullyConnectedLayer.backprop(mapping.indexOf(expectedCard), 0.01, this);
                    for (int j = poolLayersCnt - 1; j >= 0; j--) {
                        backpropResults = poolLayers[j].backprop(backpropResults, this);
                    }
                    convolutionLayer.backprop(backpropResults, 0.01);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(cnnId + ".jobj"));
                oos.writeObject(this);
                oos.flush();
                System.out.println("Wrote CNN " + cnnId + " to the filesystem; correctGuesses = " + new DecimalFormat("###.###").format(((double) correctGuesses.get() / trainingSet.size()) * 100) + "%");
                correctGuesses.set(0);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    int getFiltersCnt() { return filtersCnt; }
    int getFclChoicesCnt() { return fclChoicesCnt; }
    int getFclInputImgWidth() { return fclInputImgWidth; }
    int getFclInputImgHeight() { return fclInputImgHeight; }
}
