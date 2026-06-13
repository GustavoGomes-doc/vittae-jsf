package com.vittae.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.vittae.dto.ConsultaDTO;
import com.vittae.service.ConsultaService;

@Named("visualizarConsultasBean")
@ViewScoped
public class VisualizarConsultasBean implements Serializable {

    private List<ConsultaDTO> consultas;
    private ConsultaDTO consultaSelecionada;
    private ConsultaDTO consultaParaCancelar;
    private ConsultaDTO consultaParaRemarcar;
    private String filtroTexto;
    private String filtroStatus;
    private boolean exibirModalCancelamento = false;
    private boolean exibirModalRemarcacao = false;
    private Long idConsultaSelecionada;

    private ConsultaService service = new ConsultaService();

    @PostConstruct
    public void init() {
        filtroStatus = "todas";
        carregarConsultas();
    }

    public void carregarConsultas() {
        try {
            consultas = service.listarTodas();
        } catch (Exception e) {
            consultas = new ArrayList<>();
            e.printStackTrace();
            String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao carregar consultas: " + msg, null));
        }
    }

    // ─── FILTROS ─────────────────────────────────────────
    public List<ConsultaDTO> getConsultasFiltradas() {
        if (consultas == null)
            return new ArrayList<>();
        return consultas.stream()
                .filter(this::passaFiltroStatus)
                .filter(this::passaFiltroTexto)
                .collect(Collectors.toList());
    }

    private boolean passaFiltroStatus(ConsultaDTO c) {
        if (filtroStatus == null || filtroStatus.equalsIgnoreCase("todas"))
            return true;
        return c.getStatus() != null && c.getStatus().toString().equalsIgnoreCase(filtroStatus);
    }

    private boolean passaFiltroTexto(ConsultaDTO c) {
        if (filtroTexto == null || filtroTexto.trim().isEmpty())
            return true;
        String texto = filtroTexto.trim().toLowerCase();

        boolean nomeOk = c.getNomeMedico() != null && c.getNomeMedico().toLowerCase().contains(texto);
        boolean especOk = c.getEspecialidade() != null && c.getEspecialidade().toLowerCase().contains(texto);

        return nomeOk || especOk;
    }

    public void filtrar() {
    }

    // ─── DETALHAR ────────────────────────────────────────
    public void detalhar(ConsultaDTO c) {
        if (consultaSelecionada != null && consultaSelecionada.getId().equals(c.getId())) {
            consultaSelecionada = null;
        } else {
            consultaSelecionada = c;
        }
    }

    // ─── CANCELAR COM MODAL ───────────────────────────────
    public void prepararCancelamento(ConsultaDTO c) {
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

    public void cancelar(ConsultaDTO c) {
        try {
            service.cancelar(c.getId(), c);
            carregarConsultas();
            addInfo("Consulta cancelada com sucesso!");
        } catch (Exception e) {
            addErro("Erro ao cancelar: " + e.getMessage());
        }
    }

    // ─── REMARCAR COM MODAL ───────────────────────────────
    public void prepararRemarcar(ConsultaDTO c) {
        this.consultaParaRemarcar = c;
        this.exibirModalRemarcacao = true;
    }

    public void confirmarRemarcacao() {
        if (consultaParaRemarcar != null) {
            try {
                service.remarcar(consultaParaRemarcar.getId(), consultaParaRemarcar);
                carregarConsultas();
                consultaSelecionada = null;
                addInfo("Consulta remarcada com sucesso!");
            } catch (Exception e) {
                addErro("Erro ao remarcar: " + e.getMessage());
            }
        }
        fecharModalRemarcacao();
    }

    public void fecharModalRemarcacao() {
        this.exibirModalRemarcacao = false;
        this.consultaParaRemarcar = null;
    }

    // ─── ACTIONS POR ID ───────────────────────────────────
    public void prepararCancelamentoAction(Long id) {
        for (ConsultaDTO c : consultas) {
            if (c.getId().equals(id)) {
                prepararCancelamento(c);
                break;
            }
        }
    }

    public void prepararRemarcarAction(Long id) {
        for (ConsultaDTO c : consultas) {
            if (c.getId().equals(id)) {
                prepararRemarcar(c);
                break;
            }
        }
    }

    // ─── HELPERS ─────────────────────────────────────────
    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addErro(String msg) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // ─── GETTERS E SETTERS ────────────────────────────────
    public List<ConsultaDTO> getConsultas() {
        return consultas;
    }

    public void setConsultas(List<ConsultaDTO> consultas) {
        this.consultas = consultas;
    }

    public ConsultaDTO getConsultaSelecionada() {
        return consultaSelecionada;
    }

    public void setConsultaSelecionada(ConsultaDTO c) {
        this.consultaSelecionada = c;
    }

    public ConsultaDTO getConsultaParaCancelar() {
        return consultaParaCancelar;
    }

    public void setConsultaParaCancelar(ConsultaDTO c) {
        this.consultaParaCancelar = c;
    }

    public ConsultaDTO getConsultaParaRemarcar() {
        return consultaParaRemarcar;
    }

    public void setConsultaParaRemarcar(ConsultaDTO c) {
        this.consultaParaRemarcar = c;
    }

    public String getFiltroTexto() {
        return filtroTexto;
    }

    public void setFiltroTexto(String filtroTexto) {
        this.filtroTexto = filtroTexto;
    }

    public String getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(String filtroStatus) {
        this.filtroStatus = filtroStatus;
    }

    public boolean isExibirModalCancelamento() {
        return exibirModalCancelamento;
    }

    public void setExibirModalCancelamento(boolean exibirModalCancelamento) {
        this.exibirModalCancelamento = exibirModalCancelamento;
    }

    public boolean isExibirModalRemarcacao() {
        return exibirModalRemarcacao;
    }

    public void setExibirModalRemarcacao(boolean v) {
        this.exibirModalRemarcacao = v;
    }

    public Long getIdConsultaSelecionada() {
        return idConsultaSelecionada;
    }

    public void setIdConsultaSelecionada(Long id) {
        this.idConsultaSelecionada = id;
    }
}