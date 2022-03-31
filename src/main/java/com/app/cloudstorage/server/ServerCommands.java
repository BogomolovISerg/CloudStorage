package com.app.cloudstorage.server;

import com.app.cloudstorage.common.FileService;
import io.netty.channel.Channel;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ServerCommands {

    private FileService fileService;
    private String userName;
    private Path workingDir;

    private void authorize(String login) {
        userName = login;
        workingDir = Paths.get("remote", userName);
    }

    private boolean isAuthorized() {
        return userName != null;
    }

    public ServerCommands(FileService fileService){
        this.fileService = fileService;
        this.userName = null;
        this.workingDir = null;
    }

    public void parseAndExecute(String input, Channel channel) throws Exception {
        String[] split = input.split(" ");
        String command = split[0];

        switch (command) {
            case "AUTH":
                if (split.length == 3) {
                    if (Database.isGoodConnectUser(split[1], split[2])) {
                        channel.writeAndFlush("AUTH-RESP OK");
                        authorize(split[1]);
                    } else
                        channel.writeAndFlush("AUTH-RESP BAD-CREDENTIALS");
                } else
                    channel.writeAndFlush("AUTH-RESP SYNTAX-ERROR");
                break;

            case "LIST":
                if (!isAuthorized()) {
                    channel.writeAndFlush("LIST-RESP AUTH-REQUIRED");
                    break;
                }
                if (Files.isRegularFile(workingDir))
                    throw new RuntimeException("Существует файл " + workingDir + ". Каталог не создать.");
                if (Files.notExists(workingDir))
                    Files.createDirectory(workingDir);
                List<Path> files = Files.list(workingDir)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList());
                StringBuilder sb = new StringBuilder("LIST-RESP\n");
                for (Path f : files)
                    sb.append(f.getFileName()).append("\n");
                channel.writeAndFlush(sb.toString());
                break;

            case "FETCH":
                if (!isAuthorized()) {
                    channel.writeAndFlush("FETCH-RESP AUTH-REQUIRED");
                    break;
                }
                String filenameFetch = input.split(" ", 2)[1];
                Path pathFetch = workingDir.resolve(filenameFetch);
                if (Files.notExists(pathFetch)) {
                    channel.writeAndFlush("FETCH-RESP NOT-FOUND");
                } else {
                    File file = pathFetch.toFile();
                    channel.writeAndFlush("FETCH-RESP OK " + file.getName() + " " + file.length());
                    FileService.sendFile(file, channel, null);
                }
                break;

            case "STORE":
                if (!isAuthorized()) {
                    channel.writeAndFlush("STORE-RESP AUTH-REQUIRED");
                    break;
                }
                String filenameStore = split[1];
                long fileLength;
                try {
                    fileLength = Long.parseLong(split[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing command: " + input);
                    channel.writeAndFlush("SYNTAX-ERROR");
                    break;
                }
                fileService.setDataInput(workingDir.resolve(filenameStore).toFile());
                fileService.setDataLength(fileLength);
                channel.writeAndFlush("STORE-RESP OK");
                break;

            case "REMOVE":
                if (!isAuthorized()) {
                    channel.writeAndFlush("REMOVE-RESP AUTH-REQUIRED");
                    break;
                }
                String filenameRemove = input.split(" ", 2)[1];
                Path pathRemove = workingDir.resolve(filenameRemove);
                if (filenameRemove.isEmpty() || Files.notExists(pathRemove)) {
                    channel.writeAndFlush("REMOVE-RESP NOT-FOUND");
                } else {
                    Files.delete(pathRemove);
                    channel.writeAndFlush("REMOVE-RESP OK");
                }
                break;
        }
    }
}
