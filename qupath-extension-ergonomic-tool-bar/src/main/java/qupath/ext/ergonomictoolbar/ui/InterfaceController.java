package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.text.Text;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.Commands;
import qupath.lib.gui.measure.ObservableMeasurementTableData;
import qupath.lib.gui.viewer.QuPathViewerListener;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionListener;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.scripting.QP;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import static java.lang.Math.round;
import static qupath.lib.gui.scripting.QPEx.getQuPath;
import static qupath.lib.scripting.QP.*;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox implements PathObjectSelectionListener, QuPathViewerListener {


    /**
     * Labels for the area display
     */
    public Text areaLabel;
    public Text areaMagnitudeLabel;

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");

    /**
     * Logger user to save report the error into logs
     */
    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    /**
     * Stage for the renameAnnotation view
     */
    private static Stage renameAnnotationStage;

    /**
     * Stage for the modifyClass view
     */
    private static Stage modifyClassStage;

    /**
     * The current orientation of the toolbar
     * true means vertical
     * false means horizontal
     */
    private static boolean currentOrientation = true;//vertical by default

    @FXML
    private void toggleToolbarOrientation() {
        boolean newOrientation = !currentOrientation;
        String fxmlPath = "/qupath/ext/ergonomictoolbar/ui/" + (newOrientation ? "VerticalInterface.fxml" : "HorizontalInterface.fxml");

        try {
            //Recreate the extension with the new orientation
            Stage stage = ErgonomicToolBarExtension.getSharedStage(newOrientation);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            loader.setController(new InterfaceController());

            Scene scene = new Scene(loader.load());

            // Update orientation for the next toggle
            currentOrientation = newOrientation;

            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.show();

            if(getCurrentHierarchy() != null)
            {
                if(getCurrentHierarchy().getSelectionModel().getSelectedObject() != null)
                {
                    getCurrentHierarchy().getSelectionModel().setSelectedObject(getCurrentHierarchy().getSelectionModel().getSelectedObject());
                }
            }
        } catch (IOException e) {
            Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
            logger.error("Unable to load extension interface FXML", e);
        }
    }

    /**
     * @return the rename annotation stage
     */
    public static Stage getSharedRenameAnnotationStage() {
        if (renameAnnotationStage == null) {
            renameAnnotationStage = new Stage();
            renameAnnotationStage.setResizable(false);
            renameAnnotationStage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return renameAnnotationStage;
    }

    @FXML
    private void renameAnnotation() {

        if(getProject() != null)
        {
            if(getCurrentHierarchy() != null)
            {
                if(getCurrentHierarchy().getSelectionModel().getSelectedObject() != null)
                {
                    try {
                        if (renameAnnotationStage == null) {
                            // If the renameAnnotation window doesn't exist, we create it
                            var url = InterfaceController.class.getResource("RenameAnnotation.fxml");
                            FXMLLoader loader = new FXMLLoader(url);
                            loader.setController(new RenameAnnotationController());

                            renameAnnotationStage = new Stage();

                            Scene scene = new Scene(loader.load());
                            renameAnnotationStage.setScene(scene);

                            renameAnnotationStage.initStyle(StageStyle.UTILITY);
                            renameAnnotationStage.setResizable(false);

                            Stage quPathStage = QuPathGUI.getInstance().getStage();

                            //It is set alway on top only if the application is showing
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
                            renameAnnotationStage.show();
                        }
                        else {
                            renameAnnotationStage.show();
                        }
                    }
                    catch (IOException e) {
                        Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
                        logger.error("Unable to load extension interface FXML", e);
                    }
                }
                else
                {
                    noAnnotation();
                }
            }
            else
            {
               noFile();
            }
        }
        else
        {
            noProject();
        }
    }

    /**
     * This method allows to display or hide filling of all the annotations according to their current state.
     */
    public void displayOrHideFilling()
    {
        QuPathViewer viewer = QuPathGUI.getInstance().getViewer();

        if(viewer != null)
        {
            boolean fillingDisplay = !QuPathGUI.getInstance().getOverlayOptions().getFillAnnotations();
            viewer.getOverlayOptions().setFillAnnotations(fillingDisplay);
        }
    }

    /**
     * This method allows to display or hide names of all the annotations according to their current state.
     */
    public void displayOrHideNames()
    {
        QuPathViewer viewer = QuPathGUI.getInstance().getViewer();

        if(viewer != null)
        {
            boolean nameDisplay = !QuPathGUI.getInstance().getOverlayOptions().getShowNames();
            viewer.getOverlayOptions().setShowNames(nameDisplay);
        }
    }

    /**
     * @return The modify class stage
     */
    public Stage getSharedSetClassAnnotationStage() {
        if (modifyClassStage == null) {
            modifyClassStage = new Stage();
            modifyClassStage.setResizable(false);
            modifyClassStage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return modifyClassStage;
    }

    /**
     * This method allows to open the stage for set the class of an annotation.
     * @throws IOException exception during the opening of a stage.
     */
    public void set_Class_Annotation_Stage() throws IOException {

        QP quPathApplication;

        // We check if the stage is null or not in order to not display it twice.
        if (modifyClassStage == null) {

            // We check if a project is opened or not.
            if(QP.getProject() != null ){
                try {
                    // We opened the stage.
                    var url = InterfaceController.class.getResource("ModifyClass.fxml");
                    FXMLLoader loader = new FXMLLoader(url);
                    modifyClassStage = new Stage();
                    Scene scene = new Scene(loader.load());
                    modifyClassStage.setScene(scene);
                    modifyClassStage.setAlwaysOnTop(true);
                    modifyClassStage.show();
                } catch (IOException e) {
                    Dialogs.showErrorMessage("Extension Error", "GUI loading failed");
                    logger.error("Unable to load extension interface FXML", e);
                }
            }
            // If there is no project we display an error message.
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No open projects.");
                alert.setHeaderText(null);
                alert.setContentText("Please open a project before using this function.");
                alert.showAndWait();
            }
        }
        else if(!modifyClassStage.isShowing())
        {
            modifyClassStage.show();
        }
    }

    /**
     * Return area when annotations have been selected
     */
    @Override
    public void selectedPathObjectChanged(PathObject pathObjectSelected, PathObject previousObject, Collection<PathObject> allSelected) {

        if (pathObjectSelected == null){
            areaLabel.setText("...");
            areaMagnitudeLabel.setText("");
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

            areaLabel.setText(String.valueOf(aire));
            areaMagnitudeLabel.setText(magnitude);
        }
    }

    public InterfaceController() throws IOException {
        if(getQuPath() != null) {
            getQuPath().getViewer().addViewerListener(this);
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

    @Override
    public void imageDataChanged(QuPathViewer viewer, ImageData<BufferedImage> imageDataOld, ImageData<BufferedImage> imageDataNew) {
        //Pour éviter les problèmes si aucune image n'est ouverte
        //Demandera de rafraichir l'extension (en changeant l'orientation par exemple)
        if(getQuPath() != null){
            if(getQuPath().getImageData() != null){
                getQuPath().getImageData().getHierarchy().getSelectionModel().removePathObjectSelectionListener(this);
                getQuPath().getImageData().getHierarchy().getSelectionModel().addPathObjectSelectionListener(this);
            }

        }
    }

    //We herit from an abstract class so we have to define those methods
    @Override
    public void visibleRegionChanged(QuPathViewer viewer, Shape shape) {

    }

    //We herit from an abstract class so we have to define those methods
    @Override
    public void selectedObjectChanged(QuPathViewer viewer, PathObject pathObjectSelected) {

    }

    //We herit from an abstract class so we have to define those methods
    @Override
    public void viewerClosed(QuPathViewer viewer) {

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
            alert.setHeaderText(null);
            alert.setContentText("No annotation is selected.");
            alert.showAndWait();
        }
    }

    public void noProject()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No projects open");
        alert.setHeaderText(null);
        alert.setContentText("Please open a project before using this function.");
        alert.showAndWait();
    }

    public void noFile()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No files open");
        alert.setHeaderText(null);
        alert.setContentText("Please open a file before using this function.");
        alert.showAndWait();
    }

    public void noAnnotation()
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("No annotation selected");
        alert.setHeaderText(null);
        alert.setContentText("Please select an annotation before using this function.");
        alert.showAndWait();
    }
}
