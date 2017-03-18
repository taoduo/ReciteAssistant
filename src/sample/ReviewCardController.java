package sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
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

    private void reviewShowImage(File image) {
        try {
            reviewCurrent.setImage(SwingFXUtils.toFXImage(ImageIO.read(image), null));
            reviewIndexLabel.setText((reviewCurrentHintIndex + 1) + "/" + reviewHints.length);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }
}
