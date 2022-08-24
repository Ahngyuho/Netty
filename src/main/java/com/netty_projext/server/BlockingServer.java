package com.netty_projext.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

//블로킹 소켓
public class BlockingServer {
    private int port;

    public BlockingServer(int port) {
        this.port = port;
    }

    public void run() throws IOException {
        ServerSocket server = new ServerSocket(port);   //
        System.out.println("접속 대기중");

        while(true){
            Socket sock = server.accept();  //accept 메서드가 실행되면 클라이언트와 연결된 소켓 생성
                                            //연결요청이 들어오면 여기서 accept() 해주는 것임
            System.out.println("클라이언트 연결됨");

            OutputStream out = sock.getOutputStream();
            InputStream in = sock.getInputStream();

            while(true){
                try{
                    //데이터를 읽기 위한 read() 함수에서 다시 멈추게 됨
                    //연결된 클라이언트에서 데이터의 입력을 받는 부분이 없기 때문에 여기서 메서드의 동작이 멈춤
                    //즉 클라이언트로부터 데이터가 수신되기를 기다리면서 스레드가 블로킹 됨
                    //블로킹 소켓은 호출된 입출력 메서드의 처리가 완료될 때까지 응답을 돌려주지 않고 대기함.
                    int request = in.read();
                    out.write(request);
                }catch (IOException e){
                    break;
                }
            }
        }
    }
}
