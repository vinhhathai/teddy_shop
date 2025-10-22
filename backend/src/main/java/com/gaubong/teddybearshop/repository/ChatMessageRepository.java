package com.gaubong.teddybearshop.repository;

import com.gaubong.teddybearshop.entity.ChatMessage;
import com.gaubong.teddybearshop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<ChatMessage> findTop10ByUserOrderByCreatedAtDesc(User user);
}
