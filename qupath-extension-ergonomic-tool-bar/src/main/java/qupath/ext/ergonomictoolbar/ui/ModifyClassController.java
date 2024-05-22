package qupath.ext.ergonomictoolbar.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Spinner;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClass.StandardPathClasses;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;


import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class ModifyClassController {

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

    /**
     * Method that allows to actualize the comboBox with all the current classes from QuPath.
     * @param event Event that led to this method.
     */
    @FXML
    void update_ComboBox(ActionEvent event) {
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

        // Check that an image has been opened.
        if(hierarchy == null) {
            errorLabel.setText("No file are open.");
        }
        else
        {
            PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();
            if(selectionModel != null)
            {
                PathObject object = selectionModel.getSelectedObject();

                // Check that an annotation has been selected.
                if(object == null) {
                    errorLabel.setText("No annotations are selected");
                }
                else
                {
                    // We check that a class has been selected.
                    if(class_ComboBox.getValue() != null){
                        errorLabel.setText("");

                        // Modify the class of the selected annotation.
                        object.setPathClass(PathClass.fromString(class_ComboBox.getValue()));

                        // Update annotation names in QuPath.
                        QP.refreshIDs();

                        // To close the window automatically once the annotation name has been updated
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