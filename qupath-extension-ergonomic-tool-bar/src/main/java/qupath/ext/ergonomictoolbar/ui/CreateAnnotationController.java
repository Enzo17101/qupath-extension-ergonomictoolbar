package qupath.ext.ergonomictoolbar.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;

public class CreateAnnotationController extends AnchorPane {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    @FXML
    private ComboBox<?> classComboBox;

    @FXML
    private CheckBox lockCheckBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private CheckBox tiledCheckBox;

    @FXML
    void createAnnotation(ActionEvent event) {

    }

}
