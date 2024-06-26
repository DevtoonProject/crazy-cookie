package yjh.devtoon.payment.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yjh.devtoon.cookie_wallet.domain.CookieWalletEntity;
import yjh.devtoon.cookie_wallet.infrastructure.CookieWalletRepository;
import yjh.devtoon.member.domain.MemberEntity;
import yjh.devtoon.payment.domain.CookiePaymentEntity;
import yjh.devtoon.payment.domain.Price;
import yjh.devtoon.payment.dto.request.CookiePaymentCreateRequest;
import yjh.devtoon.payment.infrastructure.CookiePaymentRepository;
import yjh.devtoon.policy.domain.CookiePolicyEntity;
import yjh.devtoon.policy.infrastructure.CookiePolicyRepository;
import yjh.devtoon.promotion.domain.DiscountType;
import yjh.devtoon.promotion.domain.PromotionEntity;
import yjh.devtoon.promotion.infrastructure.PromotionRepository;
import yjh.devtoon.member.domain.MembershipStatus;
import yjh.devtoon.member.infrastructure.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@DisplayName("통합 테스트 [CookiePaymentIntegration]")
@Transactional
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CookiePaymentIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CookiePolicyRepository cookiePolicyRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private CookieWalletRepository cookieWalletRepository;

    @Autowired
    private CookiePaymentRepository cookiePaymentRepository;

    @Autowired
    private MockMvc mockMvc;

    @WithMockUser(username = "email@gmail.com", password = "password", authorities = {"MEMBER"})
    @Nested
    @DisplayName("쿠키 결제 테스트")
    class CookiePaymentTests {

        @DisplayName("쿠키 결제 등록 성공")
        @Test
        void registerCookiePayment_successfully() throws Exception {

            // given
            // 1. member 등록
            MemberEntity savedMember =
                    memberRepository.save(MemberEntity.builder()
                            .name("홍길동")
                            .email("email@gmail.com")
                            .password("password")
                            .membershipStatus(MembershipStatus.GENERAL)
                            .build()
                    );

            // 2. cookie policy 등록
            CookiePolicyEntity savedCookiePolicy =
                    cookiePolicyRepository.save(CookiePolicyEntity.builder()
                            .id(1L)
                            .cookiePrice(BigDecimal.valueOf(200))
                            .cookieQuantityPerEpisode(5)
                            .startDate(LocalDateTime.parse("2024-05-05T00:00"))
                            .build()
                    );

            // 3. promotion 등록
            PromotionEntity savedPromotion = promotionRepository.save(PromotionEntity.builder()
                    .id(1L)
                    .description("12월 로맨스 프로모션입니다.")
                    .discountType(DiscountType.COOKIE_QUANTITY_DISCOUNT)
                    .discountQuantity(2)
                    .isDiscountDuplicatable(true)
                    .startDate(LocalDateTime.parse("2024-05-01T00:00"))
                    .build());

            // 4. cookie wallet 저장
            CookieWalletEntity savedCookieWallet =
                    cookieWalletRepository.save(CookieWalletEntity.builder()
                            .memberId(savedMember.getId())
                            .quantity(100)
                            .build());


            final CookiePaymentCreateRequest request = new CookiePaymentCreateRequest(
                    savedMember.getId(),
                    3
            );
            final String requestBody = objectMapper.writeValueAsString(request);

            // when, then
            mockMvc.perform(post("/v1/cookie-payments")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("성공"));

        }

        @DisplayName("특정 회원 쿠키 결제 내역 조회 성공")
        @Test
        void retrieveCookiePayment_forSpecificMember_successfully() throws Exception {

            // given
            // 1. member 등록
            MemberEntity savedMember =
                    memberRepository.save(MemberEntity.builder()
                            .name("둘리")
                            .email("email@gmail.ocm")
                            .password("password")
                            .membershipStatus(MembershipStatus.GENERAL)
                            .build()
                    );

            // 2. cookie payment 등록
            CookiePaymentEntity savedCookiePayment =
                    cookiePaymentRepository.save(CookiePaymentEntity.builder()
                            .cookiesPaymentId(1L)
                            .memberId(savedMember.getId())
                            .quantity(5)
                            .price(Price.of(200))
                            .totalDiscountRate(BigDecimal.valueOf(0.1))
                            .build());
            Long requestId = savedMember.getId();

            // when, then
            mockMvc.perform(get("/v1/cookie-payments/" + requestId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("성공"))
                    .andExpect(jsonPath("$.data.memberId").value(savedMember.getId()))
                    .andExpect(jsonPath("$.data.quantity").value(5))
                    .andExpect(jsonPath("$.data.totalPrice").value(1000))
                    .andExpect(jsonPath("$.data.totalDiscountRate").value(0.1))
                    .andExpect(jsonPath("$.data.paymentPrice").value(900));
        }

    }

}
