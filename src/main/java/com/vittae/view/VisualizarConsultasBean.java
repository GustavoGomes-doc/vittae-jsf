package com.vittae.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.vittae.model.Consulta;
import com.vittae.service.ConsultaService;

@Named("visualizarConsultasBean")
@ViewScoped
public class VisualizarConsultasBean implements Serializable {

    private List<Consulta> consultas;
    private Consulta consultaSelecionada;
    private Consulta consultaParaCancelar;
    private String filtroTexto;
    private String filtroStatus;
    private boolean exibirModalCancelamento = false;

    private ConsultaService service = new ConsultaService();

    @PostConstruct
    public void init() {
        filtroStatus = "todas";
        carregarConsultas();
    }

    // ─── CARREGAR ────────────────────────────────────────
    public void carregarConsultas() {
        try {
            consultas = service.listarTodas();
        } catch (Exception e) {
            consultas = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erro ao carregar consultas: " + e.getMessage(), null));
        }
    }

    // ─── FILTROS ─────────────────────────────────────────
    public List<Consulta> getConsultasFiltradas() {
        if (consultas == null) return new ArrayList<>();
        return consultas.stream()
                .filter(this::passaFiltroStatus)
                .filter(this::passaFiltroTexto)
                .collect(Collectors.toList());
    }

    private boolean passaFiltroStatus(Consulta c) {
        if (filtroStatus == null || filtroStatus.equalsIgnoreCase("todas")) return true;
        return c.getStatus() != null &&
               c.getStatus().toString().equalsIgnoreCase(filtroStatus);
    }

    private boolean passaFiltroTexto(Consulta c) {
        if (filtroTexto == null || filtroTexto.trim().isEmpty()) return true;
        String texto = filtroTexto.trim().toLowerCase();

        boolean nomeOk = c.getMedico() != null
                && c.getMedico().getNome() != null
                && c.getMedico().getNome().toLowerCase().contains(texto);

        boolean especOk = false;
        if (c.getMedico() != null && c.getMedico().getEspecialidades() != null) {
            especOk = c.getMedico().getEspecialidades().stream()
                    .filter(e -> e != null)
                    .anyMatch(e -> e.toLowerCase().contains(texto));
        }
        return nomeOk || especOk;
    }

    public void filtrar() {}

    // ─── DETALHAR ────────────────────────────────────────
    public void detalhar(Consulta c) {
        if (consultaSelecionada != null && consultaSelecionada.getId().equals(c.getId())) {
            consultaSelecionada = null;
        } else {
            consultaSelecionada = c;
        }
    }

    // ─── CANCELAR COM MODAL ───────────────────────────────
    public void prepararCancelamento(Consulta c) {
        this.consultaParaCancelar = c;
        this.exibirModalCancelamento = true;
    }

    public void confirmarCancelamento() {
        if (consultaParaCancelar != null) {
            cancelar(consultaParaCancelar);
        }
        fecharModal();
    }

    public void fecharModal() {
        this.exibirModalCancelamento = false;
        this.consultaParaCancelar = null;
    }

    public void cancelar(Consulta c) {
        try {
            service.cancelar(c.getId(), c);
            carregarConsultas();
            consultaSelecionada = null;
            addInfo("Consulta cancelada com sucesso!");
        } catch (Exception e) {
            addErro("Erro ao cancelar: " + e.getMessage());
        }
    }

    // ─── REMARCAR ────────────────────────────────────────
    public void prepararRemarcar(Consulta c) {
        this.consultaSelecionada = c;
        addInfo("Funcionalidade de remarcação em breve!");
    }

    // ─── HELPERS ─────────────────────────────────────────
    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ─── GETTERS E SETTERS ────────────────────────────────
    public List<Consulta> getConsultas() { return consultas; }
    public void setConsultas(List<Consulta> consultas) { this.consultas = consultas; }

    public Consulta getConsultaSelecionada() { return consultaSelecionada; }
    public void setConsultaSelecionada(Consulta c) { this.consultaSelecionada = c; }

    public Consulta getConsultaParaCancelar() { return consultaParaCancelar; }
    public void setConsultaParaCancelar(Consulta c) { this.consultaParaCancelar = c; }

    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }

    public String getFiltroStatus() { return filtroStatus; }
    public void setFiltroStatus(String filtroStatus) { this.filtroStatus = filtroStatus; }

    public boolean isExibirModalCancelamento() { return exibirModalCancelamento; }
    public void setExibirModalCancelamento(boolean exibirModalCancelamento) {
        this.exibirModalCancelamento = exibirModalCancelamento;
    }
}