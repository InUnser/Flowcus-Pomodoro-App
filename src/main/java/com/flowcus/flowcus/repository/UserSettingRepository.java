package com.flowcus.flowcus.repository;

import com.flowcus.flowcus.model.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingRepository extends JpaRepository<UserSetting,Long> {
    // Fetch settings via the user id
    Optional<UserSetting> findByUserId(Long userId);
}
