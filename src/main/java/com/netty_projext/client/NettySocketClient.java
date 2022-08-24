package com.netty_projext.client;

import com.netty_projext.Handler.NettySocketClientHandler;
import com.netty_projext.server.NettySocketServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettySocketClient {
    private String msg;
    private String host;
    private int port;

    public NettySocketClient(String host, int port, String msg){
        this.msg = msg;
        this.host = host;
        this.port = port;
    }

    public void run(){
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new NettySocketClientHandler(msg));
                }
            });

            //client connect
            try {
                ChannelFuture f = b.connect(host, port).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }finally {
            workerGroup.shutdownGracefully();
        }
    }

}
