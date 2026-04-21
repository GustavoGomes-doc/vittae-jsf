package com.vittae.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendamentoDTO {

    // 1. Dados diretos da Consulta (exatamente com os nomes do Payload do React)
    private String tipoConsulta;
    private String especialidade;
    private LocalDate dataAgendado;
    private LocalDate dataConsulta;
    private LocalTime hora;
    private Long medicoId;
    private String observacoes;
    private Double valorconsulta; // Pode ser Integer se você não usar centavos
    
    // 2. O "Pacotinho" do Paciente que vem do Frontend
    private PacienteDTO paciente;

    public AgendamentoDTO() {
    }

    // ==========================================
    // GETTERS E SETTERS DA CONSULTA
    // ==========================================

    public String getTipoConsulta() { return tipoConsulta; }
    public void setTipoConsulta(String tipoConsulta) { this.tipoConsulta = tipoConsulta; }

    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }

    public LocalDate getDataAgendado() { return dataAgendado; }
    public void setDataAgendado(LocalDate dataAgendado) { this.dataAgendado = dataAgendado; }

    public LocalDate getDataConsulta() { return dataConsulta; }
    public void setDataConsulta(LocalDate dataConsulta) { this.dataConsulta = dataConsulta; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Long getMedicoId() { return medicoId; }
    public void setMedicoId(Long medicoId) { this.medicoId = medicoId; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public Double getValorconsulta() { return valorconsulta; }
    public void setValorconsulta(Double valorconsulta) { this.valorconsulta = valorconsulta; }

    public PacienteDTO getPaciente() { return paciente; }
    public void setPaciente(PacienteDTO paciente) { this.paciente = paciente; }

    // ==========================================
    // CLASSE INTERNA: O MODELO DO PACIENTE
    // ==========================================
    // Isso é necessário porque o Frontend manda: "paciente": { "nome": "...", "cpf": "..." }
    public static class PacienteDTO {
        private String nome;
        private String cpf;
        private String telefone;

        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }

        public String getCpf() { return cpf; }
        public void setCpf(String cpf) { this.cpf = cpf; }

        public String getTelefone() { return telefone; }
        public void setTelefone(String telefone) { this.telefone = telefone; }
    }
}