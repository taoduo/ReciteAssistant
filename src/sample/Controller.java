package sample;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

class Controller {
    static Stage appStage;
    static final String NEW_CARD_BUFFER_PATH = "./.cards/";
    static final String BUFFER_HINT = NEW_CARD_BUFFER_PATH + "hints/";
    static final String BUFFER_ANS = NEW_CARD_BUFFER_PATH + "answers/";
    static final String FILE_TYPE = "png";

    Image getImageFromLatex(String latex) {
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

    static void setAppStage(Stage stage) {
        appStage = stage;
    }

    void showErrorAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setHeaderText(e.getClass().toString());
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    void showSuccessAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
