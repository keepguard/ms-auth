package com.keepguard.ms_auth.adapters.in.rest.session.mapper;

import com.keepguard.ms_auth.adapters.in.rest.auth.dto.AuthLoginResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.auth.mapper.AuthAdapterMapper;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.DeviceBlacklistRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.SendDeviceChallengeRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.request.VerifyDeviceChallengeRequestDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceBlacklistResponseDTO;
import com.keepguard.ms_auth.adapters.in.rest.session.dto.response.DeviceSessionResponseDTO;
import com.keepguard.ms_auth.application.dto.auth.AuthLoginViewDTO;
import com.keepguard.ms_auth.application.dto.session.DeviceSessionViewDTO;
import com.keepguard.ms_auth.application.dto.session.SendDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.application.dto.session.TenantDeviceBlacklistViewDTO;
import com.keepguard.ms_auth.application.dto.session.VerifyDeviceChallengeCommandDTO;
import com.keepguard.ms_auth.domain.entity.session.DeviceBlacklistEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeviceSessionAdapterMapper {

    private final AuthAdapterMapper authAdapterMapper;

    public SendDeviceChallengeCommandDTO toSendChallengeCommand(SendDeviceChallengeRequestDTO request, UUID companyId) {
        return SendDeviceChallengeCommandDTO.builder()
                .challengeSessionId(request.getChallengeSessionId())
                .channel(request.getChannel())
                .companyId(companyId != null ? companyId.toString() : null)
                .build();
    }

    public VerifyDeviceChallengeCommandDTO toVerifyChallengeCommand(VerifyDeviceChallengeRequestDTO request, UUID companyId) {
        return VerifyDeviceChallengeCommandDTO.builder()
                .challengeSessionId(request.getChallengeSessionId())
                .code(request.getCode())
                .trustDevice(request.getTrustDevice())
                .companyId(companyId != null ? companyId.toString() : null)
                .build();
    }

    public AuthLoginResponseDTO toLoginResponse(AuthLoginViewDTO view) {
        return authAdapterMapper.toLoginResponseDTO(view);
    }

    public DeviceSessionResponseDTO toSessionResponse(DeviceSessionViewDTO view) {
        if (view == null) {
            return null;
        }
        return DeviceSessionResponseDTO.builder()
                .sessionId(view.sessionId())
                .deviceId(view.deviceId())
                .deviceName(view.deviceName())
                .deviceType(view.deviceType())
                .ipAddress(view.ipAddress())
                .location(view.location())
                .isCurrent(view.isCurrent())
                .isTrusted(view.isTrusted())
                .lastActiveAt(view.lastActiveAt())
                .createdAt(view.createdAt())
                .codeUser(view.codeUser())
                .writable(view.writable())
                .build();
    }

    public List<DeviceSessionResponseDTO> toSessionResponseList(List<DeviceSessionViewDTO> views) {
        return views == null ? List.of() : views.stream().map(this::toSessionResponse).toList();
    }

    public Page<DeviceSessionResponseDTO> toSessionResponsePage(Page<DeviceSessionViewDTO> page) {
        return page.map(this::toSessionResponse);
    }

    public DeviceBlacklistResponseDTO toBlacklistResponse(DeviceBlacklistEntry entry) {
        if (entry == null) {
            return null;
        }
        return DeviceBlacklistResponseDTO.builder()
                .id(entry.getId())
                .companyId(entry.getCompanyId())
                .codeUser(entry.getCodeUser())
                .deviceId(entry.getDeviceId())
                .deviceName(entry.getDeviceName())
                .ipAddress(entry.getIpAddress())
                .userAgent(entry.getUserAgent())
                .reason(entry.getReason())
                .blockedBy(entry.getBlockedBy())
                .blockedAt(entry.getBlockedAt())
                .expiresAt(entry.getExpiresAt())
                .build();
    }

    public List<DeviceBlacklistResponseDTO> toBlacklistResponseList(List<DeviceBlacklistEntry> entries) {
        return entries == null ? List.of() : entries.stream().map(this::toBlacklistResponse).toList();
    }

    public DeviceBlacklistResponseDTO toBlacklistResponse(TenantDeviceBlacklistViewDTO view) {
        if (view == null) {
            return null;
        }
        return DeviceBlacklistResponseDTO.builder()
                .id(view.id())
                .companyId(view.companyId())
                .codeUser(view.codeUser())
                .deviceId(view.deviceId())
                .deviceName(view.deviceName())
                .ipAddress(view.ipAddress())
                .userAgent(view.userAgent())
                .reason(view.reason())
                .blockedBy(view.blockedBy())
                .blockedAt(view.blockedAt())
                .expiresAt(view.expiresAt())
                .writable(view.writable())
                .build();
    }

    public List<DeviceBlacklistResponseDTO> toTenantBlacklistResponseList(List<TenantDeviceBlacklistViewDTO> views) {
        return views == null ? List.of() : views.stream().map(this::toBlacklistResponse).toList();
    }

    public Page<DeviceBlacklistResponseDTO> toTenantBlacklistResponsePage(Page<TenantDeviceBlacklistViewDTO> page) {
        return page.map(this::toBlacklistResponse);
    }

    public DeviceBlacklistRequestDTO emptyIfNull(DeviceBlacklistRequestDTO request) {
        return request != null ? request : new DeviceBlacklistRequestDTO();
    }
}
