package com.vittae.util.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginFilter extends AbstractFilter implements Filter {
	
	public void init(FilterConfig arg0) throws ServletException {
		System.out.println("LOGIN FILTER INICIADO!");
	}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req  = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String path = req.getServletPath();

		// Páginas públicas
		List<String> publicPaths = Arrays.asList(
			"/views/login/login.xhtml",
			"/views/cadastrar/cadastrar.xhtml"
		);
		if (publicPaths.contains(path)) {
			chain.doFilter(request, response);
			return;
		}

		HttpSession session = req.getSession(false);
		String user = (session != null) ? (String) session.getAttribute("usuario") : null;
		System.out.println("FILTER - path: " + path + " | session: " + session + " | user: " + user);

		// Não logado
		if (user == null) {
			res.sendRedirect(req.getContextPath() + "/views/login/login.xhtml");
			return;
		}

		String perfil = (String) session.getAttribute("perfil");
		System.out.println("FILTER - path: " + path + " | user: " + user + " | perfil: " + perfil);
		

		// Páginas só de ADMIN
		if (path.startsWith("/views/admin/") && !"ADMIN".equals(perfil)) {
			res.sendRedirect(req.getContextPath() + "/views/login/login.xhtml");
			return;
		}

		// Páginas só de PACIENTE
		if (path.startsWith("/views/pacientes/") && !"PACIENTE".equals(perfil)) {
			res.sendRedirect(req.getContextPath() + "/views/login/login.xhtml");
			return;
		}

		// Páginas só de MEDICO
		if (path.startsWith("/views/medicos/") && !"MEDICO".equals(perfil)) {
			res.sendRedirect(req.getContextPath() + "/views/login/login.xhtml");
			return;
		}

		chain.doFilter(request, response);
	}

	public void destroy() {}
}