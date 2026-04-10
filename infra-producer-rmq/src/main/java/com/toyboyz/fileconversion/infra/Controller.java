package com.toyboyz.fileconversion.infra;

import com.toyboyz.fileconversion.infra.rabbit.sender.Sender;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class Controller {

    private final Sender sender;

    public Controller(Sender sender) {
        this.sender=sender;
    }

    @PostMapping("/send")
    public String sendMessage() {
        sender.testSend();
        return ("good");
    }
}
