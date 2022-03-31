package com.app.cloudstorage.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

class ClientCommandService{

    private static final ClientCommandService instance = new ClientCommandService();
    protected static final Logger logger = LogManager.getLogger(ClientCommandService.class);

    public static ClientCommandService getInstance() {
        return instance;
    }

    private String expectedResponse = null;

    private Consumer<List<String>> listDataConsumer;

    public void parseAndExecute(String input) throws Exception {
        String[] split = input.split("[\\s\n]", 2);
        String command = split[0];

        assertResponseExpected(command);

        switch (command) {
            case "AUTH-RESP":
                if (split[1].equals("OK")) {
                    MainService.getInstance().setAuthorized(true);
                    MainService.getInstance().getAuthSuccess().run();
                } else {
                    MainService.getInstance().setAuthorized(false);
                    MainService.getInstance().getAuthFailure().accept("Авторизация не удалась. Ответ сервера: " + split[1]);
                }
                break;

            case "LIST-RESP":
                String[] fileNames = split[1].split("\n");
                listDataConsumer.accept(Arrays.asList(fileNames));
                break;

            case "FETCH-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    ClientFileService.getInstance().setDataInput(null);
                    MainService.getInstance().getFetchFailure().accept("Получить не удалось. Ответ сервера: " + split[1]);
                } else {
                    ClientFileService.getInstance().setDataLength(Long.parseLong(split[1].split(" ")[2]));
                }
                break;

            case "STORE-RESP":
                if (!split[1].split(" ")[0].equals("OK")) {
                    ClientFileService.getInstance().setDataSource(null);
                    MainService.getInstance().getStoreFailure().accept("Ошибка записи. Ответ сервера: " + split[1]);
                } else
                    ClientFileService.getInstance().doStore(MainService.getInstance().getStoreSuccess());
                break;

            case "REMOVE-RESP":
                if (!split[1].equals("OK")) {
                    MainService.getInstance().getDeleteFailure().accept("Ошибка удаления файла. Ответ сервера: " + split[1]);
                } else {
                    MainService.getInstance().getDeleteSuccess().run();
                }
                break;
        }
    }

    public void expectResponse(String s) {
        logger.debug("Ожидание ответа сервера " + s);
        expectedResponse = s;
    }

    public void assertResponseExpected(String response) {
        if (!response.equals(expectedResponse)) {
            logger.error("Получен неожиданный ответ от сервера: " + response);
            throw new RuntimeException("Неожиданный ответ от сервера: " + response);
        }
    }

    public void setListDataConsumer(Consumer<List<String>> listDataConsumer) {
        this.listDataConsumer = listDataConsumer;
    }
}

