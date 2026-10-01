package com.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final Sender sender;

    public MessageController(Sender sender) {
        this.sender = sender;
    }

    @PostMapping
    public String sendMessage(@RequestBody MessageRequest request) {
        sender.sendMessage(request.getMessage());
        return "Mensaje enviado: " + request.getMessage();
    }

    @GetMapping("/send")
    public String sendMessageGet(@RequestParam("message") String message) {
        sender.sendMessage(message);
        return "Mensaje enviado: " + message;
    }

    public static class MessageRequest {

        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}