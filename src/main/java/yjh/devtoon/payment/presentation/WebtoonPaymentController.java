package yjh.devtoon.payment.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import yjh.devtoon.common.response.Response;
import yjh.devtoon.payment.application.WebtoonPaymentService;
import yjh.devtoon.payment.dto.request.WebtoonPaymentCreateRequest;

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
    public ResponseEntity<Response> register(
            @RequestBody final WebtoonPaymentCreateRequest request
    ) {
        webtoonPaymentService.register(request);
        return ResponseEntity.ok(Response.success(null));
    }
}
