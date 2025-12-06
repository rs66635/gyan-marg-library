package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.service.MemberService;

@Controller
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/member/reset")
    public String resetForm() {
        return "member-reset";
    }

    @PostMapping("/member/reset")
    public String doReset(@RequestParam String mobile, Model model) {
        return memberService.findByMobile(mobile).map(m -> {
            // generate a new password and save
            String newPwd = java.util.UUID.randomUUID().toString().substring(0,8);
            m.setPassword(newPwd);
            memberService.update(m);
            model.addAttribute("message", "Password reset. New password sent to " + m.getMobileNumber());
            model.addAttribute("newPwd", newPwd);
            return "member-reset";
        }).orElseGet(() -> {
            model.addAttribute("error", "Mobile not found");
            return "member-reset";
        });
    }
}
