package com.vittae.service;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vittae.dto.LoginResposta;
import com.vittae.model.Usuario;

import lombok.extern.log4j.Log4j;

@Log4j
@Named 
@ApplicationScoped
public class UsuarioService implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private final String API_URL = "http://localhost:9090/api/usuarios";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
    	    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    	    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);

    public LoginResposta autenticar(String cpf, String senha) {
        try {
            String jsonBody = String.format("{\"cpf\":\"%s\", \"senha\":\"%s\"}", cpf, senha);

            HttpRequest request = HttpRequest.newBuilder()
            		.uri(URI.create("http://localhost:9090/api/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(response.body());

                LoginResposta resposta = new LoginResposta();
                resposta.usuario = mapper.treeToValue(json.get("usuario"), Usuario.class);
                resposta.token   = json.get("token").asText();
                resposta.perfil  = json.get("perfil").asText();

                log.info("Usuário " + resposta.usuario.getNome() + " logado. Perfil: " + resposta.perfil);
                return resposta;
            }
        } catch (Exception e) {
            log.error("Erro na comunicação com a API (Login): " + e.getMessage());
        }
        return null;
    }

    public Usuario salvar(Usuario usuario) {
        try {
            String json = mapper.writeValueAsString(usuario);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cadastrar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Dados do usuário " + usuario.getNome() + " enviados para a API.");

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                // Devolve o usuário salvo (útil para pegar o ID gerado pelo banco)
                return mapper.readValue(response.body(), Usuario.class);
            }
        } catch (Exception e) {
            log.error("Erro ao salvar usuário: " + e.getMessage());
        }
        return usuario;
    }

    public List<Usuario> buscarTodos() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Transforma a lista JSON do Spring em uma List<Usuario> no Java
                return mapper.readValue(response.body(), new TypeReference<List<Usuario>>(){});
            }
        } catch (Exception e) {
            log.error("Erro ao buscar todos os usuários: " + e.getMessage());
        }
        return new ArrayList<>(); // Retorna vazio se der erro, para não quebrar a tela
    }

    public void excluir(Usuario usuario) {
        try {
            // Assumindo que a sua classe Usuario tem um getId()
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + usuario.getId())) 
                    .DELETE()
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Comando de exclusão enviado para a API. Usuário ID: " + usuario.getId());
        } catch (Exception e) {
            log.error("Erro ao excluir usuário: " + e.getMessage());
        }
    }
}