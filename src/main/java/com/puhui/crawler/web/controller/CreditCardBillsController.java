package com.puhui.crawler.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.puhui.crawler.Response;
import com.puhui.crawler.mail.MailReceiver;

@Controller
@RequestMapping("/creditCardBills")
public class CreditCardBillsController {
    @RequestMapping("/login")
    @ResponseBody
    public Response String(@RequestParam(required = true) String username, @RequestParam(required = true) String password) {
        return new MailReceiver(username, password).receiveMail();
    }

}
