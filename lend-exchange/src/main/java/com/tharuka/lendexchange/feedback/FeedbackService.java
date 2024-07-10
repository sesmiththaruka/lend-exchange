package com.tharuka.lendexchange.feedback;


import com.tharuka.lendexchange.common.PageResponse;
import com.tharuka.lendexchange.exception.OperationNotPermittedException;
import com.tharuka.lendexchange.item.Item;
import com.tharuka.lendexchange.item.ItemRepository;
import com.tharuka.lendexchange.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedBackRepository feedBackRepository;
    private final ItemRepository itemRepository;
    private final FeedbackMapper feedbackMapper;

    public Integer save(FeedbackRequest request, Authentication connectedUser) {
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(() -> new EntityNotFoundException("No item found with ID:: " + request.itemId()));
        if (item.isArchived() || !item.isShareable()) {
            throw new OperationNotPermittedException("You cannot give a feedback for and archived or not shareable item");
        }
        User user = ((User) connectedUser.getPrincipal());
        if (Objects.equals(item.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("You cannot give feedback to your own item");
        }
        Feedback feedback = feedbackMapper.toFeedback(request);
        return feedBackRepository.save(feedback).getId();
    }

    @Transactional
    public PageResponse<FeedbackResponse> findAllFeedbacksByItem(Integer itemId, int page, int size, Authentication connectedUser) {
        Pageable pageable = PageRequest.of(page, size);
        User user = ((User) connectedUser.getPrincipal());
        Page<Feedback> feedbacks = feedBackRepository.findAllByItemId(itemId, pageable);
        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(f -> feedbackMapper.toFeedbackResponse(f, user.getId()))
                .toList();
        return new PageResponse<>(
                feedbackResponses,
                feedbacks.getNumber(),
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages(),
                feedbacks.isFirst(),
                feedbacks.isLast()
        );

    }
}