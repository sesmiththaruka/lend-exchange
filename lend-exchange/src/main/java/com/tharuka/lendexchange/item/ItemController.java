package com.tharuka.lendexchange.item;

import com.tharuka.lendexchange.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("items")
@RequiredArgsConstructor
@Tag(name = "Item")
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ResponseEntity<Integer> saveItem(
            @Valid @RequestBody ItemRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.save(request, connectedUser));
    }

    @GetMapping("/{item-id}")
    public ResponseEntity<ItemResponse> findItemById(
            @PathVariable("item-id") Integer itemId
    ) {
        return ResponseEntity.ok(service.findById(itemId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<ItemResponse>> findAllItems(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllItems(page, size, connectedUser));
    }

    @GetMapping("/owner")
    public ResponseEntity<PageResponse<ItemResponse>> findAllItemsByOwner(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllItemsByOwner(page, size, connectedUser));
    }

    @GetMapping("/borrowed")
    public ResponseEntity<PageResponse<BorrowedItemResponse>> findAllBorrowedItems(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllBorrowedItems(page, size, connectedUser));
    }

    @GetMapping("/returned")
    public ResponseEntity<PageResponse<BorrowedItemResponse>> findAllReturnedItems(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.findAllReturnedItems(page, size, connectedUser));
    }

    @PatchMapping("/shareable/{item-id}")
    public ResponseEntity<Integer> updateShareableStatus(
            @PathVariable("item-id") Integer itemId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.updateShareableStatus(itemId, connectedUser));
    }

    @PatchMapping("/archived/{item-id}")
    public ResponseEntity<Integer> updateArchivedStatus(
            @PathVariable("item-id") Integer itemId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.updateArchivedStatus(itemId, connectedUser));
    }

    @PostMapping("borrow/{item-id}")
    public ResponseEntity<Integer> borrowItem(
            @PathVariable("item-id") Integer itemId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.borrowItem(itemId, connectedUser));
    }

    @PatchMapping("borrow/return/{item-id}")
    public ResponseEntity<Integer> returnBorrowItem(
            @PathVariable("item-id") Integer itemId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.returnBorrowedItem(itemId, connectedUser));
    }

    @PatchMapping("borrow/return/approve/{item-id}")
    public ResponseEntity<Integer> approveReturnBorrowItem(
            @PathVariable("item-id") Integer itemId,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(service.approveReturnBorrowedItem(itemId, connectedUser));
    }

    @PostMapping(value = "/cover/{item-id}", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadItemCoverPicture(
            @PathVariable("item-id") Integer itemId,
            @Parameter()
            @RequestPart("file") MultipartFile file,
            Authentication connectedUser
    ) {
        service.uploadItemCoverPicture(file, connectedUser, itemId);
        return ResponseEntity.accepted().build();
    }
}
