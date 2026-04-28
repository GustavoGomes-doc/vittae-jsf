package com.vittae.util.filter;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFilter {
	 
	public void dologin(ServletRequest request, ServletResponse response, HttpServletRequest req)
			throws IOException, ServletException {
		
		RequestDispatcher dispatcher = req.getRequestDispatcher("/views/login/login.xhtml");
		dispatcher.forward(request, response);
	}
	public void acessoNegado(ServletRequest request,  ServletResponse response, HttpServletRequest req)
			throws IOException, ServletException {
		
		RequestDispatcher dispatcher = req.getRequestDispatcher("acessoNegado");
		dispatcher.forward(request, response);
	}
}