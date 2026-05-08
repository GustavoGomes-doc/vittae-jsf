package com.vittae.view;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import com.vittae.model.Usuario;
import com.vittae.Service.UsuarioService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Getter
@Setter
@Named
@SessionScoped // Mantém o usuário "vivo" durante toda a navegação no site
public class UsuarioLogadoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@Inject
	private UsuarioService usuarioService;

	// Variável que vai guardar o usuário que está navegando no sistema
	private Usuario usuarioLogado;
	
	// Campos apenas para pegar o que foi digitado na tela de login
	private String cpf;
	private String senha;

	/**
	 * Método chamado pelo botão "Entrar" do JSF
	 */
	public String logar() {
		log.info("Tentando fazer login no sistema...");

		// O JSF traz o CPF com os pontos e traços da máscara. 
		// Precisamos limpar (deixar só os números) antes de mandar para a API
		String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";

		// Chama a API usando o UsuarioService que fizemos
		Usuario usuarioRetorno = usuarioService.autenticar(cpfLimpo, senha);

		if (usuarioRetorno != null) {
			this.usuarioLogado = usuarioRetorno;
			log.info("Usuário " + usuarioLogado.getNome() + " logado com sucesso!");
			
			// Redireciona para a página interna (dashboard)
			// Troque "vittae" pelo nome do seu arquivo XHTML principal (ex: index, home)
			return "vittae?faces-redirect=true";
			
		} else {
			log.error("Falha no login. CPF ou senha incorretos.");
			
			// Envia uma mensagem de erro vermelha para a tela do usuário
			FacesContext.getCurrentInstance().addMessage(null, 
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "CPF ou senha incorretos."));
			
			// Retorna null para o JSF continuar na tela de login e mostrar o erro
			return null; 
		}
	}

	/**
	 * Método para sair do sistema
	 */
	public String deslogar() {
		this.usuarioLogado = null;
		// Destrói a sessão do usuário
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		
		log.info("Sessão encerrada.");
		// Redireciona de volta para a tela de login
		return "login?faces-redirect=true";
	}

	/**
	 * Atalho para verificar no XHTML se a pessoa tem permissão de ver a página
	 */
	public boolean isLogado() {
		return usuarioLogado != null;
	}
}