package com.prography.backend.service;

import com.prography.backend.dto.SessionDto;
import com.prography.backend.entity.QrCode;
import com.prography.backend.entity.SessionEntity;
import com.prography.backend.error.AppException;
import com.prography.backend.error.ErrorCode;
import com.prography.backend.repository.QrCodeRepository;
import com.prography.backend.repository.SessionRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QrCodeService {

    private final QrCodeRepository qrCodeRepository;
    private final SessionRepository sessionRepository;

    public QrCodeService(QrCodeRepository qrCodeRepository, SessionRepository sessionRepository) {
        this.qrCodeRepository = qrCodeRepository;
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public QrCode createForSession(SessionEntity session) {
        qrCodeRepository.findFirstBySessionAndActiveTrue(session)
            .ifPresent(active -> {
                if (active.getExpiresAt().isAfter(LocalDateTime.now())) {
                    throw new AppException(ErrorCode.QR_ALREADY_ACTIVE);
                }
                active.expireNow();
            });
        QrCode qrCode = new QrCode(session, UUID.randomUUID().toString(), LocalDateTime.now().plusHours(24), true);
        return qrCodeRepository.save(qrCode);
    }

    @Transactional
    public SessionDto.QrCodeResponse create(Long sessionId) {
        SessionEntity session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new AppException(ErrorCode.SESSION_NOT_FOUND));
        QrCode qrCode = createForSession(session);
        return toDto(qrCode);
    }

    @Transactional
    public SessionDto.QrCodeResponse renew(Long qrCodeId) {
        QrCode qrCode = qrCodeRepository.findById(qrCodeId)
            .orElseThrow(() -> new AppException(ErrorCode.QR_NOT_FOUND));
        qrCode.expireNow();
        QrCode created = new QrCode(qrCode.getSession(), UUID.randomUUID().toString(), LocalDateTime.now().plusHours(24), true);
        QrCode saved = qrCodeRepository.save(created);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public QrCode findByHash(String hashValue) {
        return qrCodeRepository.findByHashValue(hashValue).orElseThrow(() -> new AppException(ErrorCode.QR_INVALID));
    }

    @Transactional(readOnly = true)
    public QrCode getActiveBySession(SessionEntity session) {
        return qrCodeRepository.findFirstBySessionAndActiveTrue(session).orElse(null);
    }

    private SessionDto.QrCodeResponse toDto(QrCode qrCode) {
        return new SessionDto.QrCodeResponse(
            qrCode.getId(),
            qrCode.getSession().getId(),
            qrCode.getHashValue(),
            qrCode.getCreatedAt(),
            qrCode.getExpiresAt()
        );
    }
}
