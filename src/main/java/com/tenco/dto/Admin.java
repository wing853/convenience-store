package com.tenco.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "password")
@Builder

public class Admin {

    private int id;
    private String adminId;
    private String password;
    private String name;

}
