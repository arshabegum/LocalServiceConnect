package com.LocalServe.LocalServe.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 2000)
    private String message;

    private LocalDate contactDate;

    // No-arg constructor
    public ContactMessage() {
    }

    // All-arg constructor
    public ContactMessage(Long id, String name, String email, String message, LocalDate contactDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.contactDate = contactDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getContactDate() {
        return contactDate;
    }

    public void setContactDate(LocalDate contactDate) {
        this.contactDate = contactDate;
    }

    // Manual Builder
    public static ContactMessageBuilder builder() {
        return new ContactMessageBuilder();
    }

    public static class ContactMessageBuilder {
        private Long id;
        private String name;
        private String email;
        private String message;
        private LocalDate contactDate;

        public ContactMessageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ContactMessageBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ContactMessageBuilder email(String email) {
            this.email = email;
            return this;
        }

        public ContactMessageBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ContactMessageBuilder contactDate(LocalDate contactDate) {
            this.contactDate = contactDate;
            return this;
        }

        public ContactMessage build() {
            return new ContactMessage(id, name, email, message, contactDate);
        }
    }
}
