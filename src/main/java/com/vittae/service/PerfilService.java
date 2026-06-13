package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.PerfilAtualizarDTO;
import com.vittae.dto.UsuarioPerfilDTO;
import com.vittae.util.ConfigUtil;

public class PerfilService {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/usuarios/";
    private final HttpClient client = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public UsuarioPerfilDTO buscarPerfil(Long id, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + id))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erro ao buscar perfil: " + response.statusCode());
        }

        return mapper.readValue(response.body(), UsuarioPerfilDTO.class);
    }

    public void atualizarDados(Long id, PerfilAtualizarDTO dto, String token) throws Exception {
        String json = mapper.writeValueAsString(dto);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + id + "/perfil"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erro ao atualizar dados: " + response.statusCode());
        }
    }

    public void trocarSenha(Long id, PerfilAtualizarDTO dto, String token) throws Exception {
        String json = mapper.writeValueAsString(dto);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(API_URL + id + "/senha"))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + token)
            .PUT(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Erro ao trocar senha: " + response.statusCode());
        }
    }
}