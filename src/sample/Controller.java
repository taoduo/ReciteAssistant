package sample;


import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXEnvironment;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;
import org.scilab.forge.jlatexmath.TeXSymbolParser;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.image.*;

public class Controller {
    @FXML
    private TextArea latexHint;

    @FXML
    private TextArea latexAnswer;

    @FXML
    private ImageView hintPreview;

    @FXML
    private ImageView answerPreview;

    private boolean hintTextChanged = true;
    private boolean answerTextChanged = true;

    private static final String CARD_PATH = "./.cards/";
    private static final String HINT_PATH = CARD_PATH + "/hints/";
    private static final String ANSWER_PATH = CARD_PATH + "/answers/";

    private static final String FILE_TYPE = "png";

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
            new File(HINT_PATH).mkdirs();
            new File(ANSWER_PATH).mkdirs();
            File hint = new File(HINT_PATH + fileName + "." + FILE_TYPE);
            File answer = new File(ANSWER_PATH + fileName + "." + FILE_TYPE);
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
