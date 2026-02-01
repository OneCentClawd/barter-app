package com.barter.service;

import com.barter.dto.ItemDto;
import com.barter.entity.Item;
import com.barter.entity.ItemImage;
import com.barter.entity.ItemWish;
import com.barter.entity.User;
import com.barter.repository.ItemRepository;
import com.barter.repository.ItemWishRepository;
import com.barter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final ItemWishRepository itemWishRepository;
    private final UserRepository userRepository;
    private final SystemConfigService systemConfigService;

    @Value("${upload.path}")
    private String uploadPath;

    @Transactional
    public ItemDto.ItemResponse createItem(ItemDto.CreateRequest request, User owner, List<MultipartFile> images) {
        // 重新从数据库加载 user 以避免 LazyInitializationException
        User user = userRepository.findById(owner.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Item item = new Item();
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setCondition(request.getCondition());
        item.setWantedItems(request.getWantedItems());
        item.setOwner(user);
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
        return toItemResponse(item, null);
    }

    @Transactional
    public ItemDto.ItemResponse updateItem(Long id, ItemDto.UpdateRequest request, User user) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));

        if (!item.getOwner().getId().equals(user.getId())) {
            throw new RuntimeException("无权修改此物品");
        }
        
        // 只有可用状态的物品才能修改
        if (item.getStatus() != Item.ItemStatus.AVAILABLE) {
            throw new RuntimeException("只有可用状态的物品才能修改");
        }

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getCondition() != null) item.setCondition(request.getCondition());
        if (request.getWantedItems() != null) item.setWantedItems(request.getWantedItems());
        if (request.getStatus() != null) item.setStatus(request.getStatus());
        item.setUpdatedAt(LocalDateTime.now());

        item = itemRepository.save(item);
        return toItemResponse(item, user);
    }

    public ItemDto.ItemResponse getItem(Long id, User currentUser) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("物品不存在"));

        // 检查可见性权限
        boolean currentIsAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin();
        boolean ownerIsAdmin = item.getOwner().getIsAdmin() != null && item.getOwner().getIsAdmin();
        boolean allowUserViewItems = systemConfigService.isAllowUserViewItems();
        
        // 非管理员且不允许查看用户物品时，只能看管理员的物品或自己的物品
        if (!currentIsAdmin && !allowUserViewItems && !ownerIsAdmin) {
            if (currentUser == null || !item.getOwner().getId().equals(currentUser.getId())) {
                throw new RuntimeException("物品不存在");
            }
        }

        // 增加浏览量
        item.setViewCount(item.getViewCount() + 1);
        itemRepository.save(item);

        return toItemResponse(item, currentUser);
    }

    public Page<ItemDto.ItemListResponse> listItems(Pageable pageable, User currentUser) {
        Page<Item> items = itemRepository.findByStatus(Item.ItemStatus.AVAILABLE, pageable);
        return filterItemsForUser(items, currentUser);
    }

    public Page<ItemDto.ItemListResponse> searchItems(String keyword, Pageable pageable, User currentUser) {
        Page<Item> items = itemRepository.searchByKeyword(keyword, Item.ItemStatus.AVAILABLE, pageable);
        return filterItemsForUser(items, currentUser);
    }

    private Page<ItemDto.ItemListResponse> filterItemsForUser(Page<Item> items, User currentUser) {
        boolean currentIsAdmin = currentUser != null && currentUser.getIsAdmin() != null && currentUser.getIsAdmin();
        boolean allowUserViewItems = systemConfigService.isAllowUserViewItems();
        
        // 管理员或开启了用户物品可见，返回全部
        if (currentIsAdmin || allowUserViewItems) {
            return items.map(this::toItemListResponse);
        }
        
        // 否则只返回管理员的物品 + 自己的物品
        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        List<ItemDto.ItemListResponse> filtered = items.getContent().stream()
                .filter(item -> {
                    boolean ownerIsAdmin = item.getOwner().getIsAdmin() != null && item.getOwner().getIsAdmin();
                    boolean isOwn = currentUserId != null && item.getOwner().getId().equals(currentUserId);
                    return ownerIsAdmin || isOwn;
                })
                .map(this::toItemListResponse)
                .collect(Collectors.toList());
        
        return new org.springframework.data.domain.PageImpl<>(filtered, items.getPageable(), filtered.size());
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
        
        // 只有可用状态的物品才能删除
        if (item.getStatus() != Item.ItemStatus.AVAILABLE) {
            throw new RuntimeException("只有可用状态的物品才能删除");
        }

        item.setStatus(Item.ItemStatus.REMOVED);
        itemRepository.save(item);
    }
    
    @Transactional
    public ItemDto.WishResponse toggleWish(Long itemId, User user) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("物品不存在"));
        
        ItemDto.WishResponse response = new ItemDto.WishResponse();
        response.setItemId(itemId);
        
        // 检查是否已收藏
        if (itemWishRepository.existsByUserAndItem(user, item)) {
            // 取消收藏
            itemWishRepository.deleteByUserAndItem(user, item);
            response.setIsWished(false);
        } else {
            // 添加收藏
            ItemWish wish = new ItemWish();
            wish.setUser(user);
            wish.setItem(item);
            itemWishRepository.save(wish);
            response.setIsWished(true);
        }
        
        response.setWishCount(itemWishRepository.countByItem(item));
        return response;
    }
    
    public Page<ItemDto.ItemListResponse> getMyWishes(User user, Pageable pageable) {
        return itemWishRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(wish -> toItemListResponse(wish.getItem()));
    }

    private String saveImage(MultipartFile file) {
        try {
            String filename = UUID.randomUUID().toString() + ".jpg";
            Path path = Paths.get(uploadPath, filename);
            Files.createDirectories(path.getParent());
            
            // 读取图片并压缩
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            if (originalImage == null) {
                // 如果无法读取为图片，直接保存原文件
                Files.write(path, file.getBytes());
                return "/uploads/" + filename;
            }
            
            // 限制最大尺寸为1920px
            int maxSize = 1920;
            int width = originalImage.getWidth();
            int height = originalImage.getHeight();
            
            if (width > maxSize || height > maxSize) {
                double scale = Math.min((double) maxSize / width, (double) maxSize / height);
                int newWidth = (int) (width * scale);
                int newHeight = (int) (height * scale);
                
                BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
                g.dispose();
                originalImage = resizedImage;
            }
            
            // 压缩质量为85%
            ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.85f);
            
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(path.toFile())) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(originalImage, null, null), param);
            }
            writer.dispose();
            
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

    private ItemDto.ItemResponse toItemResponse(Item item, User currentUser) {
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
        response.setWishCount(itemWishRepository.countByItem(item));
        response.setIsWished(currentUser != null && itemWishRepository.existsByUserAndItem(currentUser, item));
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
