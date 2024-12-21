package com.nortal.activedirectoryrestapi.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class OneTimeKeys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alice_public_key")
    String alicePublicKey;
    @Column(name = "bob_public_key")
    String bobPublicKey;
}
