package yjh.devtoon.payment.application;

import static java.lang.Math.max;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yjh.devtoon.common.exception.DevtoonException;
import yjh.devtoon.common.exception.ErrorCode;
import yjh.devtoon.common.utils.ResourceType;
import yjh.devtoon.cookie_wallet.application.CookieWalletService;
import yjh.devtoon.cookie_wallet.domain.CookieWalletEntity;
import yjh.devtoon.cookie_wallet.infrastructure.CookieWalletRepository;
import yjh.devtoon.payment.constant.ErrorMessage;
import yjh.devtoon.payment.domain.WebtoonPaymentEntity;
import yjh.devtoon.payment.dto.request.WebtoonPaymentCreateRequest;
import yjh.devtoon.payment.infrastructure.WebtoonPaymentRepository;
import yjh.devtoon.policy.infrastructure.CookiePolicyRepository;
import yjh.devtoon.promotion.application.PromotionService;
import yjh.devtoon.promotion.domain.PromotionAttributeEntity;
import yjh.devtoon.promotion.domain.PromotionEntity;
import yjh.devtoon.promotion.domain.attribute.Attribute;
import yjh.devtoon.promotion.domain.promotion.CookiePromotion;
import yjh.devtoon.promotion.domain.promotion.Promotion;
import yjh.devtoon.promotion.infrastructure.PromotionAttributeRepository;
import yjh.devtoon.webtoon.application.WebtoonService;
import yjh.devtoon.webtoon.domain.WebtoonEntity;
import yjh.devtoon.webtoon.infrastructure.WebtoonRepository;
import yjh.devtoon.member.domain.MemberEntity;
import yjh.devtoon.member.infrastructure.MemberRepository;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class WebtoonPaymentService {

    private final WebtoonRepository webtoonRepository;
    private final MemberRepository memberRepository;
    private final CookiePolicyRepository cookiePolicyRepository;
    private final CookieWalletService cookieWalletService;
    private final CookieWalletRepository cookieWalletRepository;
    private final WebtoonPaymentRepository webtoonPaymentRepository;
    private final PromotionAttributeRepository promotionAttributeRepository;
    private final WebtoonService webtoonService;
    private final PromotionService promotionService;

    /**
     * 웹툰 미리보기 결제
     * : 웹툰 미리보기는 쿠키로 결제한다.
     */
    @Transactional
    public void register(final WebtoonPaymentCreateRequest request) {
        // 1. memberId 조회
        Long memberId = getMemberIdOrThrow(request.getMemberId());

        // 2. webtoonId 조회
        Long webtoonId = getWebtoonIdOrThrow(request.getWebtoonId());
        WebtoonEntity webtoon = webtoonService.retrieve(webtoonId);

        // 3. 웹툰 미리보기 1편당 차감 쿠키 개수 정책 조회
        Integer cookiePerEpisode =
                cookiePolicyRepository.findActiveCookieQuantityPerEpisode();

        // 4. 현재 적용 가능한 프로모션 중
        List<PromotionEntity> activePromotions = promotionService.retrieveActivePromotions();

        // 4-1. COOKIE_QUANTITY_DISCOUNT에 해당하는 프로모션 조회
        List<PromotionEntity> cookieQuantityDiscountActivePromotion = activePromotions.stream()
                .filter(PromotionEntity::isCookieQuantityDiscountApplicable)
                .toList();

        List<Promotion> promotions = new ArrayList<>();
        for (PromotionEntity promotionEntity : cookieQuantityDiscountActivePromotion) {
            List<Attribute> attributes =
                    promotionAttributeRepository.findByPromotionEntityId(promotionEntity.getId()).stream()
                            .map(PromotionAttributeEntity::toModel)
                            .toList();

            Promotion promotion = new CookiePromotion(
                    promotionEntity.getDiscountQuantity(),
                    attributes
            );
            promotions.add(promotion);
        }

        // 5. 웹툰 구매시 쿠키 할인 개수
        int discount = promotions.stream()
                .map(p -> p.calculateDiscount(webtoon))
                .reduce(0, Integer::sum);

        // 6. 필요한 최종 쿠키 개수 : 정책 - 5번
        int totalCookieQuantityPerEpisode = max(0, cookiePerEpisode - discount);

        // 7. cookieWallet 결제한 만큼 감소 후 DB 저장
        CookieWalletEntity cookieWallet = cookieWalletService.retrieve(memberId);
        cookieWallet.decrease(totalCookieQuantityPerEpisode);
        cookieWalletRepository.save(cookieWallet);

        // 8. webtoonPaymentEntity 생성 후 DB 저장
        WebtoonPaymentEntity webtoonPayment = WebtoonPaymentEntity.create(
                memberId,
                webtoonId,
                request.getWebtoonDetailId(),
                Long.valueOf(totalCookieQuantityPerEpisode)
        );
        webtoonPaymentRepository.save(webtoonPayment);

    }

    private Long getMemberIdOrThrow(final Long memberId) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new DevtoonException(
                        ErrorCode.NOT_FOUND,
                        ErrorMessage.getResourceNotFound(ResourceType.MEMBER,
                                memberId))
                );
        return member.getId();
    }

    private Long getWebtoonIdOrThrow(final Long webtoonId) {
        WebtoonEntity webtoon = webtoonRepository.findById(webtoonId)
                .orElseThrow(() -> new DevtoonException(
                        ErrorCode.NOT_FOUND,
                        ErrorMessage.getResourceNotFound(ResourceType.WEBTOON, webtoonId))
                );
        return webtoon.getId();
    }

    /**
     * 특정 회원 웹툰 결제 내역 단건 조회
     */
    public WebtoonPaymentEntity retrieve(final Long memberId) {
        return webtoonPaymentRepository.findByMemberId(memberId)
                .orElseThrow(() -> new DevtoonException(ErrorCode.NOT_FOUND,
                        ErrorMessage.getResourceNotFound(ResourceType.WEBTOON_PAYMENT,
                                memberId)));
    }

}
