package qupath.ext.ergonomictoolbar.ui;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
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

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Controller for UI pane contained in interface.fxml
 */

public class InterfaceController extends VBox {

    private boolean is_Names_Display = true;

    private int rectangular_selection_width = 100;
    private int rectangular_selection_height = 100;
    private boolean rectangular_selection_size_in_pixels = true;

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
        try {
            if (renameAnnotationStage == null) {

                var url = InterfaceController.class.getResource("RenameAnnotation.fxml");
                FXMLLoader loader = new FXMLLoader(url);
                renameAnnotationStage = new Stage();
                Scene scene = new Scene(loader.load());
                renameAnnotationStage.setScene(scene);
                renameAnnotationStage.setAlwaysOnTop(true);
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

    // Cette méthode permets d'afficher ou de cacher le nom de toutes les annotations en fonction de si elles le sont déjà ou non.
    public void display_Or_Hide_Names() {
        is_Names_Display = !is_Names_Display;

        QuPathGUI quPath_GUI = QuPathGUI.getInstance();
        QuPathViewer viewer = quPath_GUI.getViewer();
        viewer.getOverlayOptions().setShowNames(is_Names_Display);
    }

    // Cette méthode permet de verrouiller ou déverouiller une annotation
    @FXML
    private void lockUnlockROI() {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        // if the viewer exists and an object is currently selected
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

    @FXML
    private void createPredefinedSizedRectangularROI() {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        int widthInPixels, heightInPixels;

        // dimensions are already in pixels
        if (rectangular_selection_size_in_pixels) {
            widthInPixels = rectangular_selection_width;
            heightInPixels = rectangular_selection_height;
        } else { // dimensions have to be converted from millimeters to pixels
            PixelCalibration cal = imageData.getServer().getPixelCalibration();

            widthInPixels = (rectangular_selection_width / (int)cal.getPixelWidthMicrons()) * 1000;
            heightInPixels = (rectangular_selection_height / (int)cal.getPixelHeightMicrons()) * 1000;
        }

        // If the viewer exists
        if (viewer != null) {
            // Coordonnées du centre du viewer
            int centerX = (int)viewer.getCenterPixelX();
            int centerY = (int)viewer.getCenterPixelY();

            // Calculer les coordonnées de départ du rectangle
            int x = centerX - widthInPixels / 2;
            int y = centerY - heightInPixels / 2;
            /*
            // Créer une instance de ImageRegion
            ImageRegion imageRegion = ImageRegion.createInstance(x, y, widthInPixels, heightInPixels, 0, 0);

            // Utiliser la méthode createRectangleROI pour créer le ROI
            ROI rectangleROI = ROIs.createRectangleROI(imageRegion);

            // Créer l'annotation à partir du ROI
            PathObject annotation = PathObjects.createAnnotationObject(rectangleROI);

            // Ajouter l'annotation à l'image
            imageData.getHierarchy().addPathObject(annotation);
            */
            // Update the display
            imageData.getHierarchy().fireHierarchyChangedEvent(this);
            viewer.repaint();
        }
    }
}
