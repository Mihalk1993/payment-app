package eu.paze.paymentapp.service;

import eu.paze.paymentapp.model.PaymentRequest;
import eu.paze.paymentapp.model.PaymentResponse;
import eu.paze.paymentapp.util.exception.PaymentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class PaymentService {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String bearerToken;
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(RestTemplateBuilder restTemplateBuilder,
                          @Value("${baseUrl}") String baseUrl,
                          @Value("${bearerToken}") String bearerToken) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
        this.bearerToken = bearerToken;
    }

    public String createPayment(BigDecimal amount) throws PaymentException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(bearerToken);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setPaymentType("DEPOSIT");
        paymentRequest.setAmount(amount);
        paymentRequest.setCurrency("EUR");

        HttpEntity<PaymentRequest> requestEntity = new HttpEntity<>(paymentRequest, headers);
        ResponseEntity<PaymentResponse> responseEntity;
        try {
            responseEntity = restTemplate.postForEntity(baseUrl + "/payments", requestEntity, PaymentResponse.class);
        } catch (RuntimeException e) {
            throw new PaymentException("Invalid payment request");
        }

        HttpStatus statusCode = (HttpStatus) responseEntity.getStatusCode();
        PaymentResponse paymentResponse = responseEntity.getBody();

        switch (statusCode) {
            case OK:
                if (paymentResponse == null || paymentResponse.getResult() == null || paymentResponse.getResult().getRedirectUrl() == null) {
                    log.error("Something went wrong with the response: {}", paymentResponse);
                    throw new PaymentException("Invalid payment response");
                }
                log.info("Payment created successfully");
                return paymentResponse.getResult().getRedirectUrl();
            case BAD_REQUEST:
                log.error("Invalid request parameters: {}", responseEntity);
                throw new PaymentException("Invalid request parameters");
            case UNAUTHORIZED:
                log.error("Unauthorized access to the API: {}", responseEntity);
                throw new PaymentException("Unauthorized access to the API");
            default:
                log.error("Failed to create payment: {}", responseEntity);
                throw new PaymentException("Failed to create payment");
        }
    }
}