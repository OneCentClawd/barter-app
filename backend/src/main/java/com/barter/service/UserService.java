package com.barter.service;

import com.barter.dto.UserDto;
import com.barter.entity.Item;
import com.barter.entity.TradeRequest;
import com.barter.entity.User;
import com.barter.repository.ItemRepository;
import com.barter.repository.TradeRequestRepository;
import com.barter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final TradeRequestRepository tradeRequestRepository;

    @Value("${upload.path}")
    private String uploadPath;

    public UserDto.ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toProfileResponse(user);
    }

    public UserDto.ProfileResponse getMyProfile(User user) {
        return toProfileResponse(user);
    }

    @Transactional
    public UserDto.ProfileResponse updateProfile(UserDto.UpdateProfileRequest request, User user) {
        if (request.getNickname() != null) user.setNickname(request.getNickname());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBio() != null) user.setBio(request.getBio());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    @Transactional
    public UserDto.ProfileResponse updateAvatar(MultipartFile file, User user) {
        if (file.isEmpty()) {
            throw new RuntimeException("请选择图片");
        }

        String avatarUrl = saveAvatar(file);
        user.setAvatar(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user);
        return toProfileResponse(user);
    }

    private String saveAvatar(MultipartFile file) {
        try {
            String filename = "avatar_" + UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            Path path = Paths.get(uploadPath, "avatars", filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("头像上传失败", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : ".jpg";
    }

    private UserDto.ProfileResponse toProfileResponse(User user) {
        UserDto.ProfileResponse response = new UserDto.ProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setPhone(user.getPhone());
        response.setBio(user.getBio());
        response.setRating(user.getRating());
        response.setRatingCount(user.getRatingCount());
        response.setCreatedAt(user.getCreatedAt());

        // 统计
        long itemCount = itemRepository.findByOwner(user, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        response.setItemCount((int) itemCount);

        long tradeCount = tradeRequestRepository.findByRequester(user, org.springframework.data.domain.Pageable.unpaged())
                .stream()
                .filter(tr -> tr.getStatus() == TradeRequest.TradeStatus.COMPLETED)
                .count();
        response.setTradeCount((int) tradeCount);

        return response;
    }
}
