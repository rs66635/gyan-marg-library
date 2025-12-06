package com.example.demo.controller;

import com.example.demo.model.Member;
import com.example.demo.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class WebController {

    @Autowired
    private MemberService memberService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("facilities", List.of(
                "Reading Room",
                "Computer Lab",
                "Children Section",
                "Magazine & Newspapers",
                "Reference Section",
                "WiFi"
        ));
        return "home";
    }

    @GetMapping("/admin/login")
    public String adminLogin(Model model) {
        model.addAttribute("hideMemberLogin", true);
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminDoLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        // simple hardcoded admin
        if ("admin".equals(username) && "admin123".equals(password)) {
            session.setAttribute("isAdmin", true);
            return "redirect:/admin/dashboard";
        }
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        model.addAttribute("hideMemberLogin", true);
        model.addAttribute("members", memberService.all());
        return "admin-dashboard";
    }

    @GetMapping("/admin/member/new")
    public String newMemberForm(Model model, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        model.addAttribute("hideMemberLogin", true);
        model.addAttribute("member", new Member());
        return "member-form";
    }

    @PostMapping("/admin/member/save")
    public String saveMember(@ModelAttribute Member member, Model model, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        Member saved = memberService.register(member);
        // simulate WhatsApp send by adding flag / message
        model.addAttribute("hideMemberLogin", true);
        model.addAttribute("saved", saved);
        model.addAttribute("whatsappMessage", "Sent to " + saved.getMobileNumber() + ": Your member ID is " + saved.getId() + " and password is " + saved.getPassword());
        return "member-success";
    }

    @GetMapping("/admin/member/edit/{id}")
    public String editMember(@PathVariable Long id, Model model, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        model.addAttribute("hideMemberLogin", true);
        Member m = memberService.findById(id).orElse(new Member());
        model.addAttribute("member", m);
        return "member-form";
    }

    @GetMapping("/admin/member/delete/{id}")
    public String deleteMember(@PathVariable Long id, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        memberService.delete(id);
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/member/search")
    public String searchMember(@RequestParam(required = false) String q, Model model, HttpSession session) {
        Object isAdmin = session.getAttribute("isAdmin");
        if (isAdmin == null || !(Boolean) isAdmin) return "redirect:/admin/login";
        model.addAttribute("hideMemberLogin", true);
        if (q == null || q.isBlank()) {
            model.addAttribute("members", memberService.all());
        } else {
            // attempt by id
            try {
                Long id = Long.parseLong(q);
                memberService.findById(id).ifPresentOrElse(m -> model.addAttribute("members", List.of(m)), () -> model.addAttribute("members", List.of()));
            } catch (NumberFormatException e) {
                // by name or mobile
                List<Member> byName = memberService.searchByName(q);
                if (!byName.isEmpty()) model.addAttribute("members", byName);
                else memberService.findByMobile(q).ifPresentOrElse(m -> model.addAttribute("members", List.of(m)), () -> model.addAttribute("members", List.of()));
            }
        }
        return "admin-dashboard";
    }

    // Member pages
    @GetMapping("/member/login")
    public String memberLogin(Model model) {
        model.addAttribute("hideAdminLogin", true);
        return "member-login";
    }

    @PostMapping("/member/login")
    public String memberDoLogin(@RequestParam Long memberId, @RequestParam String password, HttpSession session, Model model) {
        model.addAttribute("hideAdminLogin", true);
        return memberService.findById(memberId).map(m -> {
            if (m.getPassword().equals(password)) {
                session.setAttribute("memberId", m.getId());
                return "redirect:/member/home";
            } else {
                model.addAttribute("error", "Invalid credentials");
                return "member-login";
            }
        }).orElseGet(() -> {
            model.addAttribute("error", "Member not found");
            return "member-login";
        });
    }

    @GetMapping("/member/home")
    public String memberHome(HttpSession session, Model model) {
        Object mid = session.getAttribute("memberId");
        if (mid == null) return "redirect:/member/login";
        model.addAttribute("hideAdminLogin", true);
        model.addAttribute("shifts", List.of("Morning", "Afternoon", "Evening"));
        model.addAttribute("seats", 10);
        return "member-home";
    }

    @PostMapping("/member/book")
    public String bookSeat(@RequestParam String shift, @RequestParam int seat, HttpSession session, Model model) {
        Object mid = session.getAttribute("memberId");
        if (mid == null) return "redirect:/member/login";
        // record clock in
        session.setAttribute("shift", shift);
        session.setAttribute("seat", seat);
        session.setAttribute("clockIn", System.currentTimeMillis());
        model.addAttribute("hideAdminLogin", true);
        model.addAttribute("message", "Seat " + seat + " booked for " + shift);
        return "member-home";
    }

    @PostMapping("/member/checkout")
    public String checkout(HttpSession session, Model model) {
        Object mid = session.getAttribute("memberId");
        if (mid == null) return "redirect:/member/login";
        Long in = (Long) session.getAttribute("clockIn");
        long out = System.currentTimeMillis();
        long totalMs = out - (in == null ? out : in);
        long minutes = totalMs / 60000;
        model.addAttribute("hideAdminLogin", true);
        model.addAttribute("message", "You spent " + minutes + " minutes");
        session.removeAttribute("clockIn");
        session.removeAttribute("shift");
        session.removeAttribute("seat");
        return "member-home";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}