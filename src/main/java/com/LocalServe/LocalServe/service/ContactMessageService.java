package com.LocalServe.LocalServe.service;

import com.LocalServe.LocalServe.entity.ContactMessage;
import com.LocalServe.LocalServe.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public ContactMessage saveMessage(String name, String email, String message) {
        ContactMessage contactMessage = ContactMessage.builder()
                .name(name)
                .email(email)
                .message(message)
                .contactDate(LocalDate.now())
                .build();
        return contactMessageRepository.save(contactMessage);
    }

    public List<ContactMessage> getAllMessages() {
        return contactMessageRepository.findAllByOrderByIdDesc();
    }
}
