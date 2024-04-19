package qupath.ext.ergonomictoolbar.ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.regions.ImageRegion;
import qupath.lib.roi.RectangleROI;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.PixelCalibration;
import qupath.lib.roi.RoiTools;
import qupath.lib.roi.interfaces.ROI;

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
    private void lock_Or_Unlock_Selection() {
        // On récupère l'instance actuelle de QuPathGUI et le viewer
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        if (viewer != null && viewer.getSelectedObject() != null) {
            PathObject selectedObject = viewer.getSelectedObject();

            // On vérifie si l'objet est déjà verrouillé
            boolean isLocked = selectedObject.isLocked();

            // On inverse l'état de verrouillage
            selectedObject.setLocked(!isLocked);

            // On met à jour l'affichage pour refléter le changement
            viewer.repaint();
        }
    }

    @FXML
    private void create_predefined_sized_rectangular_selection(int width, int height, boolean inPixels) {
        // On récupère l'instance actuelle de QuPathGUI et les données de l'image
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();
        ImageData<?> imageData = gui.getImageData();

        double widthInPixels, heightInPixels;

        // Si les dimensions ne sont pas déjà en pixel alors on les convertit en pixels
        if (inPixels) {
            widthInPixels = width;
            heightInPixels = height;
        } else {
            PixelCalibration cal = imageData.getServer().getPixelCalibration();

            widthInPixels = (width / cal.getPixelWidthMicrons()) * 1000;
            heightInPixels = (height / cal.getPixelHeightMicrons()) * 1000;
        }

        if (viewer != null) {
            // Définir la région masquée pour positionner le rectangle
            // Utiliser toute l'image comme masque, vous pouvez l'ajuster selon vos besoins
            ImageRegion mask = ImageRegion.createInstance(0, 0, (int) imageData.getServer().getWidth(), (int) imageData.getServer().getHeight(), 0, 0);

            /*
            // Créer un rectangle aléatoire dans le masque
            try {
                ROI rectangle = qupath.lib.roi.RoiTools.createRandomRectangle(mask, widthInPixels, heightInPixels);
                PathAnnotationObject annotation = new PathAnnotationObject(rectangle);

                // Ajouter l'annotation à l'image
                imageData.getHierarchy().addPathObject(annotation);
                gui.getViewer().repaint();
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors de la création du rectangle : " + e.getMessage());
            }*/
        }
    }
}
