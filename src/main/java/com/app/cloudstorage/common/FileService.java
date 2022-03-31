package com.app.cloudstorage.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.stream.ChunkedNioFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.*;

public class FileService implements Closeable {

    protected static final Logger logger = LogManager.getLogger(FileService.class);
    protected static final int FILE_CHUNK_SIZE = Setting.MAX_FRAME_BODY_LENGTH / 2;
    protected File dataInput;
    protected File dataOutput;
    protected long length;
    protected FileOutputStream fos;
    protected final byte[] buffer = new byte[Setting.MAX_FRAME_BODY_LENGTH];

    public void setDataLength(long length){
        logger.debug("Установлена длина " + length);
        this.length = length;
    }

    public static void sendFile(File file, Channel channel, Runnable callback) throws Exception {
        logger.trace("Отправка файла = " + file.toPath().toAbsolutePath().toString());
        ChunkedNioFile chunkedNioFile = null;

        try{
            chunkedNioFile = new ChunkedNioFile(file, FILE_CHUNK_SIZE);

            while (!chunkedNioFile.isEndOfInput()){
                ByteBuf next = chunkedNioFile.readChunk(ByteBufAllocator.DEFAULT);
                channel.writeAndFlush(next);
            }
            logger.info("Файл отправлен " + file.toPath().toAbsolutePath());
            if (callback != null){
                logger.trace("sendFile() callback run");
                callback.run();
            }
        }finally{
            if(chunkedNioFile != null)
                chunkedNioFile.close();
        }
    }

    public void setDataInput(File dataInput) throws FileNotFoundException {
        if (fos != null) {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.dataInput = dataInput;
        if (dataInput != null) {
            this.fos = new FileOutputStream(dataInput);
        }else
            logger.debug("Данных нет");
    }

    public void setDataSource(File dataOutput) throws FileNotFoundException{
        this.dataOutput = dataOutput;
        if (dataOutput != null && !dataOutput.exists()){
            logger.warn("Исходный файл " + dataOutput.toPath().toAbsolutePath().toString() + " не существует");
        }
    }

    public void receiveData(ByteBuf data) throws IOException{
        if(dataInput == null)
            throw new RuntimeException("Неверный блок");

        int l = data.readableBytes();
        if(l > length)
            throw new RuntimeException("Данных больше, чем было передано предварительно");
        data.readBytes(buffer, 0, l);
        fos.write(buffer, 0, l);
        fos.flush();
        length -= l;
        if (length == 0){
            setDataInput(null);
        }

    }

    @Override
    public void close() throws IOException {
        if(fos != null)
            fos.close();
    }
}

