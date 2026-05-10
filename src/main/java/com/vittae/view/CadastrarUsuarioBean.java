package com.vittae.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.vittae.model.Usuario;
import com.vittae.model.enums.Perfil;
import com.vittae.service.UsuarioService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Getter
@Setter
@Named
@ViewScoped
public class CadastrarUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;

    // AQUI MUDOU: Agora usamos o seu LoginBean oficial
    @Inject
    private LoginBean loginBean;

    private Usuario usuario = new Usuario();
    private List<Usuario> usuarios = new ArrayList<>();
    
    private List<Perfil> perfis; 

    @PostConstruct
    public void inicializar() {
        log.info("Inicializando tela de pesquisa de usuários...");
        
        this.usuarios = usuarioService.buscarTodos();

        // AQUI MUDOU: Pegando do LoginBean
        Usuario logado = loginBean.getUsuarioLogado();

        if (logado != null && "PACIENTE".equals(logado.getPerfil())) {
            this.perfis = Arrays.asList(Perfil.PACIENTE);
        } else {
            this.perfis = Arrays.asList(Perfil.values());
        }
    }
    
    public void salvar() {
        log.info("Tentando salvar usuário: " + usuario.toString());
        
        usuario = usuarioService.salvar(usuario);
        this.setUsuarios(usuarioService.buscarTodos());
        
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "O usuário foi gravado com sucesso!", 
                usuario.getNome()));
                
        limpar();
    }   
    
    public void excluir() {
        try {
            log.info("Tentando excluir usuário: " + usuario.getNome());
            
            usuarioService.excluir(usuario);
            this.usuarios = usuarioService.buscarTodos();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Usuário " + usuario.getNome() + " excluído com sucesso.", null));
                            
        } catch (Exception e) {
            log.error("Erro ao excluir: ", e);
            FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ocorreu um problema ao excluir", null));
        }
    }
    
    public void limpar() {
        this.usuario = new Usuario();
    }
}