package com.vittae.Service;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.vittae.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j;

@Log4j
public class UsuarioService implements Serializable {

    private static final long serialVersionUID = 1L;
    
    // URL da API que o seu colega está rodando
    private final String API_URL = "http://localhost:8081/api/usuarios";

    /**
     * Realiza o login conforme o caso de uso "Fazer login" do diagrama.
     */
    public Usuario autenticar(String cpf, String senha) {
        try {
            log.info("Iniciando autenticação para o CPF: " + cpf);
            
            // Monta o JSON com os campos EXATOS do seu Modelo de Objetos
            String jsonBody = String.format("{\"cpf\":\"%s\", \"senha\":\"%s\"}", cpf, senha);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                // Transforma o JSON da API no seu objeto Usuario (nome, cpf, emnail, senha)
                Usuario usuario_db = mapper.readValue(response.body(), Usuario.class);
                
                log.info("Usuário " + usuario_db.getNome() + " logado com sucesso.");
                return usuario_db;
            }
            
        } catch (Exception e) {
            log.error("Erro na comunicação com a API: " + e.getMessage());
        }
        return null;
    }

    /**
     * Baseado no Modelo de Objetos: cadastra um novo Usuário.
     */
    public void salvar(Usuario usuario) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(usuario);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/cadastrar"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Dados do usuário " + usuario.getNome() + " enviados para a API.");
            
        } catch (Exception e) {
            log.error("Erro ao salvar usuário: " + e.getMessage());
        }
    }
}