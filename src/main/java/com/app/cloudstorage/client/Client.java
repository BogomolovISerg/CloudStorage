package com.app.cloudstorage.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.app.cloudstorage.common.Setting;

public class Client extends Application{

    static final String SERVER_IP_ADDRESS = "localhost";
    static final int SERVER_PORT = Setting.PORT;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mainWindow.fxml"));
        scene = new Scene(fxmlLoader.load());
        MainWindowController controller = fxmlLoader.getController();
        controller.init();
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args){
        MainService.getInstance().connect(SERVER_IP_ADDRESS, SERVER_PORT);
        launch();
    }
}
