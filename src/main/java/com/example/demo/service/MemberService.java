package com.example.demo.service;

import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%";
    private static final SecureRandom rnd = new SecureRandom();

    public Member register(Member m) {
        // generate random password
        String pwd = generatePassword(8);
        m.setPassword(pwd);
        return memberRepository.save(m);
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

    public Member update(Member m) {
        return memberRepository.save(m);
    }

    private String generatePassword(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(PASSWORD_CHARS.charAt(rnd.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
