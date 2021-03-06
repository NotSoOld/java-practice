package ru.notsoold.cardcv;

import javafx.util.Pair;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import static ru.notsoold.cardcv.CardCvUtils.*;

/**
 * Created by Dmitriy "NotSoOld" Somov (1000lop@gmail.com)
 * at 7 Dec 2020. All rights reserved.
 */
public class ConvolutionNeuralNetworkContainer implements Serializable, Callable<List<String>> {
    private static final long serialVersionUID = 1879428424962345L;

    private ConvolutionLayer convolutionLayer;
    private transient PoolLayer[] poolLayers;
    private FullyConnectedLayer fullyConnectedLayer;

    private String cnnId;
    private int filtersCnt, poolLayersCnt, fclChoicesCnt, fclInputImgWidth, fclInputImgHeight;
    private double learnRate;
    private transient List<Pair<BufferedImage, String>> trainingSet;
    private transient List<String> mapping;
    private transient boolean isInTrainingMode;
    private transient List<BufferedImage> imagesToIdentify;
    private transient List<String> identificationResults;

    public ConvolutionNeuralNetworkContainer(String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt, double learnRate) {
        this.cnnId = cnnId;
        this.filtersCnt = filtersCnt;
        this.poolLayersCnt = poolLayersCnt;
        this.fclChoicesCnt = fclChoicesCnt;
        this.fclInputImgWidth = (int)(origImgWidth / (Math.pow(2, poolLayersCnt)));
        this.fclInputImgHeight = (int)(origImgHeight / (Math.pow(2, poolLayersCnt)));
        this.learnRate = learnRate;

        this.convolutionLayer = new ConvolutionLayer(this);
        this.fullyConnectedLayer = new FullyConnectedLayer(this);
    }

    public ConvolutionNeuralNetworkContainer trainingSet(List<Pair<BufferedImage, String>> trainingSet) { this.trainingSet = trainingSet; return this; }

    public ConvolutionNeuralNetworkContainer identifyImages(List<BufferedImage> imagesToIdentify) { this.imagesToIdentify = imagesToIdentify; return this; }

    public ConvolutionNeuralNetworkContainer mapping(List<String> mapping) { this.mapping = mapping; return this; }

    public ConvolutionNeuralNetworkContainer isTraining(boolean isInTrainingMode) { this.isInTrainingMode = isInTrainingMode; return this; }

    public List<String> getIdentificationResults() { return identificationResults; }

    public List<String> call() {
        poolLayers = new PoolLayer[poolLayersCnt];
        if (isInTrainingMode) {
            train();
            return null;
        } else {
            identify();
            return identificationResults;
        }
    }

    private void identify() {
        identificationResults = new ArrayList<>();
        imagesToIdentify.forEach(imageToIdentify -> {
            List<BufferedImage> result = convolutionLayer.forward(imageToIdentify);
            for (int j = 0; j < poolLayersCnt; j++) {
                poolLayers[j] = new PoolLayer(result);
                result = poolLayers[j].forward();
            }
            double[] softmaxResults = fullyConnectedLayer.forward(result, this);
            int maxProbabilityIndex = getIndexOfMaxElement(softmaxResults);
            identificationResults.add(mapping.get(maxProbabilityIndex));
        });
    }

    private void train() {
        System.out.println("started to train " + cnnId);
        AtomicInteger correctGuesses = new AtomicInteger();

        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(trainingSet);
            trainingSet.forEach(pairOfImageAndValue -> {
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
                double[][][] backpropResults = fullyConnectedLayer.backprop(mapping.indexOf(expectedCard), learnRate, this);
                for (int j = poolLayersCnt - 1; j >= 0; j--) {
                    backpropResults = poolLayers[j].backprop(backpropResults, this);
                }
                convolutionLayer.backprop(backpropResults, learnRate);
            });
            try {
                double correctGuessesRate = (double)correctGuesses.get() / trainingSet.size();
                System.out.print("correctGuesses = " + new DecimalFormat("###.###").format(correctGuessesRate * 100) + "%");
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("networks\\" + cnnId + ".jobj"));
                oos.writeObject(this);
                oos.flush();
                System.out.println(", wrote CNN " + cnnId + " to the filesystem.");
                if (correctGuessesRate > 0.99) {
                    System.out.println(cnnId + " is trained above 99% accuracy, stopping.");
                    return;
                }
                correctGuesses.set(0);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    int getFiltersCnt() { return filtersCnt; }
    int getFclChoicesCnt() { return fclChoicesCnt; }
    int getFclInputImgWidth() { return fclInputImgWidth; }
    int getFclInputImgHeight() { return fclInputImgHeight; }
}
