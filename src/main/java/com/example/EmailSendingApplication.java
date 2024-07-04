package com.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

import java.util.Properties;

@Slf4j
public class EmailSendingApplication {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "email-sending-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
//        props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams"); // for state store (RockDB 저장 경로)
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, "1");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(StreamsConfig.EXACTLY_ONCE_V2, "true"); // Exactly-once 설정
//        props.put("retention.ms", 10_000_000); // 10 seconds // 리밸런싱 및 내부 토픽의 데이터 보존 시간 설정


        StreamsBuilder builder = new StreamsBuilder();

        // EmailRequestsTopic에서 메시지를 읽고 키를 설정 (String으로 처리)
        KStream<String, String> emailRequests = builder.stream("EmailRequestsTopic",
                        Consumed.with(Serdes.String(), Serdes.String()))
                .selectKey((key, value) -> key)
                .peek((key, value) -> log.info("Email request - Key: {}, Value: {}", key, value));

        // UserTableTopic에서 메시지를 읽고 키를 설정 (String으로 처리)
        KTable<String, String> userTable = builder.table("UserTableTopic",
                        Consumed.with(Serdes.String(), Serdes.String()),
                        Materialized.with(Serdes.String(), Serdes.String()))
                .filter((key, value) -> key != null)
                .mapValues((key, value) -> {
                    log.info("User record - Key: {}, Value: {}", key, value);
                    return value;
                });

        // KStream과 KTable 조인 (String으로 처리)
        KStream<String, String> joinedStream = emailRequests.join(
                userTable,
                (emailRequest, userInfo) -> {
                    if (userInfo == null) {
                        return "Invalid user info for request: " + emailRequest;
                    }
                    return emailRequest + ", User Info: " + userInfo;
                }
        );

        // 조인된 결과를 로그로 출력
        joinedStream.foreach(
                (key, value) -> log.info("Joined message - Key: {}, Value: {}", key, value)
        );

//        // SES 클라이언트 설정
//        AwsBasicCredentials awsCreds = AwsBasicCredentials.create("YOUR_ACCESS_KEY_ID", "YOUR_SECRET_ACCESS_KEY");
//        SesClient sesClient = SesClient.builder()
//                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
//                .region(Region.US_EAST_1)
//                .build();
//
//        // 조인된 결과를 이메일로 발송
//        joinedStream.foreach((key, value) -> {
//            try {
//                String[] parts = value.split(", User Info: ");
//                String emailRequest = parts[0];
//                String userInfo = parts[1];
//
//                // Email request와 user info를 파싱하여 이메일 전송
//                sendEmail(sesClient, "from@example.com", "to@example.com", "Subject: Email Request", emailRequest + "\n\n" + userInfo);
//                log.info("Email sent successfully for key: {}, value: {}", key, value);
//            } catch (Exception e) {
//                log.error("Failed to send email for key: {}, value: {}", key, value, e);
//            }
//        });


        KafkaStreams streams = new KafkaStreams(builder.build(), props);

        streams.setUncaughtExceptionHandler(
                exception -> {
                    log.error("Stream thread encountered an uncaught exception: ", exception);
                    streams.close();
                    return null;
                }
        );

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private static void sendEmail(SesClient client, String from, String to, String subject, String body) {
        Destination destination = Destination.builder()
                .toAddresses(to)
                .build();

        Message msg = Message.builder()
                .subject(Content.builder().data(subject).build())
                .body(Body.builder().text(Content.builder().data(body).build()).build())
                .build();

        SendEmailRequest request = SendEmailRequest.builder()
                .destination(destination)
                .message(msg)
                .source(from)
                .build();

        client.sendEmail(request);
    }

}
