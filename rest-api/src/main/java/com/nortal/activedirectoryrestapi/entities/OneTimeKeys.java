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

    @Column(name = "alice_x")
    String aliceX;
    @Column(name = "alice_y")
    String aliceY;
    @Column(name = "bob_x")
    String bobX;
    @Column(name = "bob_y")
    String bobY;
}
