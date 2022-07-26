package com.jaoow.discordbridge.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@RequiredArgsConstructor
public class UserData {

    private final AtomicBoolean boosterStatus = new AtomicBoolean();
    private final Map<RewardType, Long> lastOpening;

    public long getLastOpenFor(RewardType type) {
        return lastOpening.getOrDefault(type, 0L);
    }

}
