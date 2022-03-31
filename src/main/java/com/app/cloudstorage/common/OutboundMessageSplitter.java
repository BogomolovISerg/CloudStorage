package com.app.cloudstorage.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class OutboundMessageSplitter extends MessageToMessageEncoder<Object> {

    private static Logger logger = LogManager.getLogger(OutboundMessageSplitter.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, List<Object> list) throws Exception {
        if (o instanceof String) {
            String msg = (String) o;
            ByteBuf byteBuf;
            byte[] msgBytes = msg.getBytes(StandardCharsets.UTF_8);
            if (msgBytes.length > Setting.MAX_FRAME_BODY_LENGTH) {
                throw new RuntimeException("Команда большая по длине.");
            }
            byteBuf = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + msgBytes.length);
            byteBuf.writeByte(Setting.COMMAND_SIGNAL_BYTE);
            byteBuf.writeInt(msgBytes.length);
            byteBuf.writeBytes(msgBytes);
            channelHandlerContext.writeAndFlush(byteBuf);
        } else {
            ByteBuf in = (ByteBuf) o;
            sendSplitDataFrames(in, list);
        }
    }

    private void sendSplitDataFrames(ByteBuf in, List<Object> out) {
        while (in.readableBytes() > 0) {
            int frameBodyLength = Math.min(in.readableBytes(), Setting.MAX_FRAME_BODY_LENGTH);
            ByteBuf outFrame = ByteBufAllocator.DEFAULT.buffer(1 + 4 + frameBodyLength);
            outFrame.writeByte(Setting.DATA_SIGNAL_BYTE);
            outFrame.writeInt(frameBodyLength);
            outFrame.writeBytes(in, frameBodyLength);
            out.add(outFrame);
        }
    }
}