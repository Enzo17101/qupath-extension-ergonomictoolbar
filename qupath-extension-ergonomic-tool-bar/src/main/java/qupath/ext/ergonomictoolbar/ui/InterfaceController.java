package qupath.ext.ergonomictoolbar.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathApp;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.Commands;
import qupath.lib.gui.measure.ObservableMeasurementTableData;
import qupath.lib.gui.viewer.QuPathViewerListener;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionListener;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.tools.PathTool;
import qupath.lib.gui.viewer.tools.PathTools;
import qupath.lib.gui.viewer.tools.handlers.PathToolEventHandlers;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjects;
import qupath.lib.regions.ImagePlane;
import qupath.lib.regions.ImageRegion;
import qupath.lib.roi.ROIs;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.PixelCalibration;
import qupath.lib.roi.interfaces.ROI;
import qupath.lib.scripting.QP;
import qupath.lib.scripting.QP;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
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

    public Text areaLabel;
    public Text areaLabel1;

    private boolean isFillingDisplayed = true;
    private boolean is_Names_Display = true;

    private int rectangular_selection_width = 10000;
    private int rectangular_selection_height = 10000;
    private boolean rectangular_selection_size_in_pixels = true;

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");
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
     * true means vertical
     * false means horizontal
     */
    private static boolean currentOrientation = true;//vertical

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
        if(getProject() != null)
        {
            try {
                if (renameAnnotationStage == null) {
                    var url = InterfaceController.class.getResource("RenameAnnotation.fxml");
                    FXMLLoader loader = new FXMLLoader(url);
                    loader.setController(new RenameAnnotationController());

                    renameAnnotationStage = new Stage();
                    Scene scene = new Scene(loader.load());

                    renameAnnotationStage.setScene(scene);

                    renameAnnotationStage.initStyle(StageStyle.UTILITY);
                    renameAnnotationStage.setResizable(false);

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
        // If there is no project we display an error message.
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No open projects.");
            alert.setHeaderText(null);
            alert.setContentText("Please open a project before using this function.");
            alert.showAndWait();
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

    /**
     * This method allows to display or hide filling of all the annotations according to their current state.
     */
    public void display_Or_Hide_Filling() {
        isFillingDisplayed = !isFillingDisplayed;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setFillAnnotations(isFillingDisplayed);
    }

    /**
     * This method allows to display or hide names of all the annotations according to their current state.
     */
    public void display_Or_Hide_Names() {
        is_Names_Display = !is_Names_Display;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setShowNames(is_Names_Display);
    }

    /**
     * This method locks or unlocks the currently selected ROI (Region of Interest).
     * It toggles the lock status of the selected annotation in the viewer.
     */
    @FXML
    private void lockUnlockROI() {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        // If the viewer exists and an object is currently selected
        if (viewer != null && viewer.getSelectedObject() != null) {
            // Get the currently selected object
            PathObject selectedObject = viewer.getSelectedObject();

            // Check the lock status
            boolean isLocked = selectedObject.isLocked();

            // Reverse the lock status
            selectedObject.setLocked(!isLocked);

            // Update the display
            imageData.getHierarchy().fireHierarchyChangedEvent(this);
            viewer.repaint();
        }
    }

    /**
     * This method activates the tool to create a polygon ROI (Region of Interest) in the viewer.
     * It sets the active tool to the predefined polygon tool.
     */
    @FXML
    private void createPolygonROI() {
        // Get the current instance of QuPathGUI
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        // If the viewer exists
        if (viewer != null) {
            // If you are already set to the polygon ROI tool
            if (viewer.getActiveTool() == PathTools.POLYGON) {
                // Switch to the move tool
                viewer.setActiveTool(PathTools.MOVE);
                gui.getToolManager().setSelectedTool(PathTools.MOVE);
            } else { // Otherwise
                // Set the active tool to the polygon ROI tool
                viewer.setActiveTool(PathTools.POLYGON);
                gui.getToolManager().setSelectedTool(PathTools.POLYGON);
            }
        }
    }

    /**
     * This method activates the tool to create a rectangular ROI (Region of Interest) in the viewer.
     * It sets the active tool to the predefined rectangle tool.
     */
    @FXML
        private void createRectangleROI() {
        // Get the current instance of QuPathGUI
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        // If the viewer exists
        if (viewer != null) {
            // If you are already set to the rectangle ROI tool
            System.out.println(gui.getToolManager().getSelectedTool());
            if (viewer.getActiveTool() == PathTools.RECTANGLE) {
                // Switch to the move tool
                viewer.setActiveTool(PathTools.MOVE);
                gui.getToolManager().setSelectedTool(PathTools.MOVE);
            } else { // Otherwise
                // Set the active tool to the rectangle ROI tool
                viewer.setActiveTool(PathTools.RECTANGLE);
                gui.getToolManager().setSelectedTool(PathTools.RECTANGLE);
            }
        }
    }

    /**
     * This method creates a rectangular ROI (Region of Interest) with predefined dimensions.
     * It calculates the rectangle's dimensions in pixels, centers it in the viewer,
     * and creates an annotation with the specified size.
     */
    @FXML
    private void createPredefinedSizedRectangularROI() {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        // If the image data exists
        if (imageData != null) {
            // Dimensions of the rectangle
            int widthInPixels, heightInPixels;

            // If dimensions are already in pixels
            if (rectangular_selection_size_in_pixels) {
                widthInPixels = rectangular_selection_width;
                heightInPixels = rectangular_selection_height;
            } else { // Convert dimensions from millimeters to pixels
                PixelCalibration cal = imageData.getServer().getPixelCalibration();
                widthInPixels = (rectangular_selection_width / (int)cal.getPixelWidthMicrons()) * 1000;
                heightInPixels = (rectangular_selection_height / (int)cal.getPixelHeightMicrons()) * 1000;
            }

            // Coordinates of the center of the viewer
            int centerX = (int)viewer.getCenterPixelX();
            int centerY = (int)viewer.getCenterPixelY();

            // Calculate the starting coordinates of the rectangle
            int x = centerX - widthInPixels / 2;
            int y = centerY - heightInPixels / 2;

            // Create an instance of ImageRegion
            ImageRegion imageRegion = ImageRegion.createInstance(x, y, widthInPixels, heightInPixels, 0, 0);

            // Use the createRectangleROI method to create the ROI
            ROI rectangleROI = ROIs.createRectangleROI(imageRegion);

            // Create an annotation from the ROI
            PathObject annotation = PathObjects.createAnnotationObject(rectangleROI);

            // Add the annotation to the image
            imageData.getHierarchy().addObject(annotation);

            // Update the display
            imageData.getHierarchy().fireHierarchyChangedEvent(this);
            viewer.repaint();
        }

        // Switch to the move tool
        viewer.setActiveTool(PathTools.MOVE);
        gui.getToolManager().setSelectedTool(PathTools.MOVE);
    }

    @FXML
    private void createZoomRectangularROI() {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        // If the image data exists
        if (imageData != null) {
            // Get the width and height of the viewer
            int viewerWidth = viewer.getServerWidth();
            int viewerHeight = viewer.getServerHeight();

            // Get the width and height of the visible area on the canvas
            int visibleWidth = (int) viewer.getView().getWidth();
            int visibleHeight = (int) viewer.getView().getHeight();

            // Get the current downsample factor
            double downsampleFactor = viewer.getDownsampleFactor();

            // Convert the visible dimensions to image coordinates using the downsample factor
            int imageWidth = (int) (visibleWidth * downsampleFactor);
            int imageHeight = (int) (visibleHeight * downsampleFactor);

            // Dimensions of the ROI
            int ROIWidth = imageWidth / 2;
            int ROIHeight = imageHeight / 2;

            // Calculate the starting coordinates of the rectangle
            int x = (int) viewer.getCenterPixelX() - ROIWidth / 2;
            if (x + ROIWidth > viewerWidth) x = viewerWidth - ROIWidth;
            if (x < 0) x = 0;

            int y = (int) viewer.getCenterPixelY() - ROIHeight / 2;
            if (y + ROIHeight > viewerHeight) y = viewerHeight - ROIHeight;
            if (y < 0) y = 0;

            // Create an instance of ImageRegion
            ImageRegion imageRegion = ImageRegion.createInstance(x, y, ROIWidth, ROIHeight, 0, 0);

            // Use the createRectangleROI method to create the ROI
            ROI rectangleROI = ROIs.createRectangleROI(imageRegion);

            // Create an annotation from the ROI
            PathObject annotation = PathObjects.createAnnotationObject(rectangleROI);

            // Add the annotation to the image
            imageData.getHierarchy().addObject(annotation);

            // Update the display
            imageData.getHierarchy().fireHierarchyChangedEvent(this);
            viewer.repaint();
        }

        // Switch to the move tool
        viewer.setActiveTool(PathTools.MOVE);
        gui.getToolManager().setSelectedTool(PathTools.MOVE);
    }

    /**
     * @return The stage set class annotation scene
     */
    public Stage getSharedSetClassAnnotationStage() {
        if (set_Class_Annotation_Stage == null) {
            set_Class_Annotation_Stage = new Stage();
            set_Class_Annotation_Stage.setResizable(false);
            set_Class_Annotation_Stage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return set_Class_Annotation_Stage;
    }

    /**
     * This method allows to open the stage for set the class of an annotation.
     * @throws IOException exception during the opening of a stage.
     */
    public void set_Class_Annotation_Stage() throws IOException {

        QP quPathApplication;

        // We check if the stage is null or not in order to not display it twice.
        if (set_Class_Annotation_Stage == null) {

            // We check if a project is opened or not.
            if(QP.getProject() != null ){
                try {
                    // We opened the stage.
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
            // If there is no project we display an error message.
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No open projects.");
                alert.setHeaderText(null);
                alert.setContentText("Please open a project before using this function.");
                alert.showAndWait();
            }
        }
        else if(!set_Class_Annotation_Stage.isShowing())
        {
            set_Class_Annotation_Stage.show();
        }
    }

    /**
     * Return area when annotations have been selected
     */
    @Override
    public void selectedPathObjectChanged(PathObject pathObjectSelected, PathObject previousObject, Collection<PathObject> allSelected) {

        if (pathObjectSelected == null){
            areaLabel.setText("...");
            areaLabel1.setText("");
            System.out.println("...");
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
            areaLabel1.setText(magnitude);
            System.out.println(aire);
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
}
