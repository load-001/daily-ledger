package com.load.dailyledger.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式错误")
    String email;

    @Size( max = 50, message = "显示名最多50个字符")
    private String displayName;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8 , max = 64, message = "密码长度必须在8到64之间")
    String password;
}
