package yjh.devtoon.policy.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yjh.devtoon.common.exception.DevtoonException;
import yjh.devtoon.common.exception.ErrorCode;
import yjh.devtoon.common.utils.ResourceType;
import yjh.devtoon.policy.common.Policy;
import yjh.devtoon.policy.constant.ErrorMessage;
import yjh.devtoon.policy.domain.BadWordsPolicyEntity;
import yjh.devtoon.policy.domain.CookiePolicyEntity;
import yjh.devtoon.policy.domain.PolicyFactory;
import yjh.devtoon.policy.domain.PolicyType;
import yjh.devtoon.policy.dto.request.PolicyCreateRequest;
import yjh.devtoon.policy.infrastructure.BadWordsPolicyRepository;
import yjh.devtoon.policy.infrastructure.CookiePolicyRepository;
import yjh.devtoon.policy.infrastructure.PolicyRepositoryRegistry;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class PolicyService {

    private final PolicyRepositoryRegistry policyRepositoryRegistry;
    private final CookiePolicyRepository cookiePolicyRepository;
    private final BadWordsPolicyRepository badWordsPolicyRepository;

    /**
     * 정책 등록
     */
    @Transactional
    public void register(PolicyCreateRequest request) {
        // 1. 정책 확인 -> PolicyType 객체 반환
        PolicyType policyType = PolicyType.create(request.getPolicyName());

        // 2. 정책 타입에 맞게 정책 객체(Policy)를 생성 및 반환
        Policy policy = PolicyFactory.valueOf(policyType.getPolicyName()).createPolicy(request);

        // 3. 각 정책 리포지토리에 저장
        // @SuppressWarnings("unchecked") : 제네릭 타입 캐스팅 시 발생하는 경고 억제
        @SuppressWarnings("unchecked")
        JpaRepository<Policy, Long> repository = (JpaRepository<Policy, Long>)
                Optional.ofNullable(policyRepositoryRegistry.getRepository(policy.getClass()))
                        .orElseThrow(() -> new DevtoonException(
                                ErrorCode.NOT_FOUND,
                                ErrorMessage.getResourceNotFound(
                                        ResourceType.POLICY,
                                        policyType
                                )
                        ));
        repository.save(policy);
    }

    /**
     * 쿠키 정책 조회
     */
    @Transactional(readOnly = true)
    public CookiePolicyEntity retrieveCookiePolicy() {
        return cookiePolicyRepository.findActiveCookiePolicy()
                .orElseThrow(() -> new DevtoonException(
                        ErrorCode.NOT_FOUND,
                        ErrorMessage.getResourceNotFound(
                                ResourceType.POLICY,
                                cookiePolicyRepository.findActiveCookiePolicy()
                        )
                ));
    }

    /**
     * 비속어 정책 조회
     */
    @Transactional(readOnly = true)
    public BadWordsPolicyEntity retrieveBadWordsPolicyPolicy() {
        return badWordsPolicyRepository.findActiveBadWordsPolicy()
                .orElseThrow(() -> new DevtoonException(
                        ErrorCode.NOT_FOUND,
                        ErrorMessage.getResourceNotFound(
                                ResourceType.POLICY,
                                badWordsPolicyRepository.findActiveBadWordsPolicy()
                        )
                ));
    }

}
