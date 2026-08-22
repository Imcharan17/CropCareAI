package com.cropportal.repository;

import com.cropportal.entity.Ticket;
import com.cropportal.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByFarmerUserEmail(String email, Pageable pageable);
    Page<Ticket> findByDoctorUserEmail(String email, Pageable pageable);
    long countByStatus(TicketStatus status);
}
