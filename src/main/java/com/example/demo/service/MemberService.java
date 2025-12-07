package com.example.demo.service;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JavaMailSender mailSender;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
    private static final SecureRandom rnd = new SecureRandom();

    public Member register(Member m) {
        // generate random password
        String pwd = generatePassword(8);
        m.setPassword(pwd);
        Member savedMember = memberRepository.save(m);

        // send email with member details
        sendMemberDetailsEmail(savedMember);

        return savedMember;
    }

    private void sendMemberDetailsEmail(Member member) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(member.getEmailId());
            helper.setSubject("Welcome to Gyan Marg Library");
            helper.setText("Dear " + member.getName() + ",\n\n" +
                          "Your registration is successful. Here are your details:\n" +
                          "Member ID: " + member.getId() + "\n" +
                          "Name: " + member.getName() + "\n" +
                          "Mobile: " + member.getMobileNumber() + "\n" +
                          "Aadhaar: " + member.getAadhaarNumber() + "\n" +
                          "Password: " + member.getPassword() + "\n\n" +
                          "Thank you for joining us!\n\n" +
                          "Regards,\nGyan Marg Library Team");

             mailSender.send(message);
        } catch (MessagingException e) {
            // log the error or handle it as needed
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public List<Member> all() {
        return memberRepository.findAll();
    }

    public Optional<Member> findById(Long id) {
        return memberRepository.findById(id);
    }

    public void delete(Long id) {
        memberRepository.deleteById(id);
    }

    public List<Member> searchByName(String name) {
        return memberRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<Member> findByMobile(String mobile) {
        return memberRepository.findByMobileNumber(mobile);
    }

    public Optional<Member> findByEmailId(String emailId) {
        return memberRepository.findByEmailId(emailId);
    }

    public Member update(Member m) {
        return memberRepository.save(m);
    }

    public void bookSeat(Long memberId, String shift, int seat) {
        memberRepository.findById(memberId).ifPresent(member -> {
            member.setShift(shift);
            member.setSeatNumber(seat);
            member.setLoginTime(LocalDateTime.now());
            member.setLogoutTime(null);
            member.setTotalTimeSpent(null);
            memberRepository.save(member);
        });
    }

    public long logOut(Long memberId) {
        return memberRepository.findById(memberId).map(member -> {
            LocalDateTime logoutTime = LocalDateTime.now();
            member.setLogoutTime(logoutTime);

            LocalDateTime loginTime = member.getLoginTime();
            long totalMinutes = java.time.Duration.between(loginTime, logoutTime).toMinutes();
            member.setTotalTimeSpent(totalMinutes);

            memberRepository.save(member);
            return totalMinutes;
        }).orElse(0L);
    }

    private String generatePassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(PASSWORD_CHARS.charAt(rnd.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    public List<Member> getMemberDetails(Long memberId) {
        return memberRepository.findById(memberId)
                .map(List::of)
                .orElse(List.of());
    }
}