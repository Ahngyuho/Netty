package com.netty_projext.simplenettyserver.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetSocketAddress;

public class EchoServer {

    private static final int SERVER_PORT = 11011;

    private final ChannelGroup allChannels = new DefaultChannelGroup("server",
            GlobalEventExecutor.INSTANCE);

    private EventLoopGroup boosEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;

    public void startServer(){
        boosEventLoopGroup = new NioEventLoopGroup(1,new DefaultThreadFactory("boos"));
        workerEventLoopGroup = new NioEventLoopGroup(1,new DefaultThreadFactory("worker"));

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boosEventLoopGroup,workerEventLoopGroup);

        bootstrap.channel(NioServerSocketChannel.class);

        bootstrap.childOption(ChannelOption.TCP_NODELAY,true);
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE,true);

        bootstrap.childHandler(new EchoServerInitializer());

        try{
            ChannelFuture bindFuture = bootstrap.bind(new InetSocketAddress(SERVER_PORT)).sync();
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            close();
            
        }
    }

    private void close() {

        allChannels.close().awaitUninterruptibly();
        workerEventLoopGroup.shutdownGracefully().awaitUninterruptibly();
        boosEventLoopGroup.shutdownGracefully().awaitUninterruptibly();
    }

    public static void main(String[] args) throws Exception{
        new EchoServer().startServer();
    }

}
