package qupath.ext.ergonomictoolbar.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.measure.ObservableMeasurementTableData;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionListener;

import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import static java.lang.Math.round;
import static qupath.lib.gui.scripting.QPEx.getQuPath;
import static qupath.lib.scripting.QP.*;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox implements PathObjectSelectionListener {
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");

    @FXML
    private Spinner<Integer> threadSpinner;
    private QuPathGUI qupath;

    public static InterfaceController createInstance() throws IOException {


        return new InterfaceController();
    }

     public InterfaceController() throws IOException {
        /*var url = InterfaceController.class.getResource("interface.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        //loader.setRoot(this);
        loader.setController(this);
        loader.load();

        // For extensions with a small number of options,
        // or with options that are very important for how the extension works,
        // it may be better to present them all to the user in the main extension GUI,
        // binding them to GUI elements, so they are updated when the user interacts with
        // the GUI, and so that the GUI elements are updated if the preference changes
        threadSpinner.getValueFactory().valueProperty().bindBidirectional(ErgonomicToolBarExtension.numThreadsProperty());
        threadSpinner.getValueFactory().valueProperty().addListener((observableValue, oldValue, newValue) -> {
            Dialogs.showInfoNotification(
                    resources.getString("title"),
                    String.format(resources.getString("threads"), newValue));
        });*/
         System.out.println("test 1");
         qupath = getQuPath();
         qupath.getImageData().getHierarchy().getSelectionModel().addPathObjectSelectionListener(this);

    }

    @FXML
    private void runDemoExtension() {
        System.out.println("Demo extension run");



    }

    @Override
    public void selectedPathObjectChanged(PathObject pathObjectSelected, PathObject previousObject, Collection<PathObject> allSelected) {
        // Here goes your selection change logic

        if (pathObjectSelected == null){
            //label.setText("No selection");
            System.out.println("No selection");
        }
        else {
            ImageData imageData = getCurrentImageData();

            Collection<PathObject> tissues = getAnnotationObjects();
            ObservableMeasurementTableData ob = new ObservableMeasurementTableData();
            ob.setImageData(imageData, tissues);
            String area = "Area µm^2";
            double annotationArea = ob.getNumericValue(getCurrentHierarchy().getSelectionModel().getSelectedObject(), area);
            //label.setText("Names: " + (annotationArea/1000000) + "\n");
            double roundArea = (double) round(annotationArea/1000)*1000;
            System.out.println("Aires: " + roundArea/1000000 + "\n");
        }
    }
}
