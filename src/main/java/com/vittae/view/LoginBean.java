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
    private String cpf; // O professor usou email, mas a sua tela pede CPF
    private String senha;

    // === Campos do seu formulário de Cadastro ===
    // Sem isso, a sua tela Login.xhtml vai dar erro ao carregar!
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

        // Se usuário não está na sessão = não está logado
        if (usuario == null) {
            
            // 1. Limpa a máscara do CPF que vem do PrimeFaces
            String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";

            // 2. Chama o método que conversa com o seu Spring Boot (substitui o DAO/Service local)
            usuario = autenticarNoSpring(cpfLimpo, senha);
            
            if (usuario != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Login realizado com sucesso!", usuario.getNome()));
                log.info("Usuário logado: " + usuario.toString());

                session.setAttribute("usuario", usuario);
                
                // Redireciona para a página interna do SEU sistema
                return "/views/comum/Inicio.xhtml?faces-redirect=true";
                
            } else {
                /* Não autenticado */
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login falhou!", "CPF ou senha incorretos"));
                
                return "/Login.xhtml"; // Fica na tela de login
            }
        }
        return "/views/comum/Inicio.xhtml?faces-redirect=true";
    }

    public String sair() {
        log.info("Session invalidate");

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        try {
            // Mudei o redirecionamento do professor ("/index.xhtml") para a sua tela "/Login.xhtml"
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
    // MÉTODO ISOLADO PARA CONVERSAR COM O BACK-END (SPRING BOOT)
    // =========================================================================
    private Usuario autenticarNoSpring(String cpfLimpo, String senha) {
        try {
            String jsonBody = String.format("{\"cpf\":\"%s\", \"senha\":\"%s\"}", cpfLimpo, senha);
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    // ATENÇÃO: Ajuste a porta aqui para a porta que o seu colega usou no Spring!
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
    // MÉTODOS PARA A TELA NÃO QUEBRAR (Ações dos botões)
    // =========================================================================
    public void cadastrar() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso", "Ainda precisa plugar na API de cadastro."));
    }
    public void recuperarSenha() { }
    public void mostrarLogin() { }
    public void mostrarCadastro() { }
}