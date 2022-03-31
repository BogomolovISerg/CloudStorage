package com.app.cloudstorage.client;

import io.netty.buffer.ByteBuf;
import com.app.cloudstorage.common.FileService;

import java.io.IOException;

class ClientFileService extends FileService {

    private static final ClientFileService instance = new ClientFileService();

    public static ClientFileService getInstance() {
        return instance;
    }


    public void doStore(Runnable callback) throws Exception {
        sendFile(dataOutput, NetworkHandler.getInstance().getChannel(), callback);
    }

    @Override
    public void receiveData(ByteBuf data) throws IOException {
        if (dataInput == null)
            throw new RuntimeException("Неверный блок данных");

        int l = data.readableBytes();
        if (l > length)
            throw new RuntimeException("В блоке данных больше, чем ожидается");
        data.readBytes(buffer, 0, l);
        fos.write(buffer, 0, l);
        fos.flush();
        length -= l;
        if (length == 0) {
            setDataInput(null);
            MainService.getInstance().getFetchSuccess().run();
        }
    }
}

