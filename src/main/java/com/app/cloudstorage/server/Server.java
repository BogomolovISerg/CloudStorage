package com.app.cloudstorage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.app.cloudstorage.common.*;

public class Server {
    private static Logger logger = LogManager.getLogger(Server.class);

    public Server() {

    }

    public void start(int port) throws Throwable {

        logger.info("Старт сервера, порт " + port);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new OutboundMessageSplitter());
                            socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(Setting.MAX_FRAME_BODY_LENGTH, 1, 4));
                            socketChannel.pipeline().addLast(new MessageTypeDecoder());
                            socketChannel.pipeline().addLast(new FinalHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(port).sync();
            logger.info("Сервер запущен");
            future.channel().closeFuture().sync();
        } finally {
            logger.info("Сервер не запустился");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
