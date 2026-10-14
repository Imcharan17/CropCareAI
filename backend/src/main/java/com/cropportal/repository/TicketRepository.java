package com.cropportal.repository;

import com.cropportal.entity.Ticket;
import com.cropportal.entity.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Page<Ticket> findByFarmerUserEmail(String email, Pageable pageable);
    Page<Ticket> findByDoctorUserEmail(String email, Pageable pageable);

    @Query("select t from Ticket t left join t.doctor d left join d.user doctorUser "
            + "where doctorUser.email = :email or (d is null and t.status in :statuses)")
    Page<Ticket> findDoctorQueue(@Param("email") String email,
                                 @Param("statuses") Collection<TicketStatus> statuses,
                                 Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(TicketStatus status);
}
