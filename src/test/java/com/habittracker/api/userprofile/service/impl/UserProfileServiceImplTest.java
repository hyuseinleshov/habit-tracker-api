package com.habittracker.api.userprofile.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import com.habittracker.api.userprofile.repository.UserProfileRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

  @Mock private UserProfileRepository userProfileRepository;

  @InjectMocks private UserProfileServiceImpl toTest;
}
