package com.vittae.view;

import java.io.Serializable;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Named("adminConsultasBean")
@ViewScoped
public class AdminConsultasBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String API_URL = "http://localhost:9090/api/agendamentos";

    private transient HttpClient httpClient;
    private transient ObjectMapper objectMapper;

    private List<AdminConsultaDTO> consultas = new ArrayList<>();
    private String filtroTexto;
    private String filtroStatus;

    @PostConstruct
    public void inicializar() {
        garantirClientes();
        carregarConsultas();
    }

    public void carregarConsultas() {
        String token = getTokenSessao();

        if (token == null || token.isBlank()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Sessao expirada. Faca login novamente.");
            consultas = new ArrayList<>();
            return;
        }

        try {
            garantirClientes();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + token)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                consultas = objectMapper.readValue(
                        response.body(),
                        new TypeReference<List<AdminConsultaDTO>>() {}
                );
                ordenarConsultas();
            } else {
                consultas = new ArrayList<>();
                adicionarMensagem(FacesMessage.SEVERITY_ERROR,
                        "Erro ao carregar consultas. Codigo: " + response.statusCode());
            }
        } catch (Exception e) {
            consultas = new ArrayList<>();
            adicionarMensagem(FacesMessage.SEVERITY_ERROR,
                    "Erro ao carregar consultas: " + e.getMessage());
        }
    }

    public void cancelarConsulta(Long consultaId) {
        String token = getTokenSessao();

        if (consultaId == null) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Consulta invalida.");
            return;
        }

        if (token == null || token.isBlank()) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR, "Sessao expirada. Faca login novamente.");
            return;
        }

        try {
            garantirClientes();

            String json = "{\"status\":\"CANCELADA\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL + "/" + consultaId))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                adicionarMensagem(FacesMessage.SEVERITY_INFO, "Consulta cancelada com sucesso.");
                carregarConsultas();
            } else {
                adicionarMensagem(FacesMessage.SEVERITY_ERROR,
                        "Erro ao cancelar consulta. Codigo: " + response.statusCode());
            }
        } catch (Exception e) {
            adicionarMensagem(FacesMessage.SEVERITY_ERROR,
                    "Erro ao cancelar consulta: " + e.getMessage());
        }
    }

    public List<AdminConsultaDTO> getConsultasFiltradas() {
        String texto = filtroTexto != null ? filtroTexto.trim().toLowerCase() : "";
        String status = filtroStatus != null ? filtroStatus.trim() : "";

        return consultas.stream()
                .filter(c -> status.isBlank() || status.equalsIgnoreCase(c.getStatus()))
                .filter(c -> texto.isBlank() || contemTexto(c, texto))
                .collect(Collectors.toList());
    }

    public long getTotalConsultas() {
        return consultas.size();
    }

    public long getTotalPendentes() {
        return contarPorStatus("PENDENTE");
    }

    public long getTotalCanceladas() {
        return contarPorStatus("CANCELADA");
    }

    public long getTotalRealizadas() {
        return contarPorStatus("REALIZADA");
    }

    public boolean podeCancelar(AdminConsultaDTO consulta) {
        if (consulta == null || consulta.getStatus() == null) {
            return false;
        }

        String status = consulta.getStatus();
        return !"CANCELADA".equalsIgnoreCase(status)
                && !"REALIZADA".equalsIgnoreCase(status);
    }

    public String formatarData(String data) {
        if (data == null || data.isBlank()) {
            return "-";
        }

        try {
            return LocalDate.parse(data).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return data;
        }
    }

    public String formatarHora(String hora) {
        if (hora == null || hora.isBlank()) {
            return "-";
        }

        try {
            return LocalTime.parse(hora).format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return hora;
        }
    }

    public String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "-";
        }

        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }
    
    public String getClasseStatus(AdminConsultaDTO consulta) {
        if (consulta == null || consulta.getStatus() == null || consulta.getStatus().isBlank()) {
            return "admin-consultas-status-indefinido";
        }

        return "admin-consultas-status-" + consulta.getStatus().toLowerCase();
    }


    private void ordenarConsultas() {
        consultas.sort(Comparator
                .comparing(AdminConsultaDTO::getDataConsulta, Comparator.nullsLast(String::compareTo))
                .thenComparing(AdminConsultaDTO::getHora, Comparator.nullsLast(String::compareTo))
                .reversed());
    }

    private void garantirClientes() {
        if (httpClient == null) {
            httpClient = HttpClient.newHttpClient();
        }

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    private boolean contemTexto(AdminConsultaDTO consulta, String texto) {
        return contem(consulta.getNomePaciente(), texto)
                || contem(consulta.getNomeMedico(), texto)
                || contem(consulta.getEspecialidade(), texto)
                || contem(consulta.getStatus(), texto);
    }

    private boolean contem(String valor, String texto) {
        return valor != null && valor.toLowerCase().contains(texto);
    }

    private long contarPorStatus(String status) {
        return consultas.stream()
                .filter(c -> status.equalsIgnoreCase(c.getStatus()))
                .count();
    }

    private String getTokenSessao() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        return session != null ? (String) session.getAttribute("token") : null;
    }

    private void adicionarMensagem(FacesMessage.Severity severity, String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, mensagem, null));
    }

    public List<AdminConsultaDTO> getConsultas() {
        return consultas;
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

    public static class AdminConsultaDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String nomePaciente;
        private String nomeMedico;
        private String especialidade;
        private String dataConsulta;
        private String hora;
        private String status;
        private BigDecimal valorConsulta;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNomePaciente() {
            return nomePaciente;
        }

        public void setNomePaciente(String nomePaciente) {
            this.nomePaciente = nomePaciente;
        }

        public String getNomeMedico() {
            return nomeMedico;
        }

        public void setNomeMedico(String nomeMedico) {
            this.nomeMedico = nomeMedico;
        }

        public String getEspecialidade() {
            return especialidade;
        }

        public void setEspecialidade(String especialidade) {
            this.especialidade = especialidade;
        }

        public String getDataConsulta() {
            return dataConsulta;
        }

        public void setDataConsulta(String dataConsulta) {
            this.dataConsulta = dataConsulta;
        }

        public String getHora() {
            return hora;
        }

        public void setHora(String hora) {
            this.hora = hora;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
        public BigDecimal getValorConsulta() {
            return valorConsulta;
        }

        public void setValorConsulta(BigDecimal valorConsulta) {
            this.valorConsulta = valorConsulta;
        }
    }
}
