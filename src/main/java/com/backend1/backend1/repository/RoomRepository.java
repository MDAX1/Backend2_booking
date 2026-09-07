package com.backend1.backend1.repository;

import com.backend1.backend1.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByIdNotIn(Collection<Long> ids);
}
