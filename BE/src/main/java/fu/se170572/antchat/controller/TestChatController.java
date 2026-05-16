package fu.se170572.antchat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class TestChatController {
    @MessageMapping("/test.chat")
    @SendTo("/topic/public")
    public String testMessage(String message) {
        return "Server AntChat nhận được: " + message;
    }
}
