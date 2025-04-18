package com.hj.kafkaeventrush.repository;

import com.hj.kafkaeventrush.entity.EventJoin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventJoinRepository extends JpaRepository<EventJoin, Long> {
}
