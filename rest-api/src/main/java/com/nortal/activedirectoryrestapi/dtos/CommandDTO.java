package com.nortal.activedirectoryrestapi.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommandDTO {
    private Long id;
    private String command;
    private String commandStatus;
    private String result;
}
