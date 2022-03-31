package com.app.cloudstorage.server;

import com.app.cloudstorage.common.FileService;
import com.app.cloudstorage.common.Setting;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FinalHandler extends ChannelInboundHandlerAdapter{

    private static final Logger logger = LogManager.getLogger(FinalHandler.class);
    private FileService fileService;
    private ServerCommands commandService;

    private final byte[] buffer = new byte[Setting.MAX_FRAME_BODY_LENGTH];

    public FinalHandler(){
        fileService = new FileService();
        commandService = new ServerCommands(fileService);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        logger.catching(cause);
        fileService.close();
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        if(msg instanceof String){
            String command = (String) msg;
            commandService.parseAndExecute(command, ctx.channel());
        }else{
            ByteBuf data = (ByteBuf) msg;
            fileService.receiveData(data);
        }
    }
}
