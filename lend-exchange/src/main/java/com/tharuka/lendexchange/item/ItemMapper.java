package com.tharuka.lendexchange.item;

import com.tharuka.lendexchange.file.FileUtils;
import com.tharuka.lendexchange.history.ItemTransactionHistory;
import org.springframework.stereotype.Service;

@Service
public class ItemMapper {
    public Item toItem(ItemRequest request) {
        return Item.builder()
                .id(request.id())
                .title(request.title())
                .archived(false)
                .shareable(request.shareable())
                .build();
    }

    public ItemResponse toItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .archived(item.isArchived())
                .shareable(item.isShareable())
                .owner(item.getOwner().fullName())
                .cover(FileUtils.readFileFromLocation(item.getItemImg()))
                .build();
    }

    public BorrowedItemResponse toBorrowedItemResponse(ItemTransactionHistory history) {
        return BorrowedItemResponse.builder()
                .id(history.getItem().getId())
                .title(history.getItem().getTitle())
                .rate(history.getItem().getRate())
                .returned(history.isReturned())
                .returnApproved(history.isReturnApproved())
                .build();
    }
}
