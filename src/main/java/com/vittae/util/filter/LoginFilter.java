package com.vittae.util.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.vittae.model.Usuario;

@WebFilter(urlPatterns = "/restrito/*", servletNames = "{Faces Servlet}")
public class LoginFilter extends AbstractFilter implements Filter {

	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession();

		// 1. Pega a URL da requisição
		String requestURI = req.getRequestURI();

		// 2. REGRA VIP: Libera tudo que for visual (CSS, JS, imagens)
		if (requestURI.contains("/javax.faces.resources/")) {
			chain.doFilter(request, response);
			return; // Para a execução do filtro aqui e não exige login
		}

		// 3. Regra normal para as outras telas do sistema
		Usuario user = (Usuario) session.getAttribute("usuario");

		if (session.isNew() || user == null) {
			doLogin(request, response, req);
		} else {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
	}
}