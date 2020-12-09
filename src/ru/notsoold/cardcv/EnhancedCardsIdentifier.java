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
            train(args[1]);
        } else if ("identify".equals(args[0])) {
            identify(args[1]);
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
        List<Path> imagesPaths = Files.walk(Paths.get(pathToFolderWithImages)).filter(Files::isRegularFile).collect(Collectors.toList());
        List<Pair<String, List<BufferedImage>>> screenshotsToIdentify = imagesPaths.stream()
                .map(imgPath -> new Pair<>(imgPath.getFileName().toString(), CardsCutter.cut(imgPath))).collect(Collectors.toList());
        // Identification of the card by its suit and value.
        ConvolutionNeuralNetworkContainer suitCNN = getCardSuitCnnForIdentification(null);
        ConvolutionNeuralNetworkContainer valueCNN = getCardValueCnnForIdentification(null);
        ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 2, 60000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        screenshotsToIdentify.forEach(listOfCardsOnTable -> {
            try {
                List<BufferedImage> listOfCardSuits = listOfCardsOnTable.getValue().stream().map(CardsCutter::cutCardSuit).collect(Collectors.toList());
                List<BufferedImage> listOfCardvalues = listOfCardsOnTable.getValue().stream().map(CardsCutter::cutCardValue).collect(Collectors.toList());
                Future<List<String>> suitIdentificationFuture = executor.submit(suitCNN.identifyImages(listOfCardSuits));
                Future<List<String>> valueIdentificationFuture = executor.submit(valueCNN.identifyImages(listOfCardvalues));
                List<String> suitIdentificationResult = suitIdentificationFuture.get();
                List<String> valueIdentificationResult = valueIdentificationFuture.get();
                System.out.print(listOfCardsOnTable.getKey() + " - ");
                for (int i = 0; i < listOfCardsOnTable.getValue().size(); i++) {
                    System.out.print(valueIdentificationResult.get(i) + suitIdentificationResult.get(i));
                }
                System.out.println();
            } catch (Exception e) { e.printStackTrace(); }
        });
        executor.shutdown();
        // Identification of the card by the whole image - not used by now, but it could be if someone wants to.
        /*ConvolutionNeuralNetworkContainer wholeCardCNN = getWholeCardCnnForIdentification(null);
        screenshotsToIdentify.forEach(listOfCardsOnTable -> {
            List<String> identificationResults = wholeCardCNN.identifyImages(listOfCardsOnTable.getValue()).call();
            System.out.print(listOfCardsOnTable.getKey() + " - ");
            identificationResults.forEach(System.out::print);
            System.out.println();
        });*/
    }

    public static ConvolutionNeuralNetworkContainer loadOrCreateCNN(String jObjPath, String cnnId, int filtersCnt, int poolLayersCnt, int origImgWidth, int origImgHeight, int fclChoicesCnt, boolean forTraining, double learnRate) throws Exception {
        if (Files.exists(Paths.get(jObjPath))) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(jObjPath));
            return ((ConvolutionNeuralNetworkContainer)ois.readObject()).isTraining(forTraining);
        } else {
            if (forTraining) {
                return new ConvolutionNeuralNetworkContainer(cnnId, filtersCnt, poolLayersCnt, origImgWidth, origImgHeight, fclChoicesCnt, learnRate).isTraining(forTraining);
            } else {
                throw new RuntimeException("Cannot find trained CNN " + cnnId + " in " + jObjPath + ", first train and save it.");
            }
        }
    }

    public static ConvolutionNeuralNetworkContainer getCardSuitCnnForTraining(List<Pair<BufferedImage, String>> cardSuitImagesTrainingSet) throws Exception {
        return loadOrCreateCNN("networks\\suitCNN_1.0.jobj", "suitCNN_1.0", 4, 1, 40, 40, 4, true, 0.01).trainingSet(cardSuitImagesTrainingSet).mapping(CardCvUtils.cardSuitsMapping);
    }

    public static ConvolutionNeuralNetworkContainer getCardSuitCnnForIdentification(List<BufferedImage> cardSuitImagesIdentificationSet) throws Exception {
        return loadOrCreateCNN("networks\\suitCNN_1.0.jobj", "suitCNN_1.0", 4, 1, 40, 40, 4, false, 0.01).identifyImages(cardSuitImagesIdentificationSet).mapping(CardCvUtils.cardSuitsMapping);
    }

    public static ConvolutionNeuralNetworkContainer getCardValueCnnForTraining(List<Pair<BufferedImage, String>> cardValueImagesTrainingSet) throws Exception {
        return loadOrCreateCNN("networks\\valueCNN_1.0.jobj", "valueCNN_1.0", 4, 1, 30, 30, 13, true, 0.01).trainingSet(cardValueImagesTrainingSet).mapping(CardCvUtils.cardValuesMapping);
    }

    public static ConvolutionNeuralNetworkContainer getCardValueCnnForIdentification(List<BufferedImage> cardValueImagesIdentificationSet) throws Exception {
        return loadOrCreateCNN("networks\\valueCNN_1.0.jobj", "valueCNN_1.0", 4, 1, 30, 30, 13, false, 0.01).identifyImages(cardValueImagesIdentificationSet).mapping(CardCvUtils.cardValuesMapping);
    }

    public static ConvolutionNeuralNetworkContainer getWholeCardCnnForTraining(List<Pair<BufferedImage, String>> wholeCardsTrainingSet) throws Exception {
        return loadOrCreateCNN("networks\\PlayingCardsIdentifier.jobj", "PlayingCardsIdentifier", 4, 2, 70, 100, 52, true, 0.1).trainingSet(wholeCardsTrainingSet).mapping(CardCvUtils.cardsMapping);
    }

    public static ConvolutionNeuralNetworkContainer getWholeCardCnnForIdentification(List<BufferedImage> wholeCardsIdentificationSet) throws Exception {
        return loadOrCreateCNN("networks\\PlayingCardsIdentifier.jobj", "PlayingCardsIdentifier", 4, 2, 70, 100, 52, false, 0.1).identifyImages(wholeCardsIdentificationSet).mapping(CardCvUtils.cardsMapping);
    }
}
