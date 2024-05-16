package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.measure.ObservableMeasurementTableData;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionListener;
import qupath.lib.gui.viewer.QuPathViewer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;

import static java.lang.Math.round;
import static qupath.lib.gui.scripting.QPEx.getQuPath;
import static qupath.lib.scripting.QP.*;
import qupath.lib.gui.commands.Commands;
import java.util.ResourceBundle;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox implements PathObjectSelectionListener{

    public Text my_Label;
    private boolean is_Names_Display = true;

    /**
     * Logger user to save report the error into logs
     */
    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    /**
     * Create a stage for the renameAnnotation view
     */
    private Stage renameAnnotationStage;


    /**
     * The current orientation of the toolbar
     * true means vertical
     * false means horizontal
     */
    private static boolean currentOrientation = true;//vertical

    @FXML
    private void toggleToolbarOrientation() {
        boolean newOrientation = !currentOrientation;
        String fxmlPath = "/qupath/ext/ergonomictoolbar/ui/" + (newOrientation ? "VerticalInterface.fxml" : "HorizontalInterface.fxml");

        //Recreate the extension with the new orientation
        Stage stage = ErgonomicToolBarExtension.getSharedStage(newOrientation);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        try {
            AnchorPane mainPane = loader.load();
            Scene scene = new Scene(mainPane);

            // Update orientation for the next toggle
            currentOrientation = newOrientation;

            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.show();
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
        try {
            if (renameAnnotationStage == null) {

                var url = InterfaceController.class.getResource("RenameAnnotation.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                renameAnnotationStage = new Stage();
                Scene scene = new Scene(loader.load());
                renameAnnotationStage.setScene(scene);
                renameAnnotationStage.initStyle(StageStyle.UTILITY);
                renameAnnotationStage.setResizable(false);
                //renameAnnotationStage.setAlwaysOnTop(true);
                initRenameAnnotationSetAlwaysOnTop();
                renameAnnotationStage.show();
            }
            else if (!renameAnnotationStage.isShowing()) {
                renameAnnotationStage.show();
            }
        }
        catch (IOException e) {
            Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
            logger.error("Unable to load extension interface FXML", e);
        }
    }

    public void initRenameAnnotationSetAlwaysOnTop()
    {
        Stage quPathStage = QuPathGUI.getInstance().getStage();

        quPathStage.focusedProperty().addListener((observableValue, onHidden, onShown) -> {
            if(onHidden || renameAnnotationStage.isFocused())
            {
                renameAnnotationStage.setAlwaysOnTop(false);
            }
            if(onShown)
            {
                renameAnnotationStage.setAlwaysOnTop(true);
            }
        });
    }

    // Cette méthode permets d'afficher ou de cacher le nom de toutes les annotations en fonction de si elles le sont déjà ou non.
    public void display_Or_Hide_Names() {
        is_Names_Display = !is_Names_Display;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setShowNames(is_Names_Display);
    }

    /**
     * Return area when annotations have been selected
     */
    @Override
    public void selectedPathObjectChanged(PathObject pathObjectSelected, PathObject previousObject, Collection<PathObject> allSelected) {

        if (pathObjectSelected == null){
            my_Label.setText("...");
        }
        else {
            // Here goes your selection change logic
            ImageData<BufferedImage> imageData = getCurrentImageData();

            Collection<PathObject> tissues = getAnnotationObjects();
            ObservableMeasurementTableData ob = new ObservableMeasurementTableData();
            ob.setImageData(imageData, tissues);
            String area = "Area µm^2";
            double annotationArea = ob.getNumericValue(getCurrentHierarchy().getSelectionModel().getSelectedObject(), area);
            String magnitude;
            double aire;
            if (annotationArea < 10000){
                aire = (double) round(annotationArea*100)/100;
                magnitude = " μm²";
            }
            else{
                double roundArea = (double) round(annotationArea/1000)*1000;
                aire= roundArea / 1000000;
                magnitude = " mm²";
            }

            my_Label.setText( aire + magnitude);
        }
    }

    public InterfaceController() throws IOException {

        //Pour éviter les problèmes si aucune image n'est ouverte
        //Demandera de rafraichir l'extension (en changeant l'orientation par exemple)
        if(getQuPath() != null){
            if(getQuPath().getImageData() != null){
                getQuPath().getImageData().getHierarchy().getSelectionModel().addPathObjectSelectionListener(this);
            }

        }

    }

    public static InterfaceController createInstance() throws IOException {
        return new InterfaceController();
    }

    /**
     * Sauvegarde l'image actuelle
     */
    public void save_Project(){

        if(getQuPath() != null) {
            if (getQuPath().getImageData() != null) {
                Commands.promptToSaveImageData(getQuPath(),getQuPath().getImageData(),true);
            }
        }
    }


    @FXML
    private void toggleLockAnnotation() {
        QuPathGUI quPathGUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPathGUI.getViewer();
        if (viewer != null && viewer.getSelectedObject() != null) {
            var selectedObject = viewer.getSelectedObject();
            boolean isCurrentlyLocked = selectedObject.isLocked();
            selectedObject.setLocked(!isCurrentlyLocked);

            // Forcer la mise à jour de l'annotation pour refléter le changement dans l'interface utilisateur
            if (viewer.getHierarchy() != null) {
                viewer.getHierarchy().fireHierarchyChangedEvent(selectedObject);
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Annotation Lock Error");
            alert.setContentText("No annotation is selected.");
            alert.showAndWait();
        }
    }





}
