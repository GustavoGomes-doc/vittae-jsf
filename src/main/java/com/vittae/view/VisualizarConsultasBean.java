package com.vittae.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import com.vittae.model.Consulta;
import com.vittae.service.AgendarConsultaService;

@Named("visualizarConsultasBean")
@ViewScoped
public class VisualizarConsultasBean implements Serializable {

    private List<Consulta> consultas;
    private Consulta consultaSelecionada;

    // Service que faz as chamadas HTTP pro Back
    private AgendarConsultaService service = new AgendarConsultaService();

    @PostConstruct
    public void init() {
        carregarConsultas();
    }

    // ─── CARREGAR LISTA DO BANCO ─────────────────────────
    public void carregarConsultas() {
        try {
            consultas = service.listarTodas();
        } catch (Exception e) {
            consultas = new ArrayList<>();
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erro ao carregar consultas: " + e.getMessage(), null));
        }
    }

    // ─── DETALHAR ────────────────────────────────────────
    // Clica na mesma = fecha, clica em outra = abre
    public void detalhar(Consulta c) {
        if (consultaSelecionada != null && consultaSelecionada.getId().equals(c.getId())) {
            consultaSelecionada = null;
        } else {
            consultaSelecionada = c;
        }
    }

    // ─── CANCELAR ────────────────────────────────────────
    public void cancelar(Consulta c) {
        try {
            service.cancelar(c.getId(), c);
            carregarConsultas(); // recarrega a lista do banco
            consultaSelecionada = null;

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Consulta cancelada com sucesso!", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erro ao cancelar: " + e.getMessage(), null));
        }
    }

    // ─── GETTERS E SETTERS ───────────────────────────────
    public List<Consulta> getConsultas() { return consultas; }
    public void setConsultas(List<Consulta> consultas) { this.consultas = consultas; }

    public Consulta getConsultaSelecionada() { return consultaSelecionada; }
    public void setConsultaSelecionada(Consulta c) { this.consultaSelecionada = c; }
}

