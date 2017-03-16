package sample;


import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

    @FXML
    public void previewBtnClick() {
        refreshHint();
        refreshAnswer();
    }

    @FXML
    public void saveBtnClick() {
//        File file = new File("Example1.png");
//        try {
//            ImageIO.write(image, "png", file.getAbsoluteFile());
//        } catch (IOException ex) { }
    }

    private void refreshHint() {
        String latex = latexHint.getText();
        hintPreview.setImage(getImageFromLatex(latex));
    }

    private void refreshAnswer() {
        String latex = latexAnswer.getText();
        answerPreview.setImage(getImageFromLatex(latex));
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
            showErrorAlert(e.getClass().toString(), e.getMessage());
        }
        return image;
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
