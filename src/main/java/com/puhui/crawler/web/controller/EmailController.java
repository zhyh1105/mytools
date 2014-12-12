package com.puhui.crawler.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.puhui.crawler.mail.MailReceiverManager;

@Controller
public class EmailController {
    @RequestMapping("/login")
    @ResponseBody
    public boolean String(@RequestParam(required = true) String username, @RequestParam(required = true) String password) {
        MailReceiverManager.receiveEmails(username, password);
        return true;
    }

}
