package com.neusoft.NettyDemo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @Title: IdleStateHandlerInitializer
 * @ProjectName Jdk8Train
 * @Description: TODO 处理心跳连接
 * @Author yisheng.wu
 * @Date 2020/5/2515:31
 */
public class IdleStateHandlerInitializer extends ChannelInitializer<Channel>
{
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        pipeline.addLast(new HeartbeatHandler());
    }

    /**
      * @Author: yisheng.wu
      * @Description TODO  实现userEventTriggered()方法以发送心跳消息
      * @Date 15:34 2020/5/25
      * @Param
      * @return
      **/
    public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {

        // 发送到远程节点的心跳消息
        private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer(
                "HEARTBEAT", CharsetUtil.ISO_8859_1));

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx,
                                       Object evt) throws Exception {
            /*8
                发送心跳消息，并在发送失败时关闭该连接
             */
            if (evt instanceof IdleStateEvent) {
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate()).addListener(
                        ChannelFutureListener.CLOSE_ON_FAILURE);
            } else {
                /*8
                    不是 IdleStateEvent事件，所以将它传递给下一个 ChannelInboundHandler
                 */
                super.userEventTriggered(ctx, evt);
            }
        }
    }
}