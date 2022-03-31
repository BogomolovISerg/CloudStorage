package com.app.cloudstorage.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class MainService {

    private static final MainService instance = new MainService();

    public static MainService getInstance() {
        return instance;
    }

    private static final Logger logger = LogManager.getLogger(MainService.class);

    private NetworkHandler networkHandler = NetworkHandler.getInstance();
    private ClientCommandService commandService = ClientCommandService.getInstance();
    private ClientFileService fileService = ClientFileService.getInstance();

    private boolean connected = false;
    private boolean authorized = false;

    public void connect(String serverAddress, int serverPort) { ;
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                networkHandler.launch(latch, serverAddress, serverPort, new IncomingDataReader());
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }).start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        connected = true;
    }

    public void disconnect() {
        networkHandler.stop();
        connected = false;
    }

    public void fetch(String fileName, Runnable successCallback, Consumer<String> failureCallback) throws FileNotFoundException {
        checkConnection();
        File file = Paths.get("local", fileName).toFile();
        if (file.exists())
            file.delete();
        setFetchSuccess(successCallback);
        setFetchFailure(failureCallback);
        fileService.setDataInput(file);
        commandService.expectResponse("FETCH-RESP");
        networkHandler.getChannel().writeAndFlush("FETCH" + file.getName());
    }

    private void checkConnection() {
        if (!connected)
            throw new IllegalStateException("Нет соединения");
    }

    public void store(String fileName, Runnable successCallback, Consumer<String> failureCallback) throws FileNotFoundException {
        checkConnection();
        File file = Paths.get("local", fileName).toFile();
        if (!file.exists())
            throw new FileNotFoundException("Попытка сохранить несуществующий файл: " + file.toString());
        setStoreSuccess(successCallback);
        setStoreFailure(failureCallback);
        fileService.setDataSource(file);
        commandService.expectResponse("STORE-RESP");
        networkHandler.getChannel().writeAndFlush("STORE " + file.getName() + " " + file.length());
    }

    public void delete(String fileName, Runnable successCallback, Consumer<String> failureCallback) {
        checkConnection();
        commandService.expectResponse("REMOVE-RESP");
        setDeleteSuccess(successCallback);
        setDeleteFailure(failureCallback);
        networkHandler.getChannel().writeAndFlush("REMOVE " + fileName);
    }

    public void rename(String fileName) {
        checkConnection();
        // TODO
        commandService.expectResponse("RENAME-RESP");
        networkHandler.getChannel().writeAndFlush("RENAME " + fileName);
    }

    public void list(Consumer<List<String>> consumer) {
        checkConnection();
        commandService.expectResponse("LIST-RESP");
        commandService.setListDataConsumer(consumer);
        networkHandler.getChannel().writeAndFlush("LIST");
    }

    private Runnable storeSuccess;
    private Consumer<String> storeFailure;
    private Runnable fetchSuccess;
    private Consumer<String> fetchFailure;
    private Runnable deleteSuccess;
    private Consumer<String> deleteFailure;
    private Runnable authSuccess;
    private Consumer<String> AuthFailure;

    public void setStoreSuccess(Runnable storeSuccess) {
        this.storeSuccess = storeSuccess;
    }

    public void setStoreFailure(Consumer<String> storeFailure) {
        this.storeFailure = storeFailure;
    }

    public void setFetchSuccess(Runnable fetchSuccess) {
        this.fetchSuccess = fetchSuccess;
    }

    public void setFetchFailure(Consumer<String> fetchFailure) {
        this.fetchFailure = fetchFailure;
    }

    public void setDeleteSuccess(Runnable deleteSuccess) {
        this.deleteSuccess = deleteSuccess;
    }

    public void setDeleteFailure(Consumer<String> deleteFailure) {
        this.deleteFailure = deleteFailure;
    }

    public void setAuthSuccess(Runnable authSuccess) {
        this.authSuccess = authSuccess;
    }

    public void setAuthFailure(Consumer<String> authFailure) {
        AuthFailure = authFailure;
    }

    public Runnable getStoreSuccess() {
        return storeSuccess;
    }

    public Consumer<String> getStoreFailure() {
        return storeFailure;
    }

    public Runnable getFetchSuccess() {
        return fetchSuccess;
    }

    public Consumer<String> getFetchFailure() {
        return fetchFailure;
    }

    public Runnable getDeleteSuccess() {
        return deleteSuccess;
    }

    public Consumer<String> getDeleteFailure() {
        return deleteFailure;
    }

    public Runnable getAuthSuccess() {
        return authSuccess;
    }

    public Consumer<String> getAuthFailure() {
        return AuthFailure;
    }

    public void auth(String login, String password, Runnable successCallback, Consumer<String> failureCallback) {
        checkConnection();
        commandService.expectResponse("AUTH-RESP");
        setAuthSuccess(successCallback);
        setAuthFailure(failureCallback);
        networkHandler.getChannel().writeAndFlush("AUTH " + login + " " + password);
    }

    public boolean getAuthorized() {
        return authorized;
    }

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
    }
}

