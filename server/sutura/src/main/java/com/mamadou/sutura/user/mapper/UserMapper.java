package com.mamadou.sutura.user.mapper;

import com.mamadou.sutura.user.dto.CreateUserRequest;
import com.mamadou.sutura.user.dto.UserProfileResponse;
import com.mamadou.sutura.user.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private  final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserMapper(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public  UserProfileResponse mapToUserProfileResponse(User user) {
        return new UserProfileResponse(
                user.getUsername(),
                user.getLastName()
        );
    }
    public  User mapToUser(CreateUserRequest createUserRequest) {
        return User.builder()
                .firstName(createUserRequest.firstName())
                .lastName(createUserRequest.lastname())
                .phoneNumber(createUserRequest.phoneNumber())
                .passwordHash(bCryptPasswordEncoder.encode(createUserRequest.password()))
                .build();
    }
}
