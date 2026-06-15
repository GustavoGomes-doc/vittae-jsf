package com.vittae.view;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vittae.dto.PacienteListagemDTO;
import com.vittae.util.ConfigUtil;

@Named("meusPacientesBean")
@ViewScoped
public class MeusPacientesBean implements Serializable {

	private static final String API_URL = ConfigUtil.get("api.base.url") + "/api/agendamentos/medico/";

	private List<PacienteResumoDTO> todosPacientes = new ArrayList<>();
	private List<PacienteResumoDTO> pacientesFiltrados = new ArrayList<>();

	private String filtroTexto = "";
	private Long medicoId;
	private String token;

	@PostConstruct
	public void init() {
		HttpSession session = getSession();
		if (session != null) {
			Object idObj = session.getAttribute("usuarioId");
			medicoId = idObj instanceof Long ? (Long) idObj : Long.valueOf(idObj.toString());
			token = (String) session.getAttribute("token");
		}
		carregarConsultas();
	}

	public void carregarConsultas() {
		if (medicoId == null || token == null)
			return;
		try {
			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + medicoId))
					.header("Authorization", "Bearer " + token).GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

				List<PacienteListagemDTO> consultas = mapper.readValue(response.body(),
						mapper.getTypeFactory().constructCollectionType(List.class, PacienteListagemDTO.class));

				// Agrupa por nome do paciente
				Map<String, List<PacienteListagemDTO>> porPaciente = consultas.stream()
						.collect(Collectors.groupingBy(c -> c.getNomePaciente() != null ? c.getNomePaciente() : "—"));

				todosPacientes = porPaciente.entrySet().stream().map(entry -> {
					String nome = entry.getKey();
					List<PacienteListagemDTO> cs = entry.getValue();
					PacienteListagemDTO primeira = cs.get(0);

					PacienteResumoDTO p = new PacienteResumoDTO();
					p.setNome(nome);
					p.setDataNascimento(primeira.getDataNascimento());
					p.setNomeResponsavel(primeira.getNomeResponsavel());
					p.setQtdConsultas(cs.size());
					return p;
				}).sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome())).collect(Collectors.toList());

				aplicarFiltros();
			}
		} catch (Exception e) {
			System.err.println("MeusPacientesBean.carregarConsultas: " + e.getMessage());
		}
	}

	public void filtrar() {
		aplicarFiltros();
	}

	private void aplicarFiltros() {
		if (filtroTexto == null || filtroTexto.trim().isEmpty()) {
			pacientesFiltrados = new ArrayList<>(todosPacientes);
		} else {
			String texto = filtroTexto.trim().toLowerCase();
			pacientesFiltrados = todosPacientes.stream()
					.filter(p -> p.getNome() != null && p.getNome().toLowerCase().contains(texto))
					.collect(Collectors.toList());
		}
	}

	private HttpSession getSession() {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc == null)
			return null;
		return (HttpSession) fc.getExternalContext().getSession(false);
	}

	public List<PacienteResumoDTO> getPacientesFiltrados() {
		return pacientesFiltrados;
	}

	public int getTotalFiltradas() {
		return pacientesFiltrados.size();
	}

	public String getFiltroTexto() {
		return filtroTexto;
	}

	public void setFiltroTexto(String v) {
		this.filtroTexto = v;
	}

	// ── DTO interno ─────────────────────────────────────────────────────────
	public static class PacienteResumoDTO implements Serializable {
		private String nome;
		private LocalDate dataNascimento;
		private String nomeResponsavel;
		private int qtdConsultas;

		public String getNome() {
			return nome;
		}

		public void setNome(String v) {
			this.nome = v;
		}

		public LocalDate getDataNascimento() {
			return dataNascimento;
		}

		public void setDataNascimento(LocalDate v) {
			this.dataNascimento = v;
		}

		public String getNomeResponsavel() {
			return nomeResponsavel;
		}

		public void setNomeResponsavel(String v) {
			this.nomeResponsavel = v;
		}

		public int getQtdConsultas() {
			return qtdConsultas;
		}

		public void setQtdConsultas(int v) {
			this.qtdConsultas = v;
		}
	}
}