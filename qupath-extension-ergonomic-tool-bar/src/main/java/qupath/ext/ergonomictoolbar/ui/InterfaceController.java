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
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathApp;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.Commands;
import qupath.lib.gui.measure.ObservableMeasurementTableData;
import qupath.lib.gui.panes.PathObjectHierarchyView;
import qupath.lib.gui.viewer.QuPathViewerListener;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.events.PathObjectHierarchyEvent;
import qupath.lib.objects.hierarchy.events.PathObjectHierarchyListener;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionListener;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.scripting.QP;
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;

import static java.lang.Math.round;
import static qupath.lib.gui.scripting.QPEx.getQuPath;
import static qupath.lib.scripting.QP.*;
import static qupath.lib.scripting.QP.getCurrentHierarchy;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox implements PathObjectSelectionListener, QuPathViewerListener, PathObjectHierarchyListener {


    /**
     * Labels for the area display
     */
    public Text areaLabel;
    public Text areaMagnitudeLabel;

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ext.ergonomictoolbar.ui.strings");

    private int rectangular_selection_width = 10000;
    private int rectangular_selection_height = 10000;
    private boolean rectangular_selection_size_in_pixels = true;

    /**
     * Logger user to save report the error into logs
     */
    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    /**
     * Stage for the createAnnotation view
     */
    private static Stage createAnnotationStage;

    private static CreateAnnotationController createAnnotationController;

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


    /**
     * true when the user is creating an annotation
     */
    private static boolean inCreation = false;

    /**
     * number of annotation currently loaded
     */
    private static int annotationNumber;

    public InterfaceController() throws IOException {
        if(getQuPath() != null) {
            getQuPath().getViewer().addViewerListener(this);
            if(getQuPath().getImageData() != null){
                getQuPath().getImageData().getHierarchy().getSelectionModel().addPathObjectSelectionListener(this);
                getQuPath().getImageData().getHierarchy().addListener(this);
            }
        }
    }

    @FXML
    private void initialize()
    {
        annotationNumber = 0;
        if(getCurrentHierarchy() != null)
        {
            annotationNumber = getCurrentHierarchy().getAnnotationObjects().size();
        }

        createAnnotationController = new CreateAnnotationController();
    }

    /**
     * @return the create annotation stage
     */
    public static Stage getSharedCreateAnnotationStage() {
        if (createAnnotationStage == null) {
            createAnnotationStage = new Stage();
            createAnnotationStage.setResizable(false);
            createAnnotationStage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return createAnnotationStage;
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

    /**
     * @return The modify class stage
     */
    public static Stage getSharedSetClassAnnotationStage() {
        if (modifyClassStage == null) {
            modifyClassStage = new Stage();
            modifyClassStage.setResizable(false);
            modifyClassStage.initStyle(StageStyle.UTILITY); // Change this as needed
        }
        return modifyClassStage;
    }


    @Override
    public void imageDataChanged(QuPathViewer viewer, ImageData<BufferedImage> imageDataOld, ImageData<BufferedImage> imageDataNew) {
        //Pour éviter les problèmes si aucune image n'est ouverte
        //Demandera de rafraichir l'extension (en changeant l'orientation par exemple)
        System.out.println("image changed");
        if(getQuPath() != null){
            if(getQuPath().getImageData() != null){
                getQuPath().getImageData().getHierarchy().getSelectionModel().removePathObjectSelectionListener(this);
                getQuPath().getImageData().getHierarchy().getSelectionModel().addPathObjectSelectionListener(this);

                getQuPath().getImageData().getHierarchy().removeListener(this);
                getQuPath().getImageData().getHierarchy().addListener(this);
            }
        }
    }

    //We herit from an abstract class so we have to define those methods
    @Override
    public void visibleRegionChanged(QuPathViewer viewer, Shape shape) {

    }


    //We herit from an abstract class so we have to define those methods
    @Override
    public void viewerClosed(QuPathViewer viewer) {

    }

    @Override
    public void hierarchyChanged(PathObjectHierarchyEvent event) {
        if(event.getEventType().equals(PathObjectHierarchyEvent.HierarchyEventType.ADDED))
        {
            //Permit to avoid function call when we move an annotation
            if(annotationNumber < getCurrentHierarchy().getAnnotationObjects().size())
            {
                createAnnotation(event.getChangedObjects().get(event.getChangedObjects().size() - 1));
            }
        }
    }


    //We herit from an abstract class so we have to define those methods
    @Override
    public void selectedObjectChanged(QuPathViewer viewer, PathObject pathObjectSelected) {

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

        //TODO REFACTOR
        annotationNumber = getCurrentHierarchy().getAnnotationObjects().size();



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

    /**
     * Sauvegarde l'image actuelle
     */
    public void saveProject(){
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
            noAnnotation();
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
            getCurrentHierarchy().getSelectionModel().setSelectedObject(annotation);
        }

        // Switch to the move tool
        viewer.setActiveTool(PathTools.MOVE);
        gui.getToolManager().setSelectedTool(PathTools.MOVE);
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
            getCurrentHierarchy().getSelectionModel().setSelectedObject(annotation);
        }

        // Switch to the move tool
        viewer.setActiveTool(PathTools.MOVE);
        gui.getToolManager().setSelectedTool(PathTools.MOVE);

    }




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

    private void createAnnotation(PathObject object) {

        if(getProject() != null)
        {
            if(getCurrentHierarchy() != null)
            {
                if(object != null)
                {
                    try {
                        if (createAnnotationStage == null) {
                            // If the renameAnnotation window doesn't exist, we create it
                            var url = InterfaceController.class.getResource("CreateAnnotation.fxml");
                            FXMLLoader loader = new FXMLLoader(url);

                            loader.setController(createAnnotationController);
                            createAnnotationController.setObject(object);

                            createAnnotationStage = new Stage();

                            Scene scene = new Scene(loader.load());
                            createAnnotationStage.setScene(scene);

                            createAnnotationStage.initStyle(StageStyle.UTILITY);
                            createAnnotationStage.setResizable(false);

                            Stage quPathStage = QuPathGUI.getInstance().getStage();

                            //It is set alway on top only if the application is showing
                            quPathStage.focusedProperty().addListener((observableValue, onHidden, onShown) -> {
                                if(onHidden || createAnnotationStage.isFocused())
                                {
                                    createAnnotationStage.setAlwaysOnTop(false);
                                }
                                if(onShown)
                                {
                                    createAnnotationStage.setAlwaysOnTop(true);
                                }
                            });
                            createAnnotationStage.show();
                        }
                        else {
                            createAnnotationController.setObject(object);
                            createAnnotationStage.show();
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
}
