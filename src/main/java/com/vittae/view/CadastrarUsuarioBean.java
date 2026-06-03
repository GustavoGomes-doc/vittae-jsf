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
import lombok.extern.slf4j.Slf4j; // CORRIGIDO — SLF4J em vez de Log4j 1.x

@Slf4j   // CORRIGIDO — era @Log4j (EOL com CVEs)
@Getter
@Setter
@Named
@ViewScoped
public class CadastrarUsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private LoginBean loginBean;

    private Usuario usuario = new Usuario();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Perfil> perfis;

    @PostConstruct
    public void inicializar() {
        log.info("Inicializando tela de pesquisa de utilizadores...");

        Usuario logado = loginBean.getUsuarioLogado();

        // CORRIGIDO — só admin lista todos os utilizadores
        if (Perfil.ADMIN.equals(logado.getPerfil())) {
            this.usuarios = usuarioService.buscarTodos();
        } else {
            this.usuarios = new ArrayList<>();
        }

        // CORRIGIDO — comparar Enum com Enum (era String vs Enum → sempre false)
        if (logado != null && Perfil.PACIENTE.equals(logado.getPerfil())) {
            this.perfis = Arrays.asList(Perfil.PACIENTE);
        } else {
            this.perfis = Arrays.asList(Perfil.values());
        }
    }

    public void salvar() {
        // CORRIGIDO — só logar dados não sensíveis (era usuario.toString() → expunha senha)
        log.info("Tentando salvar utilizador: cpf=" + usuario.getCpf()
               + ", nome=" + usuario.getNome());

        Usuario logado = loginBean.getUsuarioLogado();

        // CORRIGIDO — re-validar perfil no servidor
        if (Perfil.PACIENTE.equals(logado.getPerfil())
                && !Perfil.PACIENTE.equals(usuario.getPerfil())) {
            addMensagem(FacesMessage.SEVERITY_ERROR, "Operação não permitida.", null);
            return;
        }

        // CORRIGIDO — construir objecto no servidor (evita Mass Assignment via id manipulado)
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(usuario.getNome());
        novoUsuario.setEmail(usuario.getEmail());
        novoUsuario.setCpf(usuario.getCpf());
        novoUsuario.setSenha(usuario.getSenha());

        if (Perfil.ADMIN.equals(logado.getPerfil())) {
            novoUsuario.setPerfil(usuario.getPerfil());
        } else {
            novoUsuario.setPerfil(Perfil.PACIENTE);
        }

        novoUsuario = usuarioService.salvar(novoUsuario);
        this.setUsuarios(usuarioService.buscarTodos());

        addMensagem(FacesMessage.SEVERITY_INFO,
            "Utilizador gravado com sucesso!", novoUsuario.getNome());
        limpar();
    }

    public void excluir() {
        Usuario logado = loginBean.getUsuarioLogado();

        // CORRIGIDO — verificar perfil antes de excluir
        if (!Perfil.ADMIN.equals(logado.getPerfil())) {
            addMensagem(FacesMessage.SEVERITY_ERROR, "Acesso negado.", null);
            return;
        }

        try {
            log.info("Excluindo utilizador: nome=" + usuario.getNome()
                   + " por admin cpf=" + logado.getCpf());

            usuarioService.excluir(usuario);
            this.usuarios = usuarioService.buscarTodos();

            addMensagem(FacesMessage.SEVERITY_INFO,
                "Utilizador " + usuario.getNome() + " excluído com sucesso.", null);

        } catch (Exception e) {
            // CORRIGIDO — mensagem genérica ao utilizador, detalhe no log
            log.error("Erro ao excluir utilizador id=" + usuario.getId(), e);
            addMensagem(FacesMessage.SEVERITY_ERROR,
                "Não foi possível excluir o utilizador.", null);
        }
    }

    public void limpar() {
        this.usuario = new Usuario();
    }

    private void addMensagem(FacesMessage.Severity severidade, String resumo, String detalhe) {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage(severidade, resumo, detalhe));
    }
}