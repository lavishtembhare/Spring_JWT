package com.lavish.Spring_JWT.dao;

import com.lavish.Spring_JWT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {
    User findByUsername(String username);


}
