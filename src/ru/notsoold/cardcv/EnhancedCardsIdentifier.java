package ru.notsoold.cardcv;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class EnhancedCardsIdentifier {

    public static void main(String[] args) throws Exception {
        train();
    }

    private static void train() throws Exception {
        List<Path> trainingPlayingCardsPaths = Files.walk(Paths.get("CSSSRCase\\src\\cut_cards\\")).filter(Files::isRegularFile).collect(Collectors.toList());
        Collections.shuffle(trainingPlayingCardsPaths);
        List<Pair<BufferedImage, String>> wholeCardImagesTrainingSet = new ArrayList<>();
        List<Pair<BufferedImage, String>> cardValueImagesTrainingSet = new ArrayList<>();
        List<Pair<BufferedImage, String>> cardSuitImagesTrainingSet = new ArrayList<>();
        for (Path cardFilePath: trainingPlayingCardsPaths) {
            String cardId = cardFilePath.getParent().getFileName().toString();
            BufferedImage originalCardImage = ImageIO.read(cardFilePath.toFile());
            BufferedImage cardValueImage = originalCardImage.getSubimage(5, 5, 30, 30);
            BufferedImage cardSuitImage = originalCardImage.getSubimage(26, 58, 40, 40);
            cardValueImagesTrainingSet.add(new Pair<>(cardValueImage, cardId.substring(0, cardId.length() - 1)));
            cardSuitImagesTrainingSet.add(new Pair<>(cardSuitImage, cardId.substring(cardId.length() - 1)));
            wholeCardImagesTrainingSet.add(new Pair<>(originalCardImage, cardId));
        }

        new Thread(getCardSuitCNN(cardSuitImagesTrainingSet), "suitCNN_Thread").start();
        new Thread(getCardValueCNN(cardValueImagesTrainingSet), "valueCNN_Thread").start();
        new Thread(getWholeCardCNN(wholeCardImagesTrainingSet), "cardCNN_Thread").start();
    }

    public static ConvolutionNeuralNetworkContainer loadOrCreateCNN(String jObjPath, String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt) throws Exception {
        if (Files.exists(Paths.get(jObjPath))) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(jObjPath));
            return ((ConvolutionNeuralNetworkContainer)ois.readObject());
        } else {
            return new ConvolutionNeuralNetworkContainer(cnnId, filtersCnt, poolLayersCnt,  origImgWidth, origImgHeight, fclChoicesCnt);
        }
    }

    public static ConvolutionNeuralNetworkContainer getCardSuitCNN(List<Pair<BufferedImage, String>> cardSuitImagesTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer suitCNN = loadOrCreateCNN("CSSSRCase\\suitCNN_1.0.jobj", "suitCNN_1.0", 4, 1, 40, 40, 4);
        suitCNN.setTrainingSet(cardSuitImagesTrainingSet);
        suitCNN.setMapping(CardCvUtils.cardSuitsMapping);
        return suitCNN;
    }

    public static ConvolutionNeuralNetworkContainer getCardValueCNN(List<Pair<BufferedImage, String>> cardValueImagesTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer valueCNN = loadOrCreateCNN("CSSSRCase\\valueCNN_1.0.jobj", "valueCNN_1.0", 4, 1, 30, 30, 13);
        valueCNN.setTrainingSet(cardValueImagesTrainingSet);
        valueCNN.setMapping(CardCvUtils.cardValuesMapping);
        return valueCNN;
    }

    public static ConvolutionNeuralNetworkContainer getWholeCardCNN(List<Pair<BufferedImage, String>> wholeCardsTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer cardCNN = loadOrCreateCNN("CSSSRCase\\PlayingCardsIdentifier.jobj", "PlayingCardsIdentifier", 4, 2, 70, 100, 52);
        cardCNN.setTrainingSet(wholeCardsTrainingSet);
        cardCNN.setMapping(CardCvUtils.cardsMapping);
        return cardCNN;
    }

}
