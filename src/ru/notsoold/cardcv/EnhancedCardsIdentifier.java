package ru.notsoold.cardcv;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

// Compile into executable Jar:
// "C:\Program Files\Java\jdk1.8.0_221\bin\jar.exe" cvfe CardCv.jar ru.notsoold.cardcv.EnhancedCardsIdentifier ru\notsoold\cardcv\*
public class EnhancedCardsIdentifier {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("args: <mode: train | identify> <path to folder with images>");
            return;
        }
        if ("train".equals(args[0])) {
            train(args[1]);  // "CSSSRCase\\src\\cut_cards\\"
        } else if ("identify".equals(args[0])) {
            identify(args[1]);  // "CSSSRCase\\src\\identify_screens\\"
        }
    }

    private static void train(String pathToTrainingSet) throws Exception {
        List<Path> trainingPlayingCardsPaths = Files.walk(Paths.get(pathToTrainingSet)).filter(Files::isRegularFile).collect(Collectors.toList());
        Collections.shuffle(trainingPlayingCardsPaths);
        List<Pair<BufferedImage, String>> wholeCardImagesTrainingSet = new ArrayList<>();
        List<Pair<BufferedImage, String>> cardValueImagesTrainingSet = new ArrayList<>();
        List<Pair<BufferedImage, String>> cardSuitImagesTrainingSet = new ArrayList<>();
        for (Path cardFilePath: trainingPlayingCardsPaths) {
            String cardId = cardFilePath.getParent().getFileName().toString();
            BufferedImage originalCardImage = ImageIO.read(cardFilePath.toFile());
            cardValueImagesTrainingSet.add(new Pair<>(CardsCutter.cutCardValue(originalCardImage), cardId.substring(0, cardId.length() - 1)));
            cardSuitImagesTrainingSet.add(new Pair<>(CardsCutter.cutCardSuit(originalCardImage), cardId.substring(cardId.length() - 1)));
            wholeCardImagesTrainingSet.add(new Pair<>(originalCardImage, cardId));
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 3, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        List<Future<List<String>>> futures = executor.invokeAll(Arrays.asList(getCardSuitCnnForTraining(cardSuitImagesTrainingSet), getCardValueCnnForTraining(cardValueImagesTrainingSet), getWholeCardCnnForTraining(wholeCardImagesTrainingSet)));
        for (Future<List<String>> future: futures) {
            future.get();
        }
        executor.shutdown();
    }

    private static void identify(String pathToFolderWithImages) throws Exception {
        List<List<BufferedImage>> screenshotsToIdentify = Files.walk(Paths.get(pathToFolderWithImages)).filter(Files::isRegularFile)
                .map(CardsCutter::cut).collect(Collectors.toList());
        // Identification of the card by its suit and value.
        ConvolutionNeuralNetworkContainer suitCNN = getCardSuitCnnForIdentification(null);
        ConvolutionNeuralNetworkContainer valueCNN = getCardValueCnnForIdentification(null);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        screenshotsToIdentify.forEach(listOfCardsOnTable -> {
            try {
                long start = System.currentTimeMillis();
                List<BufferedImage> listOfCardSuits = listOfCardsOnTable.stream().map(CardsCutter::cutCardSuit).collect(Collectors.toList());
                List<BufferedImage> listOfCardvalues = listOfCardsOnTable.stream().map(CardsCutter::cutCardValue).collect(Collectors.toList());
                suitCNN.setImagesToIdentify(listOfCardSuits);
                valueCNN.setImagesToIdentify(listOfCardvalues);
                Future<List<String>> suitIdentificationFuture = executor.submit(suitCNN);
                Future<List<String>> valueIdentificationFuture = executor.submit(valueCNN);
                List<String> suitIdentificationResult = suitIdentificationFuture.get();
                List<String> valueIdentificationResult = valueIdentificationFuture.get();
                for (int i = 0; i < listOfCardsOnTable.size(); i++) {
                    System.out.print(valueIdentificationResult.get(i) + suitIdentificationResult.get(i));
                }
                System.out.println();
                System.out.println("" + (System.currentTimeMillis() - start));
            } catch (Exception e) { e.printStackTrace(); }
        });
        executor.shutdown();
        // Identification of the card by the whole image.
        ConvolutionNeuralNetworkContainer wholeCardCNN = getWholeCardCnnForIdentification(null);
        screenshotsToIdentify.forEach(listOfCardsOnTable -> {
            long start = System.currentTimeMillis();
            wholeCardCNN.setImagesToIdentify(listOfCardsOnTable);
            wholeCardCNN.call().forEach(System.out::print);
            System.out.println();
            System.out.println("" + (System.currentTimeMillis() - start));
        });
    }

    public static ConvolutionNeuralNetworkContainer loadOrCreateCNN(String jObjPath, String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt, boolean forTraining) throws Exception {
        if (Files.exists(Paths.get(jObjPath))) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(jObjPath));
            ConvolutionNeuralNetworkContainer cnn = ((ConvolutionNeuralNetworkContainer)ois.readObject());
            cnn.setInTrainingMode(forTraining);
            return cnn;
        } else {
            if (forTraining) {
                ConvolutionNeuralNetworkContainer cnn = new ConvolutionNeuralNetworkContainer(cnnId, filtersCnt, poolLayersCnt, origImgWidth, origImgHeight, fclChoicesCnt);
                cnn.setInTrainingMode(forTraining);
                return cnn;
            } else {
                throw new RuntimeException("Cannot find trained CNN " + cnnId + " in " + jObjPath + ", first train and save it.");
            }
        }
    }

    public static ConvolutionNeuralNetworkContainer getCardSuitCnnForTraining(List<Pair<BufferedImage, String>> cardSuitImagesTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer suitCNN = loadOrCreateCNN("CSSSRCase\\suitCNN_1.0.jobj", "suitCNN_1.0", 4, 1, 40, 40, 4, true);
        suitCNN.setTrainingSet(cardSuitImagesTrainingSet);
        suitCNN.setMapping(CardCvUtils.cardSuitsMapping);
        return suitCNN;
    }

    public static ConvolutionNeuralNetworkContainer getCardSuitCnnForIdentification(List<BufferedImage> cardSuitImagesIdentificationSet) throws Exception {
        ConvolutionNeuralNetworkContainer suitCNN = loadOrCreateCNN("CSSSRCase\\suitCNN_1.0.jobj", "suitCNN_1.0", 4, 1, 40, 40, 4, false);
        suitCNN.setImagesToIdentify(cardSuitImagesIdentificationSet);
        suitCNN.setMapping(CardCvUtils.cardSuitsMapping);
        return suitCNN;
    }

    public static ConvolutionNeuralNetworkContainer getCardValueCnnForTraining(List<Pair<BufferedImage, String>> cardValueImagesTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer valueCNN = loadOrCreateCNN("CSSSRCase\\valueCNN_1.0.jobj", "valueCNN_1.0", 4, 1, 30, 30, 13, true);
        valueCNN.setTrainingSet(cardValueImagesTrainingSet);
        valueCNN.setMapping(CardCvUtils.cardValuesMapping);
        return valueCNN;
    }

    public static ConvolutionNeuralNetworkContainer getCardValueCnnForIdentification(List<BufferedImage> cardValueImagesIdentificationSet) throws Exception {
        ConvolutionNeuralNetworkContainer valueCNN = loadOrCreateCNN("CSSSRCase\\valueCNN_1.0.jobj", "valueCNN_1.0", 4, 1, 30, 30, 13, false);
        valueCNN.setImagesToIdentify(cardValueImagesIdentificationSet);
        valueCNN.setMapping(CardCvUtils.cardValuesMapping);
        return valueCNN;
    }

    public static ConvolutionNeuralNetworkContainer getWholeCardCnnForTraining(List<Pair<BufferedImage, String>> wholeCardsTrainingSet) throws Exception {
        ConvolutionNeuralNetworkContainer cardCNN = loadOrCreateCNN("CSSSRCase\\PlayingCardsIdentifier.jobj", "PlayingCardsIdentifier", 4, 2, 70, 100, 52, true);
        cardCNN.setTrainingSet(wholeCardsTrainingSet);
        cardCNN.setMapping(CardCvUtils.cardsMapping);
        return cardCNN;
    }

    public static ConvolutionNeuralNetworkContainer getWholeCardCnnForIdentification(List<BufferedImage> wholeCardsIdentificationSet) throws Exception {
        ConvolutionNeuralNetworkContainer cardCNN = loadOrCreateCNN("CSSSRCase\\PlayingCardsIdentifier.jobj", "PlayingCardsIdentifier", 4, 2, 70, 100, 52, false);
        cardCNN.setImagesToIdentify(wholeCardsIdentificationSet);
        cardCNN.setMapping(CardCvUtils.cardsMapping);
        return cardCNN;
    }

}
