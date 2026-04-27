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
			if (foto != null) {
				InputStream input = foto.getInputStream();
				dto.setFoto(input.readAllBytes());
			}

			//Sintaxe limpa com Java Streams
			if (especialidadesSelecionadas != null && !especialidadesSelecionadas.isEmpty()) {
                List<String> listaEsp = Arrays.stream(especialidadesSelecionadas.split(","))
                    .map(String::trim) // Limpa os espaços em branco
                    .collect(Collectors.toList());
                
                dto.setEspecialidades(listaEsp);
            }

			//processar Disponibilidades (Com LocalTime e Enum)
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("HH:mm");

			List<Disponibilidade> disponibilidades = listaDias.stream().filter(DiaUI::isSelecionado).map(dia -> {
				Disponibilidade d = new Disponibilidade();
				d.setDiaSemana(DiaSemana.valueOf(dia.getValorEnum()));

				//conversão de String da tela para LocalTime do Java
				if (dia.getHoraInicio() != null && !dia.getHoraInicio().isEmpty()) {
					d.setHoraInicio(LocalTime.parse(dia.getHoraInicio(), parser));
				}
				if (dia.getHoraFim() != null && !dia.getHoraFim().isEmpty()) {
					d.setHoraFim(LocalTime.parse(dia.getHoraFim(), parser));
				}

				d.setMedico(dto);
				return d;
			}).collect(Collectors.toList());

			dto.setDisponibilidades(disponibilidades);

			// 4. Enviar para o Service (Spring Boot)
			service.salvarMedico(dto);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso!", "Médico cadastrado com sucesso."));

			init(); // Limpa a tela para o próximo cadastro

		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro",
					"Erro ao processar dados: " + e.getMessage()));
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