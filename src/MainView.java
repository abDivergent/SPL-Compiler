import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MainView extends Application
{

    Label outputField;
    int count = 1;
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("SPL Compiler");

        GridPane mainView = new GridPane();
        mainView.setMinSize(350, 550);
        mainView.setPadding(new Insets(10,10,10,10));
        mainView.setVgap(5);
        mainView.setHgap(5);
        mainView.setAlignment(Pos.CENTER);

        outputField = new Label();
        outputField.alignmentProperty().set(Pos.TOP_LEFT);

//        outputField.setDisable(true);
        outputField.setPrefWidth(300);
        outputField.setPrefHeight(500);

        Button selectFileButton = new Button("Load file");
        selectFileButton.setAlignment(Pos.BOTTOM_LEFT);
        selectFileButton.setOnAction(event ->
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt"));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));

            File file = fileChooser.showOpenDialog(null);
            selectFileButton.setDisable(true);
            if(file != null )
            {
                try
                {
                    cLexer lexer = new cLexer(file.getAbsolutePath());
                    outputField.setText(outputField.getText() + "\n Performing Lexical Analysis");
                    cParser parser = new cParser(lexer.start());
                    outputField.setText(outputField.getText() + "\n Performing Syntactical Analysis");
                    parser.start();

                    outputField.setText(outputField.getText() + "\n Analysis completed without error");
                    writeToFile(parser.printTree(), count++);
                } catch (Exception e)
                {
                    outputField.setText(outputField.getText() + "\n Error found...");
                    writeToFile(e.getMessage(), count++);
                }
                outputField.setText(outputField.getText()+"\n-------------------------------------------------\n");
            }
            selectFileButton.setDisable(false);
        });

        mainView.add(outputField, 0, 0, 1,1);
        mainView.add(selectFileButton, 0,1,1,1);

        Scene scene = new Scene(mainView);
        primaryStage.setScene(scene);
        primaryStage.show();


    }
    private void writeToFile(String str, int i)
    {
        try
        {
            String filepath = System.getProperty("user.dir")+"\\results"+i+".txt";
            File resultFile = new File(filepath);
            resultFile.createNewFile();

            outputField.setText(outputField.getText() + "\n Saving Results to file\n");

            FileWriter myWriter = new FileWriter(filepath, false);
            myWriter.write(str);
            myWriter.close();
            outputField.setText(outputField.getText() + filepath);


        } catch (IOException e)
        {
            outputField.setText(outputField.getText() + "\n Error while writing to file");


        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
