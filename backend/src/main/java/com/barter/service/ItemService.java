package com.barter.service;

import com.barter.dto.ItemDto;
import com.barter.entity.Item;
import com.barter.entity.ItemImage;
import com.barter.entity.User;
import com.barter.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @Transactional
    public ItemDto.ItemResponse createItem(ItemDto.CreateRequest request, User owner, List<MultipartFile> images) {
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setCondition(request.getCondition());
        item.setWantedItems(request.getWantedItems());
        item.setOwner(owner);
        item.setImages(new ArrayList<>());

        // 处理图片上传
        if (images != null && !images.isEmpty()) {
            int order = 0;
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    String imageUrl = saveImage(file);
                    ItemImage itemImage = new ItemImage();
                    itemImage.setImageUrl(imageUrl);
                    itemImage.setSortOrder(order++);
                    itemImage.setItem(item);
                    item.getImages().add(itemImage);
                }
            }
        }

        item = itemRepository.save(item);
        return toItemResponse(item);
    }

    @Transactional
    public ItemDto.ItemResponse updateItem(Long id, ItemDto.UpdateRequest request, User user) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此物品");
        }

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getCondition() != null) item.setCondition(request.getCondition());
        if (request.getWantedItems() != null) item.setWantedItems(request.getWantedItems());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        item.setUpdatedAt(LocalDateTime.now());

        item = itemRepository.save(item);
        return toItemResponse(item);
    }

    public ItemDto.ItemResponse getItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));

        // 增加浏览量
        item.setViewCount(item.getViewCount() + 1);
        itemRepository.save(item);

        return toItemResponse(item);
    }

    public Page<ItemDto.ItemListResponse> listItems(Pageable pageable) {
        return itemRepository.findByStatus(Item.ItemStatus.AVAILABLE, pageable)
                .map(this::toItemListResponse);
    }

    public Page<ItemDto.ItemListResponse> searchItems(String keyword, Pageable pageable) {
        return itemRepository.searchByKeyword(keyword, Item.ItemStatus.AVAILABLE, pageable)
                .map(this::toItemListResponse);
    }

    public Page<ItemDto.ItemListResponse> getMyItems(User user, Pageable pageable) {
        return itemRepository.findByOwner(user, pageable)
                .map(this::toItemListResponse);
    }

    public List<ItemDto.ItemListResponse> getMyAvailableItems(User user) {
        return itemRepository.findByOwnerAndStatus(user, Item.ItemStatus.AVAILABLE)
                .stream()
                .map(this::toItemListResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteItem(Long id, User user) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("无权删除此物品");
        }

        item.setStatus(Item.ItemStatus.REMOVED);
        itemRepository.save(item);
    }

    private String saveImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID().toString() + getExtension(file.getOriginalFilename());
            Path path = Paths.get(uploadPath, filename);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("图片上传失败", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null) return ".jpg";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : ".jpg";
    }

    private ItemDto.ItemResponse toItemResponse(Item item) {
        ItemDto.ItemResponse response = new ItemDto.ItemResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setDescription(item.getDescription());
        response.setCategory(item.getCategory());
        response.setCondition(item.getCondition());
        response.setStatus(item.getStatus());
        response.setWantedItems(item.getWantedItems());
        response.setOwner(toUserBrief(item.getOwner()));
        response.setImages(item.getImages() != null ?
                item.getImages().stream().map(ItemImage::getImageUrl).collect(Collectors.toList()) :
                new ArrayList<>());
        response.setViewCount(item.getViewCount());
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    public ItemDto.ItemListResponse toItemListResponse(Item item) {
        ItemDto.ItemListResponse response = new ItemDto.ItemListResponse();
        response.setId(item.getId());
        response.setTitle(item.getTitle());
        response.setCategory(item.getCategory());
        response.setCondition(item.getCondition());
        response.setStatus(item.getStatus());
        response.setCoverImage(item.getImages() != null && !item.getImages().isEmpty() ?
                item.getImages().get(0).getImageUrl() : null);
        response.setOwner(toUserBrief(item.getOwner()));
        response.setCreatedAt(item.getCreatedAt());
        return response;
    }

    public ItemDto.UserBrief toUserBrief(User user) {
        ItemDto.UserBrief brief = new ItemDto.UserBrief();
        brief.setId(user.getId());
        brief.setUsername(user.getUsername());
        brief.setNickname(user.getNickname());
        brief.setAvatar(user.getAvatar());
        brief.setRating(user.getRating());
        return brief;
    }
}
