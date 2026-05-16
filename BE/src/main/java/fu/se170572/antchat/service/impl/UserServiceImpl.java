package fu.se170572.antchat.service.impl;

import fu.se170572.antchat.dto.response.UserDto;
import fu.se170572.antchat.entity.User;
import fu.se170572.antchat.repository.UserRepository;
import fu.se170572.antchat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    public List<UserDto> searchUsers(String keyword) {
        // Tìm kiếm theo keyword cho cả username và email
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);

        // Map List<User> sang List<UserDto> để không lộ password_hash
        return users.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }
}
