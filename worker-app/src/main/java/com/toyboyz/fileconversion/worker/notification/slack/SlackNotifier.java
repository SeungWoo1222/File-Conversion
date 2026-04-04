package com.toyboyz.fileconversion.worker.notification.slack;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class SlackNotifier {

    private final String url;
    private final WebClient webClient;

    public SlackNotifier(@Value("${slack.webhook.url}") String url, WebClient.Builder webClient) {
        this.url = url;
        this.webClient = webClient.build();
    }

    public void sendSlackNotification(String message) {
        try {
            webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("text", message))
                    .retrieve()
                    .toBodilessEntity()
                    .subscribe(res -> {
                        log.info("[Slack] 메세지 전송에 성공했습니다.");
                    }, error -> {
                        log.error("[Slack] 메세지 전송에 실패했습니다. : {}", error.getMessage());
                    });
        } catch (Exception e) {
            log.error("[Slack] 알림 프로세스 중 예외 발생 : {}",e.getMessage());
        }
    }

    //[ref] 현재는 DLQ 전용 하드코딩되어있지만 좀더 글로벌하게 처리되어야함
    public String createErrorMessage(String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[SpringBoot-File-Conversion-Worker]").append("\n");
        sb.append("발생 시간 : "+ LocalDateTime.now()).append("\n");
        sb.append("Error File : ").append(message).append("\n");
        sb.append("DLQ 로 메세지가 이관되었습니다. 해당 변환 요청은 처리되지 않습니다.");
        return sb.toString();
    }


}










