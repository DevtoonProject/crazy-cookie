package yjh.devtoon.payment.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessage {

    private static final String RESOURCE_NOT_FOUND = "%s : '%s' 을/를 찾을 수 없습니다.";

    public static <T> String getResourceNotFound(String entity, T identifier) {
        return String.format(RESOURCE_NOT_FOUND, entity, identifier);
    }
}