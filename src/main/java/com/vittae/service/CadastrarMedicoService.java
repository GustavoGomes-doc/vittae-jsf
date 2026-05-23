package com.vittae.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // Import obrigatório para configurar a data
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.MedicoEnvioDTO;


public class CadastrarMedicoService {

    private static final String API_URL = "http://localhost:8082/api/medicos";

    public void salvarMedico(MedicoEnvioDTO dto) throws Exception {
        try {
            // Configurar o Jackson para entender datas (LocalDate e LocalTime)
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); 
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // Converter o objeto Medico para String JSON
            String json = mapper.writeValueAsString(dto);
            
            // Dica de Ouro: Deixe esse print para você ver no console do Eclipse como o JSON ficou!
            System.out.println("Enviando JSON: " + json);

            // Criar o Cliente HTTP
            HttpClient client = HttpClient.newHttpClient();

            // Montar a Requisição POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json") // avisa o spring que esta mandando um JSON
                    .POST(BodyPublishers.ofString(json)) // pega o JSON gerado e coloca no corpo da mensagem HTTP 
                    .build();

            // Enviar e receber a resposta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Validar o resultado (200 OK ou 201 Created)
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("Médico enviado com sucesso para a API!");
            } else {
                throw new Exception("Falha ao salvar médico na API. Status: " + response.statusCode() 
                                    + " - Erro: " + response.body());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Erro de comunicação com a API: " + e.getMessage());
        }
    }
}

