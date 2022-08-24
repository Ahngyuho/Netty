package com.netty_projext.Handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

public class NettySocketClientHandler extends ChannelInboundHandlerAdapter {
    private String msg;

    public NettySocketClientHandler(String msg) {
        this.msg = msg;
    }

    @Override
    //channelActive 이벤트는 ChannelInboundHandler 에 정의된 이벤트로써 소켓 채널이 최초 활성화 되었을 때
    //실행됨
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(msg.getBytes());

        //writeAndFlush 메서드는 내부적으로 데이터 기록과 전송의 두 가지 메서드를 호출
        //첫 번째는 채널에 데이터를 기록하는 write 메서드며 두 번째 채널에 기록된 데이터를
        //서버로 전송하는 flush 메서드다.
        ctx.writeAndFlush(messageBuffer);

        System.out.println("send message {" + msg + "}");
    }

    @Override
    //서버로부터 수신된 데이터가 있을 때 호출되는 네티 이벤트 메서드
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //서버로부터 수신된 데이터가 저장된 msg 객체에서 문자열 데이터 추출
        String readMessage = ((ByteBuf)msg).toString(Charset.defaultCharset());


        System.out.println("receive message {" + readMessage + "}");
    }

    @Override
    //수신된 데이터를 모두 읽었을 때 호출되는 이벤트 메서드
    //channelRead 메서드의 수행이 완료되고 자동으로 호출됨
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //수신된 데이터를 모두 읽은 후 서버와 연결된 채널을 닫음
        //이후 데이터 송수신 채널은 닫히게 되고 클라이언트 프로그램 종료
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(cause);
        ctx.close();
    }
}
