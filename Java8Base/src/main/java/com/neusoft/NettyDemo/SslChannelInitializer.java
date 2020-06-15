package com.neusoft.NettyDemo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;

/**
 * @Title: SslChannelInitializer
 * @ProjectName Jdk8Train
 * @Description: TODO 要想为 WebSocket 添加安全性，只需要将 SslHandler 作为第一个 ChannelHandler 添加到ChannelPipeline 中。
 * @Author yisheng.wu
 * @Date 2020/5/2514:36
 */
public class SslChannelInitializer extends ChannelInitializer<Channel> {

    private final SslContext context;

    private final boolean startTls;

    public SslChannelInitializer(SslContext context,
                                 boolean startTls) {
        this.context = context;
        this.startTls = startTls;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SSLEngine engine = context.newEngine(ch.alloc());
        ch.pipeline().addFirst("ssl",
                new SslHandler(engine, startTls));
    }
}
