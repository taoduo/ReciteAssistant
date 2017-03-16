package sample;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class Controller {
    @FXML
    private TextField latexInput;

    @FXML
    public void addBtnClick() {
        System.out.println(latexInput.getText());
    }
}
