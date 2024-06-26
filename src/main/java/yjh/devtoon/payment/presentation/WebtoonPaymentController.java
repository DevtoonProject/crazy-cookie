package yjh.devtoon.payment.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yjh.devtoon.common.response.ApiResponse;
import yjh.devtoon.payment.application.WebtoonPaymentService;
import yjh.devtoon.payment.domain.WebtoonPaymentEntity;
import yjh.devtoon.payment.dto.request.WebtoonPaymentCreateRequest;
import yjh.devtoon.payment.dto.response.WebtoonPaymentRetrieveResponse;

@RequestMapping("/v1/webtoon-payments")
@RequiredArgsConstructor
@RestController
public class WebtoonPaymentController {

    private final WebtoonPaymentService webtoonPaymentService;

    /**
     * 웹툰 미리보기 결제
     * : 웹툰 미리보기는 쿠키로 결제한다.
     */
    @PostMapping
    public ResponseEntity<ApiResponse> register(
            @RequestBody final WebtoonPaymentCreateRequest request
    ) {
        webtoonPaymentService.register(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 특정 회원 웹툰 결제 내역 단건 조회
     */
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse> retrieve(
            @PathVariable final Long memberId
    ) {
        WebtoonPaymentEntity webtoonPayment = webtoonPaymentService.retrieve(memberId);
        WebtoonPaymentRetrieveResponse response = WebtoonPaymentRetrieveResponse.from(webtoonPayment);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
