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
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vittae.model.Usuario;
import com.vittae.service.UsuarioService;

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

    private Usuario usuario = new Usuario();

    // --- Campos de Login ---
    private String cpf; 
    private String senha;

    // --- Campos de Cadastro ---
    private String nome;
    private String email;
    private String cpfCadastro;
    private String telefone;
    private String senhaCadastro;
    private String cep;
    
    @Inject
    private UsuarioService usuarioService;

    @PostConstruct
    public void inicializar() {
        usuario = new Usuario();
        log.info("Iniciando LoginBean da Vittae");
    }

    // ==========================================
    // MÉTODOS DE AÇÃO (TELAS)
    // ==========================================

    public String login() {
        log.info("Tentando logar.......");

        HttpSession session = getSession(); 
        usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";
            
            // Chama a API do Spring
            usuario = usuarioService.autenticar(cpfLimpo, senha);
            
            if (usuario != null) {
            	session.setAttribute("usuario", usuario);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Login realizado com sucesso!", usuario.getNome()));
                log.info("Usuário logado: " + usuario.toString());

                session.setAttribute("usuario", usuario);
                return "/views/pacientes/agendarConsulta.xhtml?faces-redirect=true";
                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login falhou!", "CPF ou senha incorretos"));
                return "/login.xhtml"; 
            }
        }
        return "/views/pacientes/agendarConsulta.xhtml?faces-redirect=true";
    }

    public String sair() {
        log.info("Session invalidate");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(
                    FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath() + "/login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "/login.xhtml?faces-redirect=true"; // Corrigido para minúsculo
    }

    public void cadastrar() {
        log.info("Iniciando tentativa de cadastro...");
        try {
            String cpfLimpo = this.cpfCadastro != null ? this.cpfCadastro.replaceAll("\\D", "") : "";
            String telLimpo = this.telefone != null ? this.telefone.replaceAll("\\D", "") : "";
            String cepLimpo = this.cep != null ? this.cep.replaceAll("\\D", "") : "";

            // ✅ CORREÇÃO 1: Ajustei para ter exatamente 6 variáveis para os 6 espaços (%s)
            String jsonBody = String.format(
                "{\"nome\":\"%s\", \"email\":\"%s\", \"cpf\":\"%s\", \"cep\":\"%s\", \"senha\":\"%s\", \"telefone\":\"%s\", \"perfil\":\"PACIENTE\"}",
                this.nome, this.email, cpfLimpo, cepLimpo, this.senhaCadastro, telLimpo
            );
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    // ✅ CORREÇÃO 2: A URL agora aponta para /api/usuarios/cadastrar
                    .uri(URI.create("http://localhost:8083/api/usuarios/cadastrar")) 
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 201 || response.statusCode() == 200) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Cadastro realizado! Agora você pode fazer o login."));
                
                // Limpa os campos após o cadastro
                this.nome = ""; this.email = ""; this.cpfCadastro = "";
                this.telefone = ""; this.cep = ""; this.senhaCadastro = "";
                
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

   
    private HttpSession getSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getSession();
    }

    public Usuario getUsuarioLogado() {
        HttpSession session = getSession();
        return (Usuario) session.getAttribute("usuario");
    }
    

    public String mostrarLogin() {
        log.info("Alternando para Login");
        return null; // O null diz ao JSF: "execute o método e fique na mesma página"
    }

    public String mostrarCadastro() {
        log.info("Alternando para Cadastro");
        return null; 
    }

    public String recuperarSenha() {
        log.info("Recuperar senha");
        return null;
    }
}