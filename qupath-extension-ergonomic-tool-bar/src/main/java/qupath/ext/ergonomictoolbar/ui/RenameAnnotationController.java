package qupath.ext.ergonomictoolbar.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
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

import java.io.IOException;

public class RenameAnnotationController extends AnchorPane{

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private QP quPathApplication;

    @FXML
    private Spinner<Integer> threadSpinner;


    @FXML
    private TextField nameTextField;

    @FXML
    private Label errorLabel;

    @FXML
    void validateNewName() {
        //Stage test = (Stage) nameTextField.getScene().getWindow();
        //System.out.println("test : ");
        try
        {
            String newName = nameTextField.getText();
            if(newName.isEmpty())
            {
                errorLabel.setText("Nom invalide.");
            }
            else
            {
                PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();
                if(hierarchy == null) {
                    errorLabel.setText("Aucun fichier n'est ouvert.");
                }
                else
                {
                    PathObjectSelectionModel selectionModel = hierarchy.getSelectionModel();
                    if(selectionModel != null)
                    {
                        PathObject object = selectionModel.getSelectedObject();
                        if(object == null) {
                            errorLabel.setText("Aucune annotation n'est sélectionnée.");
                        }
                        else
                        {
                            errorLabel.setText("");
                            nameTextField.setText("");

                            //Modification du nom de l'annotation sélectionnée
                            object.setName(newName);

                            //Actualisation des noms d'annotation dans QuPath
                            QP.refreshIDs();


                            //Pour fermer la fenêtre automatiquement une fois le nom de l'annotation actualisé

                            Stage stage = (Stage) nameTextField.getScene().getWindow();

                            stage.close();
                        }
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
