package qupath.ext.ergonomictoolbar.ui;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.ext.ergonomictoolbar.ErgonomicToolBarExtension;
import qupath.fx.dialogs.Dialogs;
import javafx.scene.layout.AnchorPane;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClass.StandardPathClasses;
import qupath.lib.objects.hierarchy.PathObjectHierarchy;
import qupath.lib.objects.hierarchy.events.PathObjectSelectionModel;
import qupath.lib.scripting.QP;


import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class CalculateNecrosisRateController {

    private static final Logger logger = LoggerFactory.getLogger(ErgonomicToolBarExtension.class);

    private QP quPathApplication;

    @FXML
    private Label label_Necrosis_Rate;

    @FXML
    private TextField text_Distance, folder_Path, file_Name;

    @FXML
    private CheckBox check_Graph_Display;

    private String display_Graph = "0";

    public static CalculateNecrosisRateController createInstance() throws IOException {
        return new CalculateNecrosisRateController();
    }

    public CalculateNecrosisRateController() throws IOException {
    }

    /**
     * Method that allows to create an alert of error.
     * @param title
     * @param text
     * @param owner
     * @return
     */
    public Alert create_Alert_Error(String title, String text, Window owner){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        if(owner != null)
            alert.initOwner(owner);

        return alert;
    }

    /**
     * Method that allows to create an alert of information.
     * @param title
     * @param text
     * @param owner
     * @return
     */
    public Alert create_Alert_Information(String title, String text, Window owner){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.initOwner(owner);

        return alert;
    }

    /**
     * Method that allows to know if the str is a double > to 0
     * @param str
     * @return
     */
    public boolean is_Dist_Ok(String str){
        try {
            double number = Double.parseDouble(str);
            if (number > 0) {
                return true;
            } else {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Method that allows to know if there is at least one tumor zone
     * @return
     */
    public boolean is_Tumor_Zone_Present() {

        PathObjectHierarchy hierarchy = QP.getCurrentHierarchy();
        Collection<PathObject> annotations = hierarchy.getAnnotationObjects();

        // Iterate through each annotation object
        for (PathObject annotation : annotations) {

            // Check if the name of the annotation is "Tumor zone"
            if (Objects.equals(annotation.getName(), "Tumor zone")) {
                return true;
            }
        }

        // Return false if no "Tumor zone" annotation is found
        return false;
    }

    /**
     * Method that allows to get the gravity centers of cells with a groovy script
     * @return Gravity centers cells
     */
    public String get_Gravity_Centers() {

        String groovyFilePath = "qupath-extension-ergonomic-tool-bar/src/main/java/qupath/ext/ergonomictoolbar/groovy/GetGravityCenters.groovy";

        // Redirect standard output to capture script's print statements
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        // Create a binding for Groovy script variables
        Binding binding = new Binding();
        binding.setVariable("out", printWriter);

        // Create a GroovyShell instance with the binding
        GroovyShell shell = new GroovyShell(binding);

        try {
            // Parse and run the Groovy script
            Script script = shell.parse(new File(groovyFilePath));
            script.run();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Return the captured output as a String
        return stringWriter.toString();
    }

    /**
     * Method that allows to get the rate necrosis from a python file and to display it with a graph or not
     * @param gravity_Centers_Cells
     * @param display_Graph
     */
    public void get_And_Display_Rate_Necrosis(String gravity_Centers_Cells, String display_Graph) {
        try {
            String file_Path = folder_Path.getText() + "\\" + file_Name.getText() + ".png";

            // Créer un fichier temporaire pour stocker les arguments
            File temp_File = File.createTempFile("args", ".txt");
            temp_File.deleteOnExit();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(temp_File))) {
                writer.write(gravity_Centers_Cells);
                writer.write(text_Distance.getText() + "\n");
                writer.write(display_Graph + "\n");
                writer.write(file_Path + "\n");
            }

            // Lire et afficher le contenu du fichier temporaire pour vérification
            try (BufferedReader reader = new BufferedReader(new FileReader(temp_File))) {
                String line;
                System.out.println("Contenu du fichier temporaire:");
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            // Commande pour exécuter le script Python avec le chemin du fichier temporaire comme argument
            String[] command = {
                    "python",
                    "qupath-extension-ergonomic-tool-bar/src/main/java/qupath/ext/ergonomictoolbar/python/CalculateNecrosisRate.py",
                    temp_File.getAbsolutePath()
            };

            // Create a ProcessBuilder instance
            ProcessBuilder pb = new ProcessBuilder(command);

            // Redirect error stream to the standard output stream
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read the process output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            label_Necrosis_Rate.setText("Results :");

            // Append each line of the process output to the label
            while ((line = reader.readLine()) != null) {
                label_Necrosis_Rate.setText(label_Necrosis_Rate.getText() + "\n" + line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method that allows to display the necrosis rate
     */
    public void display_Rate_Necrosis() {

        // Get the current stage from the scene of the check_Graph_Display control
        Stage currentStage = (Stage) check_Graph_Display.getScene().getWindow();

        // Check if there is at least one tumor zone present
        if (is_Tumor_Zone_Present()) {

            // Check if the distance entered is valid.
            if (is_Dist_Ok(text_Distance.getText())) {

                if((folder_Path.getText().isEmpty() || file_Name.getText().isEmpty() )&& check_Graph_Display.isSelected()){
                    Alert alert_Error_File_Or_Folder_Name = create_Alert_Error("File or folder error","You must fill the file's name and the folder's path in order to save the graphe.",currentStage);
                    alert_Error_File_Or_Folder_Name.show();
                } else {
                    // Display a waiting message.
                    Alert alert_Waiting_Message = create_Alert_Information("Calcul started","Calculate in progress ... Please wait until the end.",currentStage);
                    alert_Waiting_Message.show();

                    Task<Void> task = new Task<>() {
                        @Override
                        protected Void call() {
                            if (check_Graph_Display.isSelected()) {
                                display_Graph = "1";
                            } else {
                                display_Graph = "0";
                            }
                            Platform.runLater(() -> {
                                String gravity_Centers = get_Gravity_Centers();
                                get_And_Display_Rate_Necrosis(gravity_Centers, display_Graph);
                            });
                            return null;
                        }

                        @Override
                        protected void succeeded() {

                            // Close the initial alert
                            alert_Waiting_Message.close();

                            Alert alert_Suceed_Thread = create_Alert_Information("Calculation Completed","The calculation is complete.",currentStage);
                            alert_Suceed_Thread.show();
                        }

                        @Override
                        protected void failed() {

                            // Close the initial alert
                            alert_Waiting_Message.close();

                            Alert alert_Error_Thread = create_Alert_Error("Calculation Failed","An error occurred during the calculation.",currentStage);
                            alert_Error_Thread.show();
                        }
                    };
                    String display_Graph = "0";
                    Thread thread = new Thread(task);
                    thread.setDaemon(true);
                    thread.start();
                }

            } else {
                Alert alert_Error_Distance = create_Alert_Error("Distance error.","Please fill a distance with a float superior to 0.",currentStage);

                // Display an error alert if distance input is invalid
                alert_Error_Distance.showAndWait();
            }
        } else {
            Alert alert_Error_Tumor_Zone = create_Alert_Error("Tumor zone error.","You must create at least one tumor zone.",currentStage);

            // Display an error alert if no tumor zone is present
            alert_Error_Tumor_Zone.showAndWait();
        }
    }

    /**
     * Method that allows to select a folder.
     */
    public void select_Folder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selec a folder");

        File selectedDirectory = directoryChooser.showDialog((Stage) check_Graph_Display.getScene().getWindow());

        if (selectedDirectory != null) {
            folder_Path.setText(selectedDirectory.getAbsolutePath());
        }
    }
}

