package com.vittae.view;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vittae.dto.PerfilAtualizarDTO;
import com.vittae.dto.UsuarioPerfilDTO;
import com.vittae.service.PerfilService;

@Named("perfilBean")
@RequestScoped
public class PerfilBean implements Serializable {

	private static final long serialVersionUID = 1L;

	// Dados exibidos
	private UsuarioPerfilDTO usuario;

	// Campos editáveis
	private String novoEmail;
	private String novoTelefone;
	private String senhaAtual;
	private String novaSenha;
	private String confirmarSenha;
	private BigDecimal novoValorConsulta;
	private Integer novoTempoConsulta;

	private final PerfilService perfilService = new PerfilService();

	// Inicializa ao carregar a página
	public void init() {
		HttpSession session = getSession();
		String token = (String) session.getAttribute("token");
		Long id = (Long) session.getAttribute("usuarioId");

		if (token != null && id != null) {
			try {
				usuario = perfilService.buscarPerfil(id, token);
				novoEmail = usuario.getEmail();
				novoTelefone = usuario.getTelefone();
				
				if (usuario != null && "MEDICO".equals(usuario.getPerfil())) {
					novoValorConsulta = usuario.getValorConsulta();
					novoTempoConsulta = usuario.getTempoConsultaMinutos();
				}
				
			} catch (Exception e) {
				addErro("Erro ao carregar perfil: " + e.getMessage());
			}
		}
	}

	public void salvarDados() {
		HttpSession session = getSession();
		String token = (String) session.getAttribute("token");
		Long id = (Long) session.getAttribute("usuarioId");

		try {
			PerfilAtualizarDTO dto = new PerfilAtualizarDTO();
			dto.setEmail(novoEmail);
			dto.setTelefone(novoTelefone);
			dto.setValorConsulta(novoValorConsulta);   
			dto.setTempoConsultaMinutos(novoTempoConsulta);

			perfilService.atualizarDados(id, dto, token);

			// Atualiza email na sessão se mudou
			session.setAttribute("usuarioEmail", novoEmail);

			addSucesso("Dados updated com sucesso!");
		} catch (Exception e) {
			addErro("Erro ao atualizar dados: " + e.getMessage());
		}
	}

	public void trocarSenha() {
		if (!novaSenha.equals(confirmarSenha)) {
			addErro("As senhas não coincidem.");
			return;
		}
		if (novaSenha.length() < 6) {
			addErro("A senha deve ter no mínimo 6 caracteres.");
			return;
		}

		HttpSession session = getSession();
		String token = (String) session.getAttribute("token");
		Long id = (Long) session.getAttribute("usuarioId");

		try {
			PerfilAtualizarDTO dto = new PerfilAtualizarDTO();
			dto.setSenhaAtual(senhaAtual);
			dto.setNovaSenha(novaSenha);

			perfilService.trocarSenha(id, dto, token);
			addSucesso("Senha alterada com sucesso!");

			// Limpa campos
			senhaAtual = null;
			novaSenha = null;
			confirmarSenha = null;
		} catch (Exception e) {
			addErro("Erro ao trocar senha: " + e.getMessage());
		}
	}

	private HttpSession getSession() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		return request.getSession(false);
	}

	private void addSucesso(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	private void addErro(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	// Getters e Setters
	public UsuarioPerfilDTO getUsuario() {
		return usuario;
	}

	public void setUsuario(UsuarioPerfilDTO usuario) {
		this.usuario = usuario;
	}

	public String getNovoEmail() {
		return novoEmail;
	}

	public void setNovoEmail(String novoEmail) {
		this.novoEmail = novoEmail;
	}

	public String getNovoTelefone() {
		return novoTelefone;
	}

	public void setNovoTelefone(String novoTelefone) {
		this.novoTelefone = novoTelefone;
	}

	public String getSenhaAtual() {
		return senhaAtual;
	}

	public void setSenhaAtual(String senhaAtual) {
		this.senhaAtual = senhaAtual;
	}

	public String getNovaSenha() {
		return novaSenha;
	}

	public void setNovaSenha(String novaSenha) {
		this.novaSenha = novaSenha;
	}

	public String getConfirmarSenha() {
		return confirmarSenha;
	}

	public void setConfirmarSenha(String confirmarSenha) {
		this.confirmarSenha = confirmarSenha;
	}

	public BigDecimal getNovoValorConsulta() {
		return novoValorConsulta;
	}

	public void setNovoValorConsulta(BigDecimal Float) {
		this.novoValorConsulta = Float;
	}

	public Integer getNovoTempoConsulta() {
		return novoTempoConsulta;
	}

	public void setNovoTempoConsulta(Integer novoTempoConsulta) {
		this.novoTempoConsulta = novoTempoConsulta;
	}
}