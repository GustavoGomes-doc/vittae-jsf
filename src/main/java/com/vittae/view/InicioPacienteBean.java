package com.vittae.view;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

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
@Named("inicioPacienteBean")
@RequestScoped
public class InicioPacienteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private ConsultaService consultaService;

    @Inject
    private LoginBean loginBean; 

    private Date dataAtual;
    private Consulta proximaConsulta;
    private Usuario pacienteLogado;
    
    // Novas variáveis para os cards
    private Consulta ultimaConsulta;
    private String dicaDoDia;

    @PostConstruct
    public void inicializar() {
        log.info("Inicializando Dashboard do Paciente...");
        this.dataAtual = new Date(); 
        this.pacienteLogado = loginBean.getUsuarioLogado();

        sortearDicaDoDia(); // Chama o método que embaralha e escolhe a dica

        if (this.pacienteLogado != null) {
            log.info("Carregando inicio para o paciente: " + this.pacienteLogado.getNome());
            
            try {
                // Aqui você vai buscar as consultas usando os métodos do seu Service
                // this.proximaConsulta = consultaService.buscarProximaPorPacienteId(this.pacienteLogado.getId());
                // this.ultimaConsulta = consultaService.buscarUltimaPorPacienteId(this.pacienteLogado.getId());
                
            } catch (Exception e) {
                log.error("Erro ao buscar dados do paciente: " + e.getMessage());
            }
            
        } else {
            log.warn("Nenhum usuário na sessão ao tentar acessar o início.");
        }
    }

    // --- MÉTODOS DOS CARDS NOVOS ---
    
    public String agendarRetorno(Consulta consultaAntiga) {
        if (consultaAntiga != null && consultaAntiga.getMedico() != null) {
            // Pega o código (ID) do médico da consulta antiga para pré-selecionar na próxima tela
            Long idMedico = consultaAntiga.getMedico().getId();
            log.info("Agendando retorno. Pré-selecionando o médico ID: " + idMedico);
            
            return "agendarConsulta?faces-redirect=true&medicoId=" + idMedico;
        }
        return "agendarConsulta?faces-redirect=true";
    }

    private void sortearDicaDoDia() {
        String[] dicas = {
            "Beba pelo menos 2 litros de água por dia para manter o corpo hidratado.",
            "Dormir 8 horas por noite fortalece seu sistema imunológico.",
            "Pratique 30 minutos de exercícios físicos diariamente. Seu coração agradece!",
            "Evite telas (celular e TV) 1 hora antes de dormir para um sono mais reparador.",
            "Não pule o café da manhã! Ele te dá a energia necessária para começar o dia.",
            "Faça alongamentos rápidos se você trabalha muito tempo sentado."
        };
        int indiceSorteado = new Random().nextInt(dicas.length);
        this.dicaDoDia = dicas[indiceSorteado];
    }

    // --- MÉTODOS DOS CARDS ANTIGOS ---

    public String prepararRemarcacao() {
        log.info("Redirecionando para reagendamento...");
        return "/views/pacientes/agendarConsulta.xhtml?faces-redirect=true";
    }

    public void cancelarConsulta() {
        if (this.proximaConsulta != null) {
            try {
                // consultaService.cancelar(this.proximaConsulta.getId());
                log.info("Consulta cancelada com sucesso.");
                this.proximaConsulta = null; 
            } catch (Exception e) {
                log.error("Erro ao cancelar consulta: " + e.getMessage());
            }
        }
    }
    
    public String getPrimeiroNome() {
        if (pacienteLogado != null && pacienteLogado.getNome() != null && !pacienteLogado.getNome().trim().isEmpty()) {
            return pacienteLogado.getNome().split(" ")[0];
        }
        return "Paciente";
    }
}