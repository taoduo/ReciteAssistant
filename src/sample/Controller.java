package sample;


import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class Controller {
    private Stage appStage;

    private static final String NEW_CARD_BUFFER_PATH = "./.cards/";
    private static final String BUFFER_HINT = NEW_CARD_BUFFER_PATH + "hints/";
    private static final String BUFFER_ANS = NEW_CARD_BUFFER_PATH + "answers/";
    private static final String FILE_TYPE = "png";

    @FXML
    private TextArea latexHint;

    @FXML
    private TextArea latexAnswer;

    @FXML
    private ImageView hintPreview;

    @FXML
    private ImageView answerPreview;

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

    /* new card window status viables */
    private boolean hintTextChanged = true;
    private boolean answerTextChanged = true;

    @FXML
    public void previewBtnClick() {
        refreshHint();
        refreshAnswer();
    }

    @FXML
    public void saveBtnClick() {
        String fileName = Long.toString(new Date().getTime());
        refreshHint();
        refreshAnswer();
        try {
            File hintDirectory = new File(BUFFER_HINT);
            File answerDirectory = new File(BUFFER_ANS);
            hintDirectory.mkdirs();
            answerDirectory.mkdirs();
            File hint = new File(hintDirectory, fileName + "." + FILE_TYPE);
            File answer = new File(answerDirectory + fileName + "." + FILE_TYPE);
            ImageIO.write(SwingFXUtils.fromFXImage(hintPreview.getImage(), null), FILE_TYPE, hint.getAbsoluteFile());
            ImageIO.write(SwingFXUtils.fromFXImage(answerPreview.getImage(), null), FILE_TYPE, answer.getAbsoluteFile());
            latexHint.setText("");
            latexAnswer.setText("");
            hintTextChanged = true;
            answerTextChanged = true;
            refreshHint();
            refreshAnswer();
            showSuccessAlert("Flash Card Saved");
        } catch (Exception e) {
            showErrorAlert(e);
        }
    }

    @FXML
    public void hintTextChanged() {
        hintTextChanged = true;
    }

    @FXML
    public void answerTextChanged() {
        answerTextChanged = true;
    }
    @FXML
    public void reviewCardKeyType(KeyEvent event) {
        if (event.getCode() == KeyCode.LEFT) {
            // previous card
            if (reviewCurrentHintIndex > 0) {
                reviewCurrentHintIndex--;
                reviewShowImage(reviewHints[reviewCurrentHintIndex]);
            } else {
                showSuccessAlert("This the start of all cards");
            }
        } else if (event.getCode() == KeyCode.RIGHT) {
            // next card
            if (reviewCurrentHintIndex < reviewHints.length - 1) {
                reviewCurrentHintIndex++;
                reviewShowImage(reviewHints[reviewCurrentHintIndex]);
            } else {
                showSuccessAlert("We reached the end of all cards");
            }
        } else if (event.getCode() == KeyCode.SPACE) {
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
        }
    }

    @FXML
    public void exportBtnClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export Cards");
        File target = directoryChooser.showDialog(this.appStage);
        if (target != null && target.isDirectory()) {
            try {
                File[] hints = new File(BUFFER_HINT).listFiles();
                File[] answers = new File(BUFFER_ANS).listFiles();
                if (hints != null) {
                    File targetHints = new File(target.getAbsolutePath() + "/hints/");
                    targetHints.mkdirs();
                    for (File hint : hints) {
                        Files.copy(hint.toPath(), new File(targetHints, hint.getName()).toPath());
                        hint.delete();
                    }
                }
                if (answers != null) {
                    File targetAnswers = new File(target.getAbsolutePath() + "/answers/");
                    targetAnswers.mkdirs();
                    for (File answer : answers) {
                        Files.copy(answer.toPath(), new File(targetAnswers, answer.getName()).toPath());
                        answer.delete();
                    }
                }
                showSuccessAlert("Export Successful. Cards saved to " + target.getAbsolutePath());
            } catch (IOException e) {
                showErrorAlert(e);
            }
        }
    }

    @FXML
    public void importBtnClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export Cards");
        File target = directoryChooser.showDialog(this.appStage);
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
        for (int i = reviewHints.length - 1; i > 0; i--)
        {
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

    private void refreshHint() {
        if (hintTextChanged) {
            String latex = latexHint.getText();
            hintPreview.setImage(getImageFromLatex(latex));
            hintTextChanged = false;
        }
    }

    private void refreshAnswer() {
        if (answerTextChanged) {
            String latex = latexAnswer.getText();
            answerPreview.setImage(getImageFromLatex(latex));
            answerTextChanged = false;
        }
    }

    private void reviewShowImage(File image) {
        try {
            reviewCurrent.setImage(SwingFXUtils.toFXImage(ImageIO.read(image), null));
            reviewIndexLabel.setText((reviewCurrentHintIndex + 1) + "/" + reviewHints.length);
        } catch (IOException e) {
            showErrorAlert(e);
        }
    }

    private Image getImageFromLatex(String latex) {

        Image image = null;
        try {
            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(20).build();
            icon.setInsets(new Insets(5, 5, 5, 5));
            BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
            JLabel jl = new JLabel();
            jl.setForeground(new Color(0, 0, 0));
            icon.paintIcon(jl, g2, 0, 0);
            image = SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (ParseException e) {
            showErrorAlert(e);
        }
        return image;
    }

    void setAppStage(Stage stage) {
        this.appStage = stage;
    }

    private void showErrorAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setHeaderText(e.getClass().toString());
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void showSuccessAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
