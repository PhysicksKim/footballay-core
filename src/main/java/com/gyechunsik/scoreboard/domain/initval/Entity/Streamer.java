package com.gyechunsik.scoreboard.domain.initval.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.CreationTimestamp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Slf4j
@Getter
@ToString
@Entity
@Table(name = "streamers")
public class Streamer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String hash;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Streamer(String name) {
        this.name = name;
    }

    protected Streamer() {
    }

    @PrePersist
    public void fillCreatedTimeAndGenerateHash() {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] digest = instance.digest((name + createdAt).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            hash = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Streamer streamer = (Streamer) o;

        if (getId() != null ? !getId().equals(streamer.getId()) : streamer.getId() != null) return false;
        if (getName() != null ? !getName().equals(streamer.getName()) : streamer.getName() != null) return false;
        if (getHash() != null ? !getHash().equals(streamer.getHash()) : streamer.getHash() != null) return false;
        return getCreatedAt() != null ? getCreatedAt().equals(streamer.getCreatedAt()) : streamer.getCreatedAt() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getHash() != null ? getHash().hashCode() : 0);
        result = 31 * result + (getCreatedAt() != null ? getCreatedAt().hashCode() : 0);
        return result;
    }
}
