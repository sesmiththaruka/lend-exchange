package com.tharuka.lendexchange.item;

import com.tharuka.lendexchange.common.BaseEntity;
import com.tharuka.lendexchange.feedback.Feedback;
import com.tharuka.lendexchange.history.ItemTransactionHistory;
import com.tharuka.lendexchange.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Item extends BaseEntity {
    private String title;
    private String description;
    private boolean archived;
    private boolean shareable;
    private String itemImg;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "item")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "item")
    private List<ItemTransactionHistory> histories;

    @Transient
    public double getRate() {
        if (feedbacks == null || feedbacks.isEmpty()) {
            return 0.0;
        }
        var rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0.0);
        double roundedRate = Math.round(rate * 10.0) / 10.0;

        // Return 4.0 if roundedRate is less than 4.5, otherwise return 4.5
        return roundedRate;
    }
}
