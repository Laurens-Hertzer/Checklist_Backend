package ch.bbw.lah.checklist_backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI checklistApiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("Morning Checklist API")
                        .description("REST API für Checklisten, Einstellungen sowie Zug- und Wetterdaten.")
                        .version("1.0.0"));
    }
}
