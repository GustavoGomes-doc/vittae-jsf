package com.vittae.view;

import java.io.IOException;
import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Named("logoutBean")
@RequestScoped
public class LogoutBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public void sair() {
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
		HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
		
		HttpSession session = request.getSession(false);
		if (session != null) session.invalidate();
		
		try {
			response.sendRedirect(request.getContextPath() + "/views/login/login.xhtml");
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		fc.responseComplete();    
	}
}