package site.ncookie.springrestclientexample;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

import java.util.Map;

@SpringBootApplication
public class SpringRestClientExampleApplication {
    @Bean
    ApplicationRunner init(ErApi api) {
        return  args -> {
            // 환율 정보를 가지고 오는 API
            // https://open.er-api.com/v6/latest

            // RestTemplate
            RestTemplate rt = new RestTemplate();
            Map<String, Map<String, Double>> res =
                    rt.getForObject("https://open.er-api.com/v6/latest", Map.class);
            System.out.println(rt.getForObject("https://open.er-api.com/v6/latest", Map.class));
            System.out.println(res.get("rates").get("KRW"));

            // WebClient (Reactive 프로그래밍 방식)
            WebClient webClient = WebClient.create("https://open.er-api.com");
            Map<String, Map<String, Double>> res2 = webClient.get().uri("/v6/latest").retrieve().bodyToMono(Map.class).block();
            System.out.println(res2.get("rates").get("KRW"));

            // HTTP Interface
            HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                    .builder(WebClientAdapter.forClient(webClient))
                    .build();

            ErApi erApi = httpServiceProxyFactory.createClient(ErApi.class);
            Map<String, Map<String, Double>> res3 = erApi.getLatest();
            System.out.println(res3.get("rates").get("KRW"));

            // HTTP Interface를 Bean에 등록하여 사용
            Map<String, Map<String, Double>> res4 = api.getLatest();
            System.out.println(res4.get("rates").get("KRW"));

        };
    }

    // configuration에서 Bean으로 등록하여 사용
    @Bean
    ErApi erApi() {
        WebClient webClient = WebClient.create("https://open.er-api.com");
        HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
                .builder(WebClientAdapter.forClient(webClient))
                .build();
        return httpServiceProxyFactory.createClient(ErApi.class);
    }

    interface ErApi {
        @GetExchange("/v6/latest")
        Map getLatest();
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringRestClientExampleApplication.class, args);
    }

}
