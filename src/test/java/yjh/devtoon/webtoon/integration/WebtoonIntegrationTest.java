package yjh.devtoon.webtoon.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yjh.devtoon.webtoon.domain.WebtoonEntity;
import yjh.devtoon.webtoon.dto.request.WebtoonCreateRequest;
import yjh.devtoon.webtoon.infrastructure.WebtoonRepository;
import java.util.Optional;
import java.util.stream.Stream;

@DisplayName("통합 테스트 [Webtoon]")
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebtoonIntegrationTest {

    private static final String NULL = null;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebtoonRepository webtoonRepository;

    @Nested
    @DisplayName("웹툰 등록 테스트")
    class RegisterWebtoonTests{

        @DisplayName("웹툰 등록 성공")
        @Test
        void registerWebtoon_successfully() throws Exception {
            // given
            final WebtoonCreateRequest request = new WebtoonCreateRequest(
                    "쿠베라",
                    "카레곰"
            );
            final String requestBody = objectMapper.writeValueAsString(request);

            // when
            mockMvc.perform(post("/v1/webtoons")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("성공"))
                    .andExpect(jsonPath("$.data").value(NULL))
                    .andReturn();
        }

        @DisplayName("웹툰 등록 실패 - 필드가 null인 경우")
        @ParameterizedTest
        @CsvSource(value = {", 카레곰", "쿠베라, "}, delimiter = ',')
        void givenNullField_whenRegisterWebtoon_thenThrowException(String title, String writerName) throws Exception {
            // given
            final WebtoonCreateRequest request = new WebtoonCreateRequest(title, writerName);
            final String requestBody = objectMapper.writeValueAsString(request);

            // when
            mockMvc.perform(post("/v1/webtoons")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("실패"))
                    .andExpect(jsonPath("$.data.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
        }

        @DisplayName("웹툰 등록 실패 - 필드가 공백인 경우")
        @ParameterizedTest
        @CsvSource(value = {"' ', 카레곰", "쿠베라, ' '"}, delimiter = ',')
        void givenEmptyField_whenRegisterWebtoon_thenThrowException(String title, String writerName) throws Exception {
            // given
            final WebtoonCreateRequest request = new WebtoonCreateRequest(title, writerName);
            final String requestBody = objectMapper.writeValueAsString(request);

            // when
            mockMvc.perform(post("/v1/webtoons")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("실패"))
                    .andExpect(jsonPath("$.data.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
        }

        @DisplayName("웹툰 등록 실패 - 필드 사이즈 범위가 [1~20]이 아닌경우")
        @ParameterizedTest
        @CsvSource(value = {"123456789012345678901, 카레곰", "쿠베라, 123456789012345678901"}, delimiter = ',')
        void givenNotRangeFiled_whenRegisterWebtoon_thenThrowException(String title, String writerName) throws Exception {
            // given
            final WebtoonCreateRequest request = new WebtoonCreateRequest(title, writerName);
            final String requestBody = objectMapper.writeValueAsString(request);

            // when
            mockMvc.perform(post("/v1/webtoons")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statusMessage").value("실패"))
                    .andExpect(jsonPath("$.data.status").value(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();
        }

        @DisplayName("웹툰 등록 실패 - 중복된 웹툰 이름 존재")
        @TestFactory
        Stream<DynamicTest> givenDuplicatedTitleField_whenRegisterWebtoon_thenThrowException() {
            return Stream.of(
                    DynamicTest.dynamicTest("1. 웹툰 저장", () -> {
                        // given
                        WebtoonEntity webtoonEntity = WebtoonEntity.builder()
                                .title("쿠베라")
                                .writerName("카레곰")
                                .build();

                        // when
                        webtoonRepository.save(webtoonEntity);

                        // then
                        Optional<WebtoonEntity> saved = webtoonRepository.findByTitle("쿠베라");
                        assertAll(
                                () -> assertThat(saved.isPresent()).isTrue(),
                                () -> assertThat(saved.get().getTitle()).isEqualTo("쿠베라")
                        );

                    }), DynamicTest.dynamicTest("2. 중복된 웹툰 저장 시도", () -> {
                        // given
                        final WebtoonCreateRequest request = new WebtoonCreateRequest(
                                "쿠베라",
                                "짜장곰"
                        );
                        final String requestBody = objectMapper.writeValueAsString(request);

                        // when
                        mockMvc.perform(post("/v1/webtoons")
                                        .content(requestBody)
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.statusMessage").value("실패"))
                                .andExpect(jsonPath("$.data.status").value(HttpStatus.CONFLICT.value()))
                                .andReturn();
                    })
            );
        }
    }

}