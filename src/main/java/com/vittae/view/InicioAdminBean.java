package com.vittae.view;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.vittae.model.Consulta;
import com.vittae.model.Usuario;
import com.vittae.model.enums.Perfil;
import com.vittae.model.enums.Status;
import com.vittae.service.ConsultaService;
import com.vittae.service.UsuarioService;

import lombok.Getter;
import lombok.extern.log4j.Log4j;

@Log4j
@Getter
@Named("inicioAdminBean")
@RequestScoped
public class InicioAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;

    @Inject
    private ConsultaService consultaService;

    private long totalMedicos;
    private long totalPacientes;
    private long consultasHoje;
    private long consultasPendentes;
    private long consultasConfirmadas;

    private List<Usuario> ultimosMedicos = new ArrayList<>();
    private List<Consulta> consultasRecentes = new ArrayList<>();

    @PostConstruct
    public void inicializar() {
        log.info("Inicializando dashboard admin...");

        this.totalMedicos = usuarioService.contarPorPerfil(Perfil.MEDICO);
        this.totalPacientes = usuarioService.contarPorPerfil(Perfil.PACIENTE);
        this.ultimosMedicos = usuarioService.buscarUltimosMedicos(5);

        carregarConsultas();
    }

    private void carregarConsultas() {
        try {
            List<Consulta> consultas = consultaService.listarTodas();

            this.consultasHoje = consultas.stream()
                    .filter(c -> LocalDate.now().equals(c.getDataConsulta()))
                    .count();

            this.consultasPendentes = consultas.stream()
                    .filter(c -> Status.PENDENTE.equals(c.getStatus()))
                    .count();

            this.consultasConfirmadas = consultas.stream()
                    .filter(c -> Status.REALIZADA.equals(c.getStatus()))
                    .count();

            this.consultasRecentes = consultas.stream()
                    .sorted(Comparator.comparing(
                            Consulta::getDataConsulta,
                            Comparator.nullsLast(Comparator.reverseOrder())
                    ))
                    .limit(5)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Erro ao carregar consultas do dashboard admin: " + e.getMessage());
            this.consultasHoje = 0;
            this.consultasPendentes = 0;
            this.consultasConfirmadas = 0;
            this.consultasRecentes = new ArrayList<>();
        }
    }
}