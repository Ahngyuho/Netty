package com.netty_projext.controller;

import com.netty_projext.server.NettySocketServer;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
public class NettyController {

    @PostConstruct
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new NettySocketServer(5010).run();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @PreDestroy
    private void destroy(){

    }
}
