package com.netty_projext.server;

import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class NonBlockingServer {
    private int port;
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    public NonBlockingServer(int port) {
        this.port = port;
    }

    public void run() {
        startEchoServer();
    }

    //
    private void startEchoServer() {
        //try 문은 try 블록이 끝날 때 소괄호 안에서 선언된 자원을 자동으로 해제해줌
        try (Selector selector = Selector.open(); // 자바 NIO 컴포넌트 중의 하나인 Selector는 자신에게 등록된 채널에
                                                    //변경 사항이 발생했는지 검사하고 변경 사항이 발생한 채널에 대한 접근을 가능하게 해줌
             ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) //블로킹 소켓의 ServerSocket에 대응되는 논블로킹 소켓의 서버 소켓
        {

            if ((serverSocketChannel.isOpen()) && (selector.isOpen())) {    //생성한 Selector 와 ServerSocketChannel 객체가 정상적으로 생성되었는지 확인
                serverSocketChannel.configureBlocking(false);   //소켓 채널의 브로킹 모드의 기본값은 true 임. 즉 별도로 논 블로킹 모드로 지정하지 않으면
                                                                //블로킹 모드로 동작함 여기서는 ServerSocketChannel 객체를 논 블로킹 모드로 설정하기 위해 false로 설정
                serverSocketChannel.bind(new InetSocketAddress(port));  //클라이언트의 연결을 대기할 포트를 지정하고 생성된 ServerSocketChannel 객체에 할당
                                                                        //이 작업이 완료되면 ServerSocketChannel 객체가 지정된 포트로부터 클라이언트의 연결을 생성할 수 있음

                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //ServerSocketChannel 객체를 Selector 객체에 등록
                                                                                //Selector 가 감지할 이벤트는 연결 요청에 해당하는 SelectionKey.OP_ACCEPT 임.
                System.out.println("접속 대기중");

                while (true) {
                    selector.select();  //Selector 에 등록된 채널에서 변경 사항이 발생했는지 검사
                                        //Selector 에 아무런 I/O 이벤트도 발생하지 않으면 스레드는 이 부분에서 블로킹 됨
                                        //I/O 이벤트가 발생하지 않았을 때 블로킹을 피하고 싶으면 selectNow 메서드를 사용하면 됨
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator();   //Selector 에 등록된 채널 중에서 I/O 이벤트가 발생한 채널들의 목록 조회

                    while (keys.hasNext()) {
                        SelectionKey key = (SelectionKey) keys.next();
                        keys.remove();  //I/O 이벤트가 발생한 채널에서 동일한 이벤트가 감지되는 것을 방지하기 위하여 조회된 목록에서 제거

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {   //조회된 I/O 이벤트의 종류가 연결 요청인지 확인 만약 연결 요청 이벤트라면 연결처리 메서드로 이동
                            this.acceptOP(key, selector);
                        } else if (key.isReadable()) {  //조회된 I/O 이벤트의 종류가 데이서 수신인지 확인 만약 데이터 수신 이벤트라면 데이터 읽기 처리 메서드로 이동
                            this.readOP(key);
                        } else if (key.isWritable()) {  //조회된 I/O 이벤트의 종류가 데이터 쓰기 가능인지 확인 만약 데이터 쓰기 가능 이벤트라면 데이터 읽기 처리 메서드로 이동
                            this.writeOP(key);
                        }
                    }
                }
            } else {
                System.out.println("서버 소켓을 생성하지 못했습니다.");
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();    //연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel 이므로 이벤트가 발생한 채널을
                                                                                    //ServerSocketChannel 로 캐스팅 함
        SocketChannel socketChannel = serverChannel.accept();   //ServerSocketChannel 을  사용하여 클라이언트의 연결을 수락하고 연결된 소켓 채널을 가져온다.
        socketChannel.configureBlocking(false); //연결된 클라이언트 소켓 채널을 논 블로킹 모드로 설정

        System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

        keepDataTrack.put(socketChannel, new ArrayList<byte[]>());
        socketChannel.register(selector, SelectionKey.OP_READ); //클라이언트 소켓 채널을 Selector 에 등록하여 I/O 이벤트를 감시
    }

    private void readOP(SelectionKey key) {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;

            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                System.err.println("데이터 읽기 에러!");
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("클라이언트 연결 종료 : " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.channel();
                return;
            }

            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, "UTF-8") + " from " + socketChannel.getRemoteAddress());

            doEchoJob(key, data);
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private void writeOP(SelectionKey key) throws IOException {

        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }


    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

}
