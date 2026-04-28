<<<<<<< Updated upstream
package com.vittae.view; 

import com.vittae.model.Usuario;
import com.vittae.model.dao.UsuarioDao;
import java.io.Serializable;
=======
package com.vittae.view;

import com.vittae.model.Usuario;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
>>>>>>> Stashed changes
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
<<<<<<< Updated upstream
=======
import com.fasterxml.jackson.databind.ObjectMapper; 
>>>>>>> Stashed changes

@Named("loginBean")
@RequestScoped
public class LoginBean implements Serializable {

	private String cpf;
	private String senha;

	@Inject
	private UsuarioLogadoBean usuarioLogadoBean;

<<<<<<< Updated upstream
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

=======
	public String entrar() {
		// 1. Limpa a máscara do CPF
		String cpfLimpo = this.cpf != null ? this.cpf.replaceAll("\\D", "") : "";

		// 2. Monta o corpo da requisição em JSON (o que vai ser enviado para o Spring)
		String jsonBody = String.format("{\"cpf\":\"%s\", \"senha\":\"%s\"}", cpfLimpo, senha);

		try {
			// 3. Prepara o "carteiro" (HttpClient) e a "carta" (HttpRequest)
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					// ALERTA: Troque esta URL para o endereço real do seu Spring Boot!
					.uri(URI.create("http://localhost:8080/api/auth/login")) 
					.header("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
					.build();

			// 4. Dispara a requisição e espera a resposta
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			// 5. Analisa a resposta da API (Status 200 significa OK)
			if (response.statusCode() == 200) {
			
				ObjectMapper mapper = new ObjectMapper();
				Usuario user = mapper.readValue(response.body(), Usuario.class);

				// Preenche o Bean da sessão
				usuarioLogadoBean.setUsuario(user);

				// Joga na Sessão do Java (Para o seu Filtro liberar o acesso)
				HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
				session.setAttribute("usuario", user);

				return "/views/comum/Inicio.xhtml?faces-redirect=true";

			} else {
				// Se a API retornou 401 (Unauthorized), 403 (Forbidden) ou 404
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_ERROR, "CPF ou senha incorretos!", null));
				return null; 
			}

		} catch (Exception e) {
			// Se o servidor do Spring estiver fora do ar ou der erro de conexão
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_FATAL, "Erro de comunicação com o servidor!", null));
			return null;
		}
	}

	// === GETTERS E SETTERS ===
>>>>>>> Stashed changes
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