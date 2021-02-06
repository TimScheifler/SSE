package com.thesis.sse.sse;

import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.Emitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class NewsController {

    public List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    //Method for Subscription
    @RequestMapping(value = "/subscribe", consumes = MediaType.ALL_VALUE)
    public SseEmitter subscribe(){
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        try {
            sseEmitter.send(SseEmitter.event().name("INIT"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        sseEmitter.onCompletion(() -> emitters.remove(sseEmitter));
        emitters.add(sseEmitter);
        return sseEmitter;
    }

    @PostMapping(value = "/event")
    public void dispatchEventToClients (@RequestParam String title, @RequestParam String text){

        String eventFormatted = new JSONObject()
                .put("title",title)
                .put("text",text)
                .toString();

        for(SseEmitter emitter : emitters){
            try {
                emitter.send(SseEmitter.event().name("latestNews").data(eventFormatted));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    //Method for dispatching events to all clients

}
