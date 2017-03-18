package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;

/**
 * Controller for the adding new card panel
 */
public class NewCardController extends Controller {
    @FXML
    private TextArea latexHint;

    @FXML
    private TextArea latexAnswer;

    @FXML
    private ImageView hintPreview;

    @FXML
    private ImageView answerPreview;

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
    public void exportBtnClick() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Export Cards");
        File target = directoryChooser.showDialog(appStage);
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
}
