package com.vittae.view;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
<<<<<<< Updated upstream
import javax.inject.Named;

import com.vittae.model.dao.UsuarioDao;
=======
import javax.faces.context.FacesContext;
import javax.inject.Named;

>>>>>>> Stashed changes
import com.vittae.model.Usuario;
import com.vittae.model.Paciente;
import com.vittae.model.Medico;

@Named("usuarioLogadoBean")
@SessionScoped
public class UsuarioLogadoBean implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Usuario usuario; // Única fonte da verdade!

	public String getNome() {
		return (usuario != null) ? usuario.getNome() : "";
	}

<<<<<<< Updated upstream
	// Agora descobrimos o perfil pela Classe do objeto!
=======
>>>>>>> Stashed changes
	public String getPerfil() {
		if (usuario == null) return "";
		
		if (usuario instanceof Medico) {
			return "MEDICO";
		} else if (usuario instanceof Paciente) {
			return "PACIENTE";
		} 
<<<<<<< Updated upstream
        // else if (usuario instanceof Admin) { return "ADMIN"; }
		
		// 🌟 Falha Segura: Se não for nenhum dos tipos conhecidos, retorna vazio.
        // Assim, nenhuma tag 'rendered' do JSF vai liberar acessos indevidos.
		throw new IllegalStateException("Tentativa de acesso com perfil de usuário desconhecido!");
=======
		
		// Conforme seu diagrama UML: se não for Médico nem Paciente, é Admin!
		if (usuario.getClass().equals(Usuario.class)) {
			return "ADMIN";
		}
		
		// 🌟 Falha Segura para o JSF não quebrar a tela inteira
		return "DESCONHECIDO"; 
>>>>>>> Stashed changes
	}
	
	public boolean isAdmin() {
		return "ADMIN".equals(getPerfil());
	}

	public boolean isPaciente() {
		return "PACIENTE".equals(getPerfil());
	}

	public boolean isMedico() {
		return "MEDICO".equals(getPerfil());
	}
	
<<<<<<< Updated upstream
=======
	// Método de Logout para usar nos botões de "Sair"
	public String logout() {
		FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
		this.usuario = null;
		
		// Redireciona para a tela de login (ajuste o caminho se necessário)
		return "/views/login/login.xhtml?faces-redirect=true"; 
	}
	
>>>>>>> Stashed changes
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}