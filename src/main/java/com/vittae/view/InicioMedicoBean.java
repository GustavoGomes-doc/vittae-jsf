package com.vittae.view;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.vittae.model.Consulta;
import com.vittae.model.Usuario;
import com.vittae.service.ConsultaService;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Getter
@Setter
@Named("inicioMedicoBean")
@RequestScoped
public class InicioMedicoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ConsultaService consultaService;

    @Inject
    private LoginBean loginBean; 

    private Date dataAtual;
    private Usuario medicoLogado;
    
    // Agora é uma lista, pois o médico atende várias pessoas no dia
    private List<Consulta> consultasDeHoje; 

    @PostConstruct
    public void inicializar() {
        this.dataAtual = new Date(); 
        this.medicoLogado = loginBean.getUsuarioLogado();

        if (this.medicoLogado != null) {
            try {
                // Aqui você deve ter um método no seu service que busca a agenda do dia do médico
                // this.consultasDeHoje = consultaService.buscarConsultasDoDiaPorMedico(this.medicoLogado.getId(), new Date());
            } catch (Exception e) {
                log.error("Erro ao buscar agenda do médico: " + e.getMessage());
            }
        }
    }

    // Ação de clicar no botão "Atender"
    public String iniciarAtendimento(Consulta consultaSelecionada) {
        // Guarda a consulta selecionada em algum lugar (Sessão ou Flash) e redireciona para a tela de prontuário
        log.info("Iniciando atendimento do paciente: " + consultaSelecionada.getPaciente().getNome());
        return "/views/medico/prontuario.xhtml?faces-redirect=true";
    }
    
    // Retorna a quantidade de consultas para exibir no "Card de Métrica"
    public int getTotalConsultasHoje() {
        return (consultasDeHoje != null) ? consultasDeHoje.size() : 0;
    }

    public String getPrimeiroNome() {
        if (medicoLogado != null && medicoLogado.getNome() != null && !medicoLogado.getNome().trim().isEmpty()) {
            return medicoLogado.getNome().split(" ")[0];
        }
        return "Médico";
    }
}