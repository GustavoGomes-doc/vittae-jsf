package com.vittae.view;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vittae.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Getter
@Setter
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Usuario usuario;

    // === Campos do seu formulário de Login ===
    private String cpf; 
    private String senha;

    // === Campos do seu formulário de Cadastro ===
    private String nome;
    private String email;
    private String cpfCadastro;
    private String telefone;
    private String senhaCadastro;

    @PostConstruct
    public void inicializar() {
        usuario = new Usuario();
        log.info("Iniciando LoginBean da Vittae");
    }

    public String login() {
        log.info("Tentando logar.......");

        HttpSession session = getSession(); 
        
        usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            
            // 1. Limpa a máscara do CPF que vem do PrimeFaces
            String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";

            // 2. Chama o método que conversa com o seu Spring Boot
            usuario = autenticarNoSpring(cpfLimpo, senha);
            
            if (usuario != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Login realizado com sucesso!", usuario.getNome()));
                log.info("Usuário logado: " + usuario.toString());

                session.setAttribute("usuario", usuario);
                
                return "/views/comum/Inicio.xhtml?faces-redirect=true";
                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login falhou!", "CPF ou senha incorretos"));
                
                return "/Login.xhtml"; 
            }
        }
        return "/views/comum/Inicio.xhtml?faces-redirect=true";
    }

    public String sair() {
        log.info("Session invalidate");

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/Login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "/Login.xhtml?faces-redirect=true";
    }

    private HttpSession getSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getSession();
    }

    // =========================================================================
    // COMUNICAÇÃO COM O SPRING BOOT
    // =========================================================================
    
    private Usuario autenticarNoSpring(String cpfLimpo, String senha) {
        try {
            String jsonBody = String.format("{\"cpf\":\"%s\", \"senha\":\"%s\"}", cpfLimpo, senha);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/auth/login")) 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(response.body(), Usuario.class);
            }
        } catch (Exception e) {
            log.error("Erro ao conectar na API do Spring: " + e.getMessage());
        }
        return null;
    }

    // =========================================================================
    // MÉTODOS PARA A TELA NÃO QUEBRAR E FUNCIONAR
    // =========================================================================
    
    public void cadastrar() {
        log.info("Iniciando tentativa de cadastro...");
        try {
            // 1. Limpa as máscaras do CPF e do Telefone para enviar apenas números
            String cpfLimpo = this.cpfCadastro != null ? this.cpfCadastro.replaceAll("\\D", "") : "";
            String telLimpo = this.telefone != null ? this.telefone.replaceAll("\\D", "") : "";

            // 2. Monta o JSON. Como é o Front-End, colocamos tudo num pacote só (inclusive o telefone).
            // O perfil vai fixo como PACIENTE para o Back-End saber de quem se trata.
            String jsonBody = String.format(
                "{\"nome\":\"%s\", \"email\":\"%s\", \"cpf\":\"%s\", \"senha\":\"%s\", \"telefone\":\"%s\", \"perfil\":\"PACIENTE\"}",
                this.nome, this.email, cpfLimpo, this.senhaCadastro, telLimpo
            );
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    // ATENÇÃO: Confirme com seu colega se a URL de cadastro é essa mesma!
                    .uri(URI.create("http://localhost:8081/api/auth/cadastrar")) 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Cadastro realizado! Agora você pode fazer o login."));
                
                // Limpa os campos do formulário para a tela ficar em branco novamente
                this.nome = "";
                this.email = "";
                this.cpfCadastro = "";
                this.telefone = "";
                this.senhaCadastro = "";
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro!", "Não foi possível criar a conta."));
                log.error("Erro no Spring. Status: " + response.statusCode() + ". Retorno: " + response.body());
            }
        } catch (Exception e) {
            log.error("Erro ao conectar na API de Cadastro do Spring: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Falha de Conexão", "O servidor pode estar desligado."));
        }
    }

    public void recuperarSenha() { }
    public void mostrarLogin() { }
    public void mostrarCadastro() { }
}