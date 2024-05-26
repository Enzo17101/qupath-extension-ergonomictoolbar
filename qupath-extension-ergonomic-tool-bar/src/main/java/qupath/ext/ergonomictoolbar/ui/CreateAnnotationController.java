package qupath.ext.ergonomictoolbar.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;

public class CreateAnnotationController extends AnchorPane {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private PathObject object;

    @FXML
    private ComboBox<?> classComboBox;

    @FXML
    private CheckBox lockCheckBox;

    @FXML
    private TextField nameTextField;

    @FXML
    private CheckBox tiledCheckBox;

    public void setObject(PathObject object)
    {
        this.object = object;
    }

    @FXML
    void createAnnotation() {
        try
        {
            QP quPathApplication;

            PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();

            if(hierarchy == null)
            {
                System.out.println("null hierarchy");
            }
            else
            {
                PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();

                if(selectionModel != null)
                {
                    String newName = nameTextField.getText();

                    if(object == null) {
                        System.out.println("null annotation");
                    }
                    else if(newName.isEmpty())
                    {
                        System.out.println("Invalid name.");
                    }
                    else
                    {
                        //errorLabel.setText("");

                        //Modification du nom de l'annotation sélectionnée
                        object.setName(newName);

                        //Actualisation des noms d'annotation dans QuPath
                        QP.refreshIDs();

                        //Pour fermer la fenêtre automatiquement une fois le nom de l'annotation actualisé
                        //Stage stage = (Stage) nameTextField.getScene().getWindow();
                        Stage stage = InterfaceController.getSharedCreateAnnotationStage();
                        stage.close();

                        nameTextField.setText("");
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
