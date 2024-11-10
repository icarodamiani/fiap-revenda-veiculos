package io.fiap.revenda.veiculos.driven.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.CheckedFunction1;
import io.vavr.Function1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
public class SqsMessageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageClient.class);

    private final SqsAsyncClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String numberOfMessages;
    private final String waitTimeMessage;
    private final String visibilityTimeOut;

    public SqsMessageClient(SqsAsyncClient sqsClient,
                            ObjectMapper objectMapper,
                            @Value("${aws.sqs.numberOfMessages}") String numberOfMessages,
                            @Value("${aws.sqs.waitTimeMessage}") String waitTimeMessage,
                            @Value("${aws.sqs.visibilityTimeOut}") String visibilityTimeOut) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.numberOfMessages = numberOfMessages;
        this.waitTimeMessage = waitTimeMessage;
        this.visibilityTimeOut = visibilityTimeOut;
    }

    public Mono<ReceiveMessageResponse> receive(String queueName) {
        return getQueueUrl().apply(queueName)
            .map(GetQueueUrlResponse::queueUrl)
            .map(queueUrl -> ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .waitTimeSeconds(Integer.parseInt(waitTimeMessage))
                .maxNumberOfMessages(Integer.parseInt(numberOfMessages))
                .visibilityTimeout(Integer.parseInt(visibilityTimeOut))
                .build()
            ).flatMap(request -> Mono.fromFuture(sqsClient.receiveMessage(request)));
    }

    public Mono<DeleteMessageResponse> delete(String queueName, Message message) {
        return Mono.fromFuture(sqsClient.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueName)
                .receiptHandle(message.receiptHandle()).build()))
            .doOnSuccess(deleteMessageResponse -> LOGGER.info("queue message has been deleted: {}", message.messageId()))
            .doOnError(throwable -> LOGGER.error("an error occurred while deleting message", throwable));
    }


    public <T> Mono<Void> send(String queueName, T payload) {
        return Mono.just(serializePayload().unchecked().apply(payload))
            .zipWith(getQueueUrl().apply(queueName))
            .map(t -> buildMessageRequest().unchecked().apply(t))
            .doOnError(throwable -> LOGGER.error("Failed to prepare message due to error.", throwable))
            .flatMap(message -> Mono.fromFuture(sqsClient.sendMessage(message)))
            .doOnError(throwable -> LOGGER.error("Failed to send message due to error.", throwable))
            .doOnSuccess(response ->
                LOGGER.debug("Message published to queue. Message ID: {} Body: {}", response.messageId(),
                    response.md5OfMessageBody()))
            .then();
    }

    private <T> CheckedFunction1<T, String> serializePayload() {
        return objectMapper::writeValueAsString;
    }

    private Function1<String, Mono<GetQueueUrlResponse>> getQueueUrl() {
        return queueName -> Mono.fromFuture(sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(queueName)
                .build()))
            .doOnError(throwable -> LOGGER.error("Failed to get queueUrl", throwable));
    }

    private CheckedFunction1<Tuple2<String, GetQueueUrlResponse>, SendMessageRequest> buildMessageRequest() {
        return t -> SendMessageRequest.builder()
            .messageBody(t.getT1())
            .queueUrl(t.getT2().queueUrl())
            .build();
    }
}
