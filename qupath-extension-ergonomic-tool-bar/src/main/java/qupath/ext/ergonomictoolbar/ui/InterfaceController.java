package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox {

    private static String currentOrientation = "vertical";


    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");

    @FXML
    private Spinner<Integer> threadSpinner;

    public static InterfaceController createInstance() throws IOException {
        return new InterfaceController();
    }

    @FXML
    private void toggleToolbarOrientation() {
        //System.out.println("Current orientation before toggle: " + currentOrientation);
        Stage stage = ErgonomicToolBarExtension.getSharedStage();
        if (stage.isShowing()) {
            stage.close();
        }

        String newOrientation = "horizontal".equals(currentOrientation) ? "vertical" : "horizontal";
        String fxmlPath = "/qupath/ext/ergonomictoolbar/ui/" + (newOrientation.equals("vertical") ? "VerticalInterface.fxml" : "HorizontalInterface.fxml");

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        try {
            AnchorPane mainPane = loader.load();
            Scene scene = new Scene(mainPane);
            stage.setScene(scene);
            stage.show();

            // Update orientation for the next toggle
            currentOrientation = newOrientation;
            //System.out.println("New orientation after toggle: " + currentOrientation);
        } catch (IOException e) {
            Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
        }
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
    }

    @FXML
    private void runDemoExtension() {
        System.out.println("Demo extension run");
    }


}
