package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox {

    private boolean is_Names_Display = true;

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");

    @FXML
    private Spinner<Integer> threadSpinner;

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
    }

    @FXML
    private void runDemoExtension() {
        System.out.println("Demo extension run");
    }

    // Cette méthode permets d'afficher ou de cacher le nom de toutes les annotations en fonction de si elles le sont déjà ou non.
    public void display_Or_Hide_Names() {
        is_Names_Display = !is_Names_Display;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setShowNames(is_Names_Display);
    }
}
