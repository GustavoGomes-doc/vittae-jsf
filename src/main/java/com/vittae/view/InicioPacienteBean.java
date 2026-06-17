package com.vittae.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.vittae.dto.ConsultaDTO;
import com.vittae.model.Usuario;
import com.vittae.model.enums.Status;
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
    private ConsultaDTO proximaConsulta;
    private ConsultaDTO ultimaConsulta;
    private Usuario pacienteLogado;
    private String dicaDoDia;

    @PostConstruct
    public void inicializar() {
        log.info("Inicializando Dashboard do Paciente...");
        this.dataAtual = new Date();
        this.pacienteLogado = loginBean.getUsuarioLogado();
        sortearDicaDoDia();

        if (this.pacienteLogado != null) {
            log.info("Carregando inicio para o paciente: " + this.pacienteLogado.getNome());
            try {
                List<ConsultaDTO> consultas = consultaService.listarTodas();

                LocalDate hoje = LocalDate.now();

                // Próxima consulta: status PENDENTE com data >= hoje, a mais próxima
                this.proximaConsulta = consultas.stream()
                    .filter(c -> Status.PENDENTE == c.getStatus())
                    .filter(c -> c.getDataConsulta() != null && !c.getDataConsulta().isBefore(hoje))
                    .min(Comparator.comparing(ConsultaDTO::getDataConsulta)
                        .thenComparing(ConsultaDTO::getHora))
                    .orElse(null);

                // Última consulta: status REALIZADA com data < hoje, a mais recente
                this.ultimaConsulta = consultas.stream()
                    .filter(c -> Status.REALIZADA == c.getStatus())
                    .filter(c -> c.getDataConsulta() != null && c.getDataConsulta().isBefore(hoje))
                    .max(Comparator.comparing(ConsultaDTO::getDataConsulta)
                        .thenComparing(ConsultaDTO::getHora))
                    .orElse(null);

                log.info("Próxima consulta: " + (proximaConsulta != null ? proximaConsulta.getDataConsulta() : "nenhuma"));
                log.info("Última consulta: "  + (ultimaConsulta  != null ? ultimaConsulta.getDataConsulta()  : "nenhuma"));

            } catch (Exception e) {
                log.error("Erro ao buscar consultas do paciente: " + e.getMessage());
            }
        } else {
            log.warn("Nenhum usuário na sessão ao tentar acessar o início.");
        }
    }

    public String agendarRetorno(ConsultaDTO consultaAntiga) {
        if (consultaAntiga != null && consultaAntiga.getMedicoId() != null) {
            log.info("Agendando retorno. Pré-selecionando o médico ID: " + consultaAntiga.getMedicoId());
            return "agendarConsulta?faces-redirect=true&medicoId=" + consultaAntiga.getMedicoId();
        }
        return "agendarConsulta?faces-redirect=true";
    }

    public String prepararRemarcacao() {
        log.info("Redirecionando para reagendamento...");
        return "/views/pacientes/agendarConsulta.xhtml?faces-redirect=true";
    }

    public void cancelarConsulta() {
        if (this.proximaConsulta != null) {
            try {
                consultaService.cancelar(this.proximaConsulta.getId(), this.proximaConsulta);
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

    private void sortearDicaDoDia() {
        String[] dicas = {
            "Beba pelo menos 2 litros de água por dia para manter o corpo hidratado.",
            "Dormir 8 horas por noite fortalece seu sistema imunológico.",
            "Pratique 30 minutos de exercícios físicos diariamente. Seu coração agradece!",
            "Evite telas (celular e TV) 1 hora antes de dormir para um sono mais reparador.",
            "Não pule o café da manhã! Ele te dá a energia necessária para começar o dia.",
            "Faça alongamentos rápidos se você trabalha muito tempo sentado."
        };
        this.dicaDoDia = dicas[new Random().nextInt(dicas.length)];
    }
}