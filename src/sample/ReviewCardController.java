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

    @FXML
    private javafx.scene.control.Label answerOrHintLabel;

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
                break;
            case "j":
                reviewCurrentAnswerOrHint = !reviewCurrentAnswerOrHint;
                break;
            case "k":
                // toggle marking the card
                if (reviewCurrentHintIndex >= 0) {
                    File currentHintFile = reviewHints[reviewCurrentHintIndex];
                    File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                            "/answers/" + currentHintFile.getName());
                    toggleMark(currentHintFile);
                    toggleMark(currentAnswerFile);
                }
        }
        refreshView();
    }

    @FXML
    public void importBtnClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Import Cards");
        File target = directoryChooser.showDialog(appStage);
        if (importDirectory(target)) {
            reviewCurrentHintIndex = reviewHints.length != 0 ? 0 : -1;
            reviewCurrentAnswerOrHint = true;
            refreshView();
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
        refreshView();
    }

    @FXML
    public void reviewCurrentBtnClick() {
        File currentDir = new File(NEW_CARD_BUFFER_PATH);
        if (currentDir.exists() && importDirectory(currentDir)) {
            reviewCurrentHintIndex = reviewHints.length != 0 ? 0 : -1;
            reviewCurrentAnswerOrHint = true;
            refreshView();
        }
    }

    @FXML
    public void deleteBtnClick() {
        if (reviewCurrentHintIndex != -1) {
            File currentHintFile = reviewHints[reviewCurrentHintIndex];
            File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                    "/answers/" + currentHintFile.getName());
            currentHintFile.delete();
            currentAnswerFile.delete();
            if (importDirectory(currentHintFile.getParentFile().getParentFile())) {
                if (reviewCurrentHintIndex > reviewHints.length - 1) {
                    reviewCurrentHintIndex = reviewHints.length - 1;
                }
                reviewCurrentAnswerOrHint = true;
                refreshView();
            }
        }
    }

    /* Import Helper */
    private boolean importDirectory(File dir) {
        if (dir != null && dir.isDirectory()) {
            File hintFolder = new File(dir, "/hints");
            if (hintFolder.exists()) {
                File[] hints = hintFolder.listFiles((f)->f.getName().endsWith("." + FILE_TYPE));
                if (hints != null) {
                    reviewHints = hints;
                    return true;
                } else {
                    return false;
                }
            } else {
                showErrorAlert(new FileNotFoundException("Hints folder is not found"));
                return false;
            }
        } else {
            return false;
        }
    }

    /* Mark / Unmark methods */
    private void toggleMark(File image) {
        try {
            BufferedImage img = ImageIO.read(image);
            for (int i = 0; i < img.getWidth(); i++) {
                for (int j = 0; j < img.getHeight(); j++) {
                    Color ic = new Color(img.getRGB(i, j));
                    Color scr = toggleScreen(ic);
                    img.setRGB(i, j, scr.getRGB());
                }
            }
            ImageIO.write(img, FILE_TYPE, image);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private Color toggleScreen(Color img) {
        if (img.getRed() == 255 && img.getGreen() == img.getBlue()) {
            Color red = Color.RED;
            int scale = (img.getGreen() + img.getBlue()) / 2;
            return new Color(scale, scale, scale);
        }
        Color red = Color.RED;
        int scale = (img.getRed() + img.getGreen() + img.getBlue()) / 3;
        return new Color(255, scale, scale);
    }

    /* View methods */
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

    private void refreshAnswerOrHintLabel() {
        if (reviewCurrentAnswerOrHint) {
            answerOrHintLabel.setText("Hint:");
        } else {
            answerOrHintLabel.setText("Answer:");
        }
    }

    private void refreshView() {
        refreshAnswerOrHintLabel();
        if (reviewCurrentHintIndex != -1) {
            File currentHintFile = reviewHints[reviewCurrentHintIndex];
            File currentAnswerFile = new File(new File(currentHintFile.getParent()).getParent(),
                    "/answers/" + currentHintFile.getName());
            if (reviewCurrentAnswerOrHint) {
                reviewShowImage(currentHintFile);
            } else {
                reviewShowImage(currentAnswerFile);
            }
        } else {
            reviewShowImage((Image) null);
        }
    }
}
