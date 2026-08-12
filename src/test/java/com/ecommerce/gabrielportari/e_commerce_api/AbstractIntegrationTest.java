package com.ecommerce.gabrielportari.e_commerce_api;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

/**
 * Base de teste de integração: sobe um Postgres real via Testcontainers (Docker) e roda as
 * migrations Flyway de verdade, em vez de simular com H2 — mesmo motivo do roadmap.md (as
 * migrations usam sintaxe específica do Postgres). O container é estático, iniciado manualmente
 * (sem {@code @Testcontainers}/{@code @Container}) e nunca parado explicitamente — é o padrão
 * "singleton container" recomendado pelo Spring/Testcontainers pra reaproveitar UM container
 * entre TODAS as classes de teste da suíte; com a extensão JUnit5 gerenciando o ciclo de vida via
 * anotação, cada classe para o container no fim, e a próxima classe falhava tentando conectar
 * numa porta que não existia mais (raiz do timeout/connection-refused visto antes dessa mudança).
 * Cada teste roda numa transação própria, desfeita ao final (@Transactional) — evita que testes
 * precisem limpar dados uns dos outros ou usar nomes únicos pra não colidir (só o usuário admin
 * do seed V2 sobrevive entre testes, o resto do banco começa limpo a cada método).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@Transactional
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @PersistenceContext
    protected EntityManager entityManager;

    /**
     * Várias chamadas MockMvc num teste compartilham a mesma transação/sessão Hibernate (efeito do
     * @Transactional acima), diferente da produção, onde cada request tem sua própria. Sem isso,
     * uma entidade lida antes de outra chamada mutar dados relacionados (ex.: coleção lazy) fica
     * presa no cache de 1º nível com o estado antigo — não é bug de produção, é artefato do teste.
     * Chame entre um "request" que muda dado e outro que lê, simulando sessões separadas.
     */
    protected void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }

    protected String adminToken() throws Exception {
        String body =
                """
                {"email":"admin@ecommerce.com","password":"admin123"}
                """;
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                "/api/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(response).get("token").asText();
    }
}
