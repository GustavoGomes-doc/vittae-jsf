package com.vittae.util.filter;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public abstract class AbstractFilter {

	public void doLogin(ServletRequest request, ServletResponse response, HttpServletRequest req)
			throws IOException, ServletException {

		RequestDispatcher dispatcher = req.getRequestDispatcher("/vittae/Login.xhtml");
		dispatcher.forward(request, response);
	}

	public void acessoNegado(ServletRequest request, ServletResponse response, HttpServletRequest req)
			throws IOException, ServletException {

		RequestDispatcher dispatcher = req.getRequestDispatcher("acessoNegado");
		dispatcher.forward(request, response);
	}

}