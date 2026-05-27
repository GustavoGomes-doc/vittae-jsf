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

@WebFilter(urlPatterns = "/views/*", servletNames = "{Faces Servlet}")
public class LoginFilter extends AbstractFilter implements Filter {
	
	public void init(FilterConfig arg0) throws ServletException {}
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		HttpSession session = req.getSession();
		
		// 1. Verifica qual página o usuário está tentando acessar
		String reqURI = req.getRequestURI();
		
		// 2. SE FOR A TELA DE LOGIN (que também tem o cadastro), DEIXA PASSAR!
		if (reqURI.contains("/login.xhtml") || reqURI.contains("/cadastrar.xhtml")) {
			chain.doFilter(request, response);
			return; 
		}
		
		// 3. Se for qualquer outra página (como o painel de médicos), verifica a sessão
		Usuario user = (Usuario) session.getAttribute("usuario");
		
		if (session.isNew() || user == null) {
			dologin(request, response, req);
		} else {
			chain.doFilter(request, response);	
		} 
	}
	
	public void destroy() {}	
		
}