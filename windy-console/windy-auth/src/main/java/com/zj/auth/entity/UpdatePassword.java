package com.zj.auth.entity;

import lombok.Data;

@Data
public class UpdatePassword {

    private String oldPassword;

    private String newPassword;
}
