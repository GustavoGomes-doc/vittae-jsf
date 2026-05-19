package com.vittae.view;

import java.io.InputStream;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.Part;

import com.vittae.model.Disponibilidade;
import com.vittae.model.Especialidade;
import com.vittae.model.Medico;
import com.vittae.model.enums.DiaSemana;
import com.vittae.service.CadastrarMedicoService;
import com.vittae.service.MedicoEnvioDTO;

@Named
@ViewScoped
public class CadastrarMedicoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Medico dto;
	private CadastrarMedicoService service;
	private List<DiaUI> listaDias;
	private List<String> listaUfs;
	private Part foto;
	private String especialidadesSelecionadas;

	@PostConstruct
	public void init() {
		this.dto = new Medico();
		this.service = new CadastrarMedicoService();

		this.listaUfs = Arrays.asList("AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT", "MS", "MG",
				"PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO");

		// Inicializa os dias com os valores exatos que o seu Enum DiaSemana espera
		this.listaDias = new ArrayList<>();
		listaDias.add(new DiaUI("Segunda-feira", "SEGUNDA"));
		listaDias.add(new DiaUI("Terça-feira", "TERCA"));
		listaDias.add(new DiaUI("Quarta-feira", "QUARTA"));
		listaDias.add(new DiaUI("Quinta-feira", "QUINTA"));
		listaDias.add(new DiaUI("Sexta-feira", "SEXTA"));
		listaDias.add(new DiaUI("Sábado", "SABADO"));
	}

	public void salvar() {
	    try {
	        // monta o DTO de envio
	        MedicoEnvioDTO medicoDTO = new MedicoEnvioDTO();
	        medicoDTO.setNome(dto.getNome());
	        medicoDTO.setCpf(dto.getCpf());
	        medicoDTO.setEmail(dto.getEmail());
	        medicoDTO.setSenha(dto.getSenha());
	        medicoDTO.setCrm(dto.getCrm());
	        medicoDTO.setUfCrm(dto.getUfCrm());
	        medicoDTO.setValorConsulta(dto.getValorConsulta());
	        medicoDTO.setTempoConsultaMinutos(dto.getTempoConsultaMinutos());
	        medicoDTO.setDataNascimento(dto.getDataNascimento());

	        // foto
	        if (foto != null) {
	            InputStream input = foto.getInputStream();
	            medicoDTO.setFoto(input.readAllBytes());
	        }

	        // especialidades
	        if (especialidadesSelecionadas != null && !especialidadesSelecionadas.isEmpty()) {
	            List<String> listaEsp = Arrays.stream(especialidadesSelecionadas.split(","))
	                .map(String::trim)
	                .collect(Collectors.toList());
	            medicoDTO.setEspecialidades(listaEsp);
	        }

	        // disponibilidades
	        List<MedicoEnvioDTO.DisponibilidadeEnvioDTO> disponibilidades = listaDias.stream()
	            .filter(DiaUI::isSelecionado)
	            .map(dia -> {
	                MedicoEnvioDTO.DisponibilidadeEnvioDTO d = new MedicoEnvioDTO.DisponibilidadeEnvioDTO();
	                d.setDiaSemana(dia.getValorEnum());
	                d.setHoraInicio(dia.getHoraInicio());
	                d.setHoraFim(dia.getHoraFim());
	                return d;
	            })
	            .collect(Collectors.toList());
	        medicoDTO.setDisponibilidades(disponibilidades);

	        service.salvarMedico(medicoDTO);

	        FacesContext.getCurrentInstance().addMessage(null,
	            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Médico cadastrado com sucesso."));

	        init();

	    } catch (Exception e) {
	        FacesContext.getCurrentInstance().addMessage(null,
	            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao processar dados: " + e.getMessage()));
	    }
	}

	// --- Getters e Setters do Bean ---
	public Medico getDto() {
		return dto;
	}

	public void setDto(Medico dto) {
		this.dto = dto;
	}
	

	public List<DiaUI> getListaDias() {
		return listaDias;
	}

	public void setListaDias(List<DiaUI> listaDias) {
		this.listaDias = listaDias;
	}

	public List<String> getListaUfs() {
		return listaUfs;
	}

	public void setListaUfs(List<String> listaUfs) {
		this.listaUfs = listaUfs;
	}

	public Part getFoto() {
		return foto;
	}

	public void setFoto(Part foto) {
		this.foto = foto;
	}

	public String getEspecialidadesSelecionadas() {
		return especialidadesSelecionadas;
	}

	public void setEspecialidadesSelecionadas(String especialidadesSelecionadas) {
		this.especialidadesSelecionadas = especialidadesSelecionadas;
	}

	// --- Classe Auxiliar para gerenciar a repetição de dias na tela ---
	public static class DiaUI {
		private String label;
		private String valorEnum;
		private boolean selecionado;
		private String horaInicio;
		private String horaFim;

		public DiaUI(String label, String valorEnum) {
			this.label = label;
			this.valorEnum = valorEnum;
		}

		public String getLabel() {
			return label;
		}

		public String getValorEnum() {
			return valorEnum;
		}

		public boolean isSelecionado() {
			return selecionado;
		}

		public void setSelecionado(boolean selecionado) {
			this.selecionado = selecionado;
		}

		public String getHoraInicio() {
			return horaInicio;
		}

		public void setHoraInicio(String horaInicio) {
			this.horaInicio = horaInicio;
		}

		public String getHoraFim() {
			return horaFim;
		}

		public void setHoraFim(String horaFim) {
			this.horaFim = horaFim;
		}
	}
}