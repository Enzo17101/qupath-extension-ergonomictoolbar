package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    /**
     * Logger user to save report the error into logs
     */
    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    /**
     * Create a stage for the renameAnnotation view
     */
    private Stage renameAnnotationStage;

    private Stage set_Class_Annotation_Stage;

    /**
     * The current orientation of the toolbar
     */
    private static String currentOrientation = "vertical";


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


    /**
     * @return the stage rename annotation scene
     */
    public Stage getSharedRenameAnnotationStage() {
        if (renameAnnotationStage == null) {
            renameAnnotationStage = new Stage();
            renameAnnotationStage.setResizable(false);
            renameAnnotationStage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return renameAnnotationStage;
    }

    @FXML
    private void renameAnnotation() {
        if (renameAnnotationStage == null) {
            try {
                var url = InterfaceController.class.getResource("RenameAnnotation.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                renameAnnotationStage = new Stage();
                Scene scene = new Scene(loader.load());
                renameAnnotationStage.setScene(scene);
                renameAnnotationStage.setAlwaysOnTop(true);
                renameAnnotationStage.show();
            } catch (IOException e) {
                Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
                logger.error("Unable to load extension interface FXML", e);
            }
        }
        else if(!renameAnnotationStage.isShowing())
        {
            renameAnnotationStage.show();
        }
    }

    // Cette méthode permets d'afficher ou de cacher le nom de toutes les annotations en fonction de si elles le sont déjà ou non.
    public void display_Or_Hide_Names() {
        is_Names_Display = !is_Names_Display;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setShowNames(is_Names_Display);
    }

    public Stage getSharedSetClassAnnotationStage() {
        if (set_Class_Annotation_Stage == null) {
            set_Class_Annotation_Stage = new Stage();
            set_Class_Annotation_Stage.setResizable(false);
            set_Class_Annotation_Stage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return set_Class_Annotation_Stage;
    }

    public void set_Class_Annotation_Stage() throws IOException {

        if (set_Class_Annotation_Stage == null) {
            try {
                var url = InterfaceController.class.getResource("ModifyClass.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                set_Class_Annotation_Stage = new Stage();
                Scene scene = new Scene(loader.load());
                set_Class_Annotation_Stage.setScene(scene);
                set_Class_Annotation_Stage.setAlwaysOnTop(true);
                set_Class_Annotation_Stage.show();
            } catch (IOException e) {
                Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
                logger.error("Unable to load extension interface FXML", e);
            }
        }
        else if(!set_Class_Annotation_Stage.isShowing())
        {
            set_Class_Annotation_Stage.show();
        }
    }
}
