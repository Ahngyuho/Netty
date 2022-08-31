package com.netty_projext.controller;

import com.netty_projext.server.EchoServer;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
public class EchoController {

    @PostConstruct
    private void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    new EchoServer(1232).run();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void Test1(){
        new Object(){
            private final String a = "test";


        };

        Runnable r = () -> {
            try {
                new EchoServer(1232).run();
            }catch (Exception e){
                e.printStackTrace();
            }
        };

        Thread thread = new Thread(r);

        thread.start();

        String s = (name , i) -> System.out.println(); 
    }

//    private void start(){
//        Runnable oneUseClass = new OneUseClass();
//
//        Thread thread = new Thread(oneUseClass);
//        thread.start();
//    }
//
//    class OneUseClass implements Runnable{
//        public void run(){
//            System.out.println("OneUseClass");
//        }
//    }
}
