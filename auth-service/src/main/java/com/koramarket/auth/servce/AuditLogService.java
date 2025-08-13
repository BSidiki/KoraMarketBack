package com.koramarket.auth.servce;

import com.koramarket.auth.model.AuditLog;
import com.koramarket.auth.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public List<AuditLog> findAll() {
        return auditLogRepository.findAll();
    }

    public Optional<AuditLog> findById(Long id) {
        return auditLogRepository.findById(id);
    }

    public AuditLog save(AuditLog log) {
        return auditLogRepository.save(log);
    }

    public void delete(Long id) {
        auditLogRepository.deleteById(id);
    }
}
