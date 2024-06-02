package qupath.ext.ergonomictoolbar.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.PixelCalibration;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.objects.utils.Tiler;
import qupath.lib.scripting.QP;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CreateAnnotationController extends AnchorPane {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private PathObject object;

    @FXML
    private ComboBox<String> classComboBox;

    @FXML
    private CheckBox lockCheckBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private CheckBox tiledCheckBox;

    @FXML
    private Label errorLabel;

    /**
     * Width and height of a tile, in mm
     */
    private double tileSize = 0.1;

    public void setObject(PathObject object)
    {
        this.object = object;
    }

    @FXML
    private void initialize() {
        updateWindow();
    }


    /**
     * Method that allows to actualize the comboBox with all the current classes from QuPath.
     */
    @FXML
    void updateWindow() {

        errorLabel.setText("");

        nameTextField.setText("");

        // We load all the classes from QuPath.
        List<PathClass> path_Classes = QP.getProject().getPathClasses();
        List<String> class_Names = new ArrayList<>();

        // We add the name of the classes.
        for(PathClass path_Class : path_Classes){
            class_Names.add(path_Class.getName());
        }

        // We add the names to the comboBox.
        classComboBox.setItems(FXCollections.observableArrayList(class_Names));

        lockCheckBox.setSelected(!InterfaceController.getSharedPredefinedAnnotationCreated());
        tiledCheckBox.setSelected(false);
    }

    @FXML
    void createAnnotation() {
        try
        {
            PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();

            //Check that an image has been opened.
            if(hierarchy == null)
            {
                errorLabel.setText("No file are open.");
            }
            else
            {
                PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();

                //Check that an annotation has been selected.
                if(selectionModel != null)
                {
                    String newName = nameTextField.getText();

                    //Check that a class has been selected.
                    if(object == null) {
                        errorLabel.setText("No annotations are selected.");
                    }
                    else if(newName.isEmpty())
                    {
                        errorLabel.setText("Invalid name.");
                    }
                    else if(classComboBox.getValue() == null)
                    {
                        errorLabel.setText("Please select a class.");
                    }
                    else
                    {
                        errorLabel.setText("");

                        //Modify the name of the selected annotation.
                        object.setName(newName);

                        //Modify the class of the selected annotation.
                        object.setPathClass(PathClass.fromString(classComboBox.getValue()));

                        //Modify the lock property of the selected annotation.
                        object.setLocked(lockCheckBox.isSelected());

                        //Create a tiled annotation with a "0.1mm" width/height
                        if(tiledCheckBox.isSelected())
                        {
                            ImageData<BufferedImage> imageData = QuPathGUI.getInstance().getImageData();

                            PixelCalibration cal = imageData.getServer().getPixelCalibration();

                            int tileWidth = (int)(tileSize*1000/cal.getPixelWidthMicrons());
                            int tileHeight = (int)(tileSize*1000/cal.getPixelHeightMicrons());

                            Tiler.Builder tilerBuilder = Tiler.builder(tileWidth, tileHeight);

                            Tiler tiler = tilerBuilder.build();

                            List<PathObject> tiles = tiler.createTiles(object.getROI());

                            imageData.getHierarchy().addObjects(tiles);
                        }

                        //Refresh annotation properties in QuPath
                        QP.refreshIDs();

                        //Close the window once the annotation is updated
                        Stage stage = InterfaceController.getSharedCreateAnnotationStage();
                        stage.close();
                    }
                }
            }
        }
        catch (Exception e)
        {
            Dialogs.showErrorMessage("Extension Error", "Cannot rename annotation");
            logger.error("Cannot rename annotation", e);
        }
    }
}
