package sample;


import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


import javax.imageio.ImageIO;
import javax.swing.*;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class Controller {
    @FXML
    private TextField latexInput;

    @FXML
    private ImageView latexPreview;

    @FXML
    public void addBtnClick() {
        String latex = latexInput.getText();
        try {
            TeXFormula formula = new TeXFormula(latex);
            TeXIcon icon = formula.new TeXIconBuilder().setStyle(TeXConstants.STYLE_DISPLAY).setSize(20).build();
            icon.setInsets(new Insets(5, 5, 5, 5));
            BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = image.createGraphics();
            g2.setColor(Color.white);
            g2.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
            JLabel jl = new JLabel();
            jl.setForeground(new Color(0, 0, 0));
            icon.paintIcon(jl, g2, 0, 0);
            latexPreview.setImage(SwingFXUtils.toFXImage(image, null));
        } catch (ParseException e) {
            showErrorAlert(e.getClass().toString(), e.getMessage());
        }
//        File file = new File("Example1.png");
//        try {
//            ImageIO.write(image, "png", file.getAbsoluteFile());
//        } catch (IOException ex) { }

    }

    private static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error Message");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
