package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.model.Member;
import com.example.demo.service.MemberService;

import jakarta.servlet.http.HttpSession;

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

    @GetMapping("/member/search")
    public String searchMember(@RequestParam(required = false) String name, 
                                @RequestParam(required = false) String mobile, 
                                @RequestParam(required = false) String email, 
                                Model model) {
        if (name != null) {
            model.addAttribute("members", memberService.searchByName(name));
        } else if (mobile != null) {
            model.addAttribute("members", memberService.findByMobile(mobile).stream().toList());
        } else if (email != null) {
            model.addAttribute("members", memberService.findByEmailId(email).stream().toList());
        } else {
            model.addAttribute("error", "Please provide a search criteria");
        }
        return "member-search";
    }

    @PostMapping("/admin/member/save")
    public String saveMember(@RequestParam(required = false) Long id, // Made id optional
                         @RequestParam String name,
                         @RequestParam String mobileNumber,
                         @RequestParam String aadhaarNumber,
                         @RequestParam String emailId,
                         Model model) {
        Member member = new Member();
        if (id != null) {
            member.setId(id);
        }
        member.setName(name);
        member.setMobileNumber(mobileNumber);
        member.setAadhaarNumber(aadhaarNumber);
        member.setEmailId(emailId);
        memberService.register(member);
        model.addAttribute("message", "Member saved successfully");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/member/book")
    public String bookSeat(@RequestParam String shift, @RequestParam int seat, HttpSession session, Model model) {
        Object mid = session.getAttribute("memberId");
        if (mid == null) return "redirect:/member/login";

        Long memberId = (Long) mid;
        memberService.bookSeat(memberId, shift, seat);

        model.addAttribute("hideAdminLogin", true);
        model.addAttribute("message", "Seat " + seat + " booked for " + shift);
        return "member-home";
    }

    @PostMapping("/member/logout")
    public String logOut(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Object mid = session.getAttribute("memberId");
        if (mid == null) return "redirect:/member/login";

        Long memberId = (Long) mid;
        long totalMinutes = memberService.logOut(memberId);

        redirectAttributes.addFlashAttribute("message", "You spent " + totalMinutes + " minutes");
        session.invalidate();
        return "redirect:/member/login";
    }
}