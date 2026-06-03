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

        HttpServletRequest req  = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // CORRIGIDO #1 — getServletPath() não inclui path parameters
        String path = req.getServletPath();

        // CORRIGIDO #2 — lista branca com paths exactos em vez de contains()
        List<String> publicPaths = Arrays.asList(
            "/views/login/login.xhtml",
            "/views/cadastrar/cadastrar.xhtml"
        );

        if (publicPaths.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // CORRIGIDO #3 — getSession(false) não cria sessão nova (evita Session Fixation)
        HttpSession session = req.getSession(false);
        Usuario user = (session != null)
                ? (Usuario) session.getAttribute("usuario")
                : null;

        if (user == null) {
            res.sendRedirect(req.getContextPath() + "/views/login/login.xhtml");
        } else {
            chain.doFilter(request, response);
        }
    }

    public void destroy() {}
}