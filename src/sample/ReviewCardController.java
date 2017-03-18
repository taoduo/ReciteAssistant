package sample;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;

/**
 * Controller for the card review panel
 */
public class ReviewCardController extends Controller {

    @FXML
    private ImageView reviewCurrent;

    @FXML
    private javafx.scene.control.Label reviewIndexLabel;

    /* review window status variables */
    // current hint index
    private int reviewCurrentHintIndex = -1;
    // true: hint; false: answer
    private boolean reviewCurrentAnswerOrHint = true;
    // all hint files
    private File[] reviewHints;

    @FXML
    public void reviewCardKeyType(KeyEvent event) {
        switch (event.getCharacter()) {
            case "a":
                // previous card
                if (reviewCurrentHintIndex > 0) {
                    reviewCurrentHintIndex--;
                } else {
                    reviewCurrentHintIndex = reviewHints.length - 1;
                }
                reviewShowImage(reviewHints[reviewCurrentHintIndex]);
                reviewCurrentAnswerOrHint = true;
                break;
            case "d":
                // next card
                if (reviewCurrentHintIndex < reviewHints.length - 1) {
                    reviewCurrentHintIndex++;
                } else {
                    reviewCurrentHintIndex = 0;
                }
                reviewCurrentAnswerOrHint = true;
                reviewShowImage(reviewHints[reviewCurrentHintIndex]);
                break;
            case "j":
                if (reviewCurrentAnswerOrHint) {
                    // hint->answer
                    File currentHintFile = reviewHints[reviewCurrentHintIndex];
                    File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                            "/answers/" + currentHintFile.getName());
                    if (currentAnswerFile.exists()) {
                        reviewShowImage(currentAnswerFile);
                    } else {
                        showErrorAlert(new FileNotFoundException("Answer file is not Found"));
                    }
                } else {
                    // answer->hint
                    reviewShowImage(reviewHints[reviewCurrentHintIndex]);
                }
                reviewCurrentAnswerOrHint = !reviewCurrentAnswerOrHint;
                break;
            case "k":
                // mark the card
                if (reviewCurrentHintIndex >= 0) {
                    File currentHintFile = reviewHints[reviewCurrentHintIndex];
                    File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                            "/answers/" + currentHintFile.getName());
                    markCard(currentHintFile);
                    markCard(currentAnswerFile);
                    if (reviewCurrentAnswerOrHint) {
                        reviewShowImage(currentHintFile);
                    } else {
                        reviewShowImage(currentAnswerFile);
                    }
                }
                break;
            case "u":
                // unmark the card
                if (reviewCurrentHintIndex >= 0) {
                    File currentHintFile = reviewHints[reviewCurrentHintIndex];
                    File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                            "/answers/" + currentHintFile.getName());
                    unMarkCard(currentHintFile);
                    unMarkCard(currentAnswerFile);
                    if (reviewCurrentAnswerOrHint) {
                        reviewShowImage(currentHintFile);
                    } else {
                        reviewShowImage(currentAnswerFile);
                    }
                }
                break;
        }
    }

    @FXML
    public void importBtnClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export Cards");
        File target = directoryChooser.showDialog(appStage);
        if (target != null && target.isDirectory()) {
            File hintFolder = new File(target, "/hints");
            if (hintFolder.exists()) {
                File[] hints = hintFolder.listFiles((f)->f.getName().endsWith("." + FILE_TYPE));
                if (hints != null && hints.length != 0) {
                    reviewHints = hints;
                    reviewCurrentHintIndex = 0;
                    reviewCurrentAnswerOrHint = true;
                    reviewShowImage(reviewHints[reviewCurrentHintIndex]);
                } else {
                    showErrorAlert(new Exception("Hints folder is empty"));
                }
            } else {
                showErrorAlert(new FileNotFoundException("Hints folder is not found"));
            }
        }
    }

    @FXML
    public void shuffleBtnClick() {
        Random rnd = ThreadLocalRandom.current();
        for (int i = reviewHints.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            File a = reviewHints[index];
            reviewHints[index] = reviewHints[i];
            reviewHints[i] = a;
        }
        reviewCurrentHintIndex = 0;
        reviewCurrentAnswerOrHint = true;
        reviewShowImage(reviewHints[0]);
    }

    private void markCard(File image) {
        try {
            BufferedImage img = ImageIO.read(image);
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color ic = new Color(img.getRGB(i, j));
                    Color scr = screenWithRed(ic);
                    img.setRGB(i, j, scr.getRGB());
                }
            }
            ImageIO.write(img, FILE_TYPE, image);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private Color screenWithRed(Color img) {
        if (img.getRed() == 255 && img.getGreen() == img.getBlue()) {
            return img;
        }
        Color red = Color.RED;
        int scale = (img.getRed() + img.getGreen() + img.getBlue()) / 3;
        return new Color(255, scale, scale);
    }

    private void unMarkCard(File image) {
        try {
            BufferedImage img = ImageIO.read(image);
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color ic = new Color(img.getRGB(i, j));
                    Color scr = removeRedScreen(ic);
                    img.setRGB(i, j, scr.getRGB());
                }
            }
            ImageIO.write(img, FILE_TYPE, image);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private Color removeRedScreen(Color img) {
        Color red = Color.RED;
        if (img.getGreen() != img.getBlue()) {
            // not screened
            return img;
        }
        int scale = (img.getGreen() + img.getBlue()) / 2;
        return new Color(scale, scale, scale);
    }

    private void reviewShowImage(File image) {
        try {
            reviewCurrent.setImage(SwingFXUtils.toFXImage(ImageIO.read(image), null));
            reviewIndexLabel.setText((reviewCurrentHintIndex + 1) + "/" + reviewHints.length);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private void reviewShowImage(Image image) {
        reviewCurrent.setImage(image);
        reviewIndexLabel.setText((reviewCurrentHintIndex + 1) + "/" + reviewHints.length);
    }
}
