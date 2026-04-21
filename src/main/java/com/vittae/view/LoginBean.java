package com.vittae.view; 

import com.vittae.model.Usuario;
import com.vittae.model.dao.UsuarioDao;
import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

@Named("loginBean")
@RequestScoped
public class LoginBean implements Serializable {

	private String cpf;
	private String senha;

	@Inject
	private UsuarioLogadoBean usuarioLogadoBean;

	// DAO descomentado para buscar no banco real!
	@Inject
	private UsuarioDao usuarioDao; 

	public String entrar() {
		// 1. Limpa a máscara do CPF que veio do front-end (remove pontos e traços)
		String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";

		// 2. Busca o usuário no banco de dados usando o CPF limpo
		Usuario user = usuarioDao.autenticar(cpfLimpo, senha);

		// 3. Se achou o usuário no banco e a senha bate:
		if (user != null) {

			// Preenche o Bean do JSF para a interface usar
			usuarioLogadoBean.setUsuario(user);

			// A MÁGICA PRO FILTRO: Pega a Sessão do Java e joga o usuário lá dentro
			HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
			session.setAttribute("usuario", user);

			// Redireciona para a página inicial protegida
			return "/views/comum/Inicio.xhtml?faces-redirect=true";

		} else {
			// Se a senha estiver errada ou usuário não existir
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "CPF ou senha incorretos!", null));
			return null; // Fica na mesma página de login
		}
	}

	public String sair() {
		// Destrói a sessão inteira e desloga o usuário (usado no botão Sair da Sidebar)
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		return "/login.xhtml?faces-redirect=true";
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}
}