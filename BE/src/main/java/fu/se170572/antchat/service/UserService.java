package fu.se170572.antchat.service;

import fu.se170572.antchat.dto.response.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserProfile(Long userId);
    List<UserDto> searchUsers(String keyword);
}
