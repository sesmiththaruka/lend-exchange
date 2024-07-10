package com.tharuka.lendexchange.item;

import com.tharuka.lendexchange.common.PageResponse;
import com.tharuka.lendexchange.exception.OperationNotPermittedException;
import com.tharuka.lendexchange.file.FileStorageService;
import com.tharuka.lendexchange.history.ItemTransactionHistory;
import com.tharuka.lendexchange.history.ItemTransactionHistoryRepository;
import com.tharuka.lendexchange.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final ItemTransactionHistoryRepository transactionHistoryRepository;
    private final FileStorageService fileStorageService;

    public Integer save(ItemRequest request, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Item item = itemMapper.toItem(request);
        item.setOwner(user);
        return itemRepository.save(item).getId();
    }

    public ItemResponse findById(Integer itemId) {
        return itemRepository.findById(itemId)
                .map(itemMapper::toItemResponse)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
    }

    public PageResponse<ItemResponse> findAllItems(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Item> items = itemRepository.findAllDisplayableItems(pageable, user.getId());
        List<ItemResponse> itemsResponse = items.stream()
                .map(itemMapper::toItemResponse)
                .toList();
        return new PageResponse<>(
                itemsResponse,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isFirst(),
                items.isLast()
        );
    }

    public PageResponse<ItemResponse> findAllItemsByOwner(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Item> items = itemRepository.findAll(ItemSpecification.withOwnerId(user.getId()), pageable);
        List<ItemResponse> itemsResponse = items.stream()
                .map(itemMapper::toItemResponse)
                .toList();
        return new PageResponse<>(
                itemsResponse,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isFirst(),
                items.isLast()
        );
    }

    public Integer updateShareableStatus(Integer itemId, Authentication connectedUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others items shareable status");
        }
        item.setShareable(!item.isShareable());
        itemRepository.save(item);
        return itemId;
    }

    public Integer updateArchivedStatus(Integer itemId, Authentication connectedUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot update others items archived status");
        }
        item.setArchived(!item.isArchived());
        itemRepository.save(item);
        return itemId;
    }

    public Integer borrowItem(Integer itemId, Authentication connectedUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        if (item.isArchived() || !item.isShareable()) {
            throw new OperationNotPermittedException("The requested item cannot be borrowed since it is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow your own item");
        }
        final boolean isAlreadyBorrowedByUser = transactionHistoryRepository.isAlreadyBorrowedByUser(itemId, user.getId());
        if (isAlreadyBorrowedByUser) {
            throw new OperationNotPermittedException("You already borrowed this item and it is still not returned or the return is not approved by the owner");
        }

        final boolean isAlreadyBorrowedByOtherUser = transactionHistoryRepository.isAlreadyBorrowed(itemId);
        if (isAlreadyBorrowedByOtherUser) {
            throw new OperationNotPermittedException("Te requested item is already borrowed");
        }

        ItemTransactionHistory itemTransactionHistory = ItemTransactionHistory.builder()
                .user(user)
                .item(item)
                .returned(false)
                .returnApproved(false)
                .build();
        return transactionHistoryRepository.save(itemTransactionHistory).getId();

    }

    public Integer returnBorrowedItem(Integer itemId, Authentication connectedUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        if (item.isArchived() || !item.isShareable()) {
            throw new OperationNotPermittedException("The requested item is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot borrow or return your own item");
        }

        ItemTransactionHistory itemTransactionHistory = transactionHistoryRepository.findByItemIdAndUserId(itemId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("You did not borrow this item"));

        itemTransactionHistory.setReturned(true);
        return transactionHistoryRepository.save(itemTransactionHistory).getId();
    }

    public Integer approveReturnBorrowedItem(Integer itemId, Authentication connectedUser) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        if (item.isArchived() || !item.isShareable()) {
            throw new OperationNotPermittedException("The requested item is archived or not shareable");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (!Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot approve the return of a item you do not own");
        }

        ItemTransactionHistory itemTransactionHistory = transactionHistoryRepository.findByItemIdAndOwnerId(itemId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("The item is not returned yet. You cannot approve its return"));

        itemTransactionHistory.setReturnApproved(true);
        return transactionHistoryRepository.save(itemTransactionHistory).getId();
    }

    public void uploadItemCoverPicture(MultipartFile file, Authentication connectedUser, Integer itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + itemId));
        User user = ((User) connectedUser.getPrincipal());
        var profilePicture = fileStorageService.saveFile(file, itemId, user.getId());
        item.setItemImg(profilePicture);
        itemRepository.save(item);
    }

    public PageResponse<BorrowedItemResponse> findAllBorrowedItems(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<ItemTransactionHistory> allBorrowedItems = transactionHistoryRepository.findAllBorrowedItems(pageable, user.getId());
        List<BorrowedItemResponse> itemsResponse = allBorrowedItems.stream()
                .map(itemMapper::toBorrowedItemResponse)
                .toList();
        return new PageResponse<>(
                itemsResponse,
                allBorrowedItems.getNumber(),
                allBorrowedItems.getSize(),
                allBorrowedItems.getTotalElements(),
                allBorrowedItems.getTotalPages(),
                allBorrowedItems.isFirst(),
                allBorrowedItems.isLast()
        );
    }

    public PageResponse<BorrowedItemResponse> findAllReturnedItems(int page, int size, Authentication connectedUser) {
        User user = ((User) connectedUser.getPrincipal());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<ItemTransactionHistory> allBorrowedItems = transactionHistoryRepository.findAllReturnedItems(pageable, user.getId());
        List<BorrowedItemResponse> itemsResponse = allBorrowedItems.stream()
                .map(itemMapper::toBorrowedItemResponse)
                .toList();
        return new PageResponse<>(
                itemsResponse,
                allBorrowedItems.getNumber(),
                allBorrowedItems.getSize(),
                allBorrowedItems.getTotalElements(),
                allBorrowedItems.getTotalPages(),
                allBorrowedItems.isFirst(),
                allBorrowedItems.isLast()
        );
    }
}
