package com.netty_projext.server;


import com.netty_projext.discard.EchoServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EchoServer {

    private int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        //ServerBootStrap 객체에 서버 애플리케이션이 사용할 두 스레드 그룹을 설정해야함
        //첫번째 스레드 그룹은 클라이언트의 연결을 수락하는 부모 스레드 그룹
        //두번째 스레드 그룹은 연결된 클라이언트의 소켓으로부터 데이터 입출력 및 이벤트 처리 담당하는 자식 스레드 그룹
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);    //여기서 생성자 안에 들어가는 숫자 1의 의미는
        //스레드 그룹 내에서 생성할 최대 스레드 수를 의미 즉 부모 스레드 그룹은 단일 스레드로 동작
        EventLoopGroup workerGroup = new NioEventLoopGroup();           //이건 생성자에 인수가 없는 형태로 이 경우는 스레드 수를 서버 애플리케이션이 동작하는 하드웨어 코어 수를 기준으로 결정
        //스레드 수는 하드웨ㅐ어가 가지고 있는 CPU 코어 수의 2배를 사용 4코어이고 하이퍼 쓰레딩을 지원하면 16개 스레드 생성

        final EchoServerHandler serverHandler = new EchoServerHandler();    //
        try {
            ServerBootstrap b = new ServerBootstrap();      //ServerBootStrap 생성
                    //Builder 패턴
                    b.group(bossGroup, workerGroup) //위에서 생성한 NioEventLoopGroup 객체를 인수로 전달
                    .channel(NioServerSocketChannel.class)  //서버 소켓(부모 스레드)이 사용할 네트워크 입출력 모드를 설정
                                                            //여기서는 NioServerSocketChannel 클래스를 설정했으므로 NIO 모드로 동작함
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO)) //ServerBootStrap 의 handler 메서드로 LoggingHandler 를 설정하는 방법임
                                                                //LoggingHandler 는 네티에서 기본적으로 제공하는 코덱임.
                                                                //이건 채널에서 발생하는 모든 이벤트를 로그로 출력해줌
                    .childHandler(new ChannelInitializer<SocketChannel>() { //자식 채널의 초기화 방법을 설정 여기서는 익명 클래스로 채널 초기화 방법 지정
                        @Override       //ChannelInitializer 는 클라이언트로부터 연결된 채널이 초기화될 때의 기본 동작이 지정된 추상 클래스임
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();  //채널 파이트라인 객체를 생성

                            //p.addLast(new LoggingHandler(LogLevel.INFO));
                            p.addLast(serverHandler);   //채널 파이프라인에 EchoServerHandler 클래스를 등록
                            // EchoServerHandler 클래스는 이후에 클라이언트 연결이 생성되었을 때 데이터 처리를 담당
                        }
                    });

            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
