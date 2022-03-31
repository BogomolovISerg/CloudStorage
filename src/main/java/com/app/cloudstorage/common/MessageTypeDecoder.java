package com.app.cloudstorage.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class MessageTypeDecoder extends ByteToMessageDecoder{
    private static Logger logger = LogManager.getLogger(MessageTypeDecoder.class);

    public MessageTypeDecoder(){
        //        setSingleDecode(true);     }
    }


        @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (in.readableBytes() > 0){
                if (in.getByte(0) == Setting.COMMAND_SIGNAL_BYTE){
                    discardHeader(in);
                    out.add(in.toString(StandardCharsets.UTF_8));
                    in.skipBytes(in.readableBytes());
                }else if(in.getByte(0) == Setting.DATA_SIGNAL_BYTE){
                    discardHeader(in);
                    out.add(in);
                }else{
                    throw new CorruptedFrameException("Ожидается сигнальный байт перед фреймом, но его там нет.");
                }
            }
    }
    private void discardHeader(ByteBuf in){
        in.skipBytes(1 + 4);
        in.discardReadBytes();
    }
}
