package com.netty_projext.controller;

import com.netty_projext.server.NonBlockingServer;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Controller
public class NonBlockingController {

    @PostConstruct
    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new NonBlockingServer(7777).run();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @PreDestroy
    public void destroy(){
        System.out.println("NonBlockingServer End");
    }
}
