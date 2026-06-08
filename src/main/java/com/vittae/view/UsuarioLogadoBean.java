package com.vittae.view;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.vittae.model.Usuario;

@Named("usuarioLogadoBean")
@RequestScoped
public class UsuarioLogadoBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String getUsuarioDaSessao() {
	    FacesContext context = FacesContext.getCurrentInstance();
	    HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
	    HttpSession session = request.getSession(false);
	    if (session == null) return null;

	    return (String) session.getAttribute("usuario"); // era (Usuario), agora é (String)
	}

	public String getNome() {
	    String nome = getUsuarioDaSessao();
	    return nome != null ? nome : "";
	}
	
	public String getPerfil () {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
						.getExternalContext().getRequest();
		HttpSession session = request.getSession(false);
		if (session == null) return "";
		Object perfil = session.getAttribute("perfil");
		return perfil != null ? perfil.toString() : "";
		
	}
	
}