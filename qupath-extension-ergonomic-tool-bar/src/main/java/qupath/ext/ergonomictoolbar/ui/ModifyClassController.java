package qupath.ext.ergonomictoolbar.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ModifyClassController implements Initializable{

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private QP quPathApplication;

    @FXML
    private Spinner<Integer> threadSpinner;

    @FXML
    private ComboBox<String> class_ComboBox;

    @FXML
    private Label errorLabel;

    public static ModifyClassController createInstance() throws IOException {
        return new ModifyClassController();
    }

    public ModifyClassController() throws IOException {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        update_ComboBox();
    }

    /**
     * Method that allows to actualize the comboBox with all the current classes from QuPath.
     */
    @FXML
    void update_ComboBox() {
        errorLabel.setText("");

        // We load all the classes from QuPath.
        List<PathClass> path_Classes = QP.getProject().getPathClasses();
        List<String> class_Names = new ArrayList<>();

        // We add the name of the classes.
        for(PathClass path_Class : path_Classes){
            class_Names.add(path_Class.getName());
        }

        // We add the names to the comboBox.
        class_ComboBox.setItems(FXCollections.observableArrayList(class_Names));
    }

    /**
     * Method for modifying the class of an annotation.
     * @param event Event that led to this method.
     */
    @FXML
    void confirm_New_Class(ActionEvent event) {
        PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();

        //Check that an image has been opened.
        if(hierarchy == null) {
            errorLabel.setText("No file are open.");
        }
        else
        {
            PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();
            if(selectionModel != null)
            {
                PathObject object = selectionModel.getSelectedObject();

                //Check that an annotation has been selected.
                if(object == null) {
                    errorLabel.setText("No annotations are selected.");
                }
                else
                {
                    //Check that a class has been selected.
                    if(class_ComboBox.getValue() != null){
                        errorLabel.setText("");

                        //Modify the class of the selected annotation.
                        object.setPathClass(PathClass.fromString(class_ComboBox.getValue()));

                        //Refresh annotation properties in QuPath
                        QP.refreshIDs();

                        //Close the window once the annotation is updated
                        Stage stage = (Stage) class_ComboBox.getScene().getWindow();
                        stage.close();
                    }

                    else {
                        errorLabel.setText("Please select a class.");
                    }
                }
            }
        }
    }


}