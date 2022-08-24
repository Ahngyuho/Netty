package com.netty_projext.controller;


import com.netty_projext.server.BlockingServer;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;

@Controller
public class BlockingController {

    @PostConstruct
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new BlockingServer(8888).run();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @PreDestroy
    private void destroy(){
        System.out.println("end");
    }
}
