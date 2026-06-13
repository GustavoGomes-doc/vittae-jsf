package com.vittae.view;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;

@Named("disponibilidadeBean")
@ViewScoped
public class DisponibilidadeBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final String API_URL = "http://localhost:9090/api/disponibilidade/";

	private Long medicoId;
	private String token;
	private List<Map<String, String>> disponibilidades = new ArrayList<>();

	// Campos do formulário
	private String diaSemana;
	private String horaInicio;
	private String horaFim;

	private final HttpClient client = HttpClient.newHttpClient();
	private final ObjectMapper mapper = new ObjectMapper();

	@PostConstruct
	public void init() {
		HttpSession session = getSession();
		if (session != null) {
			medicoId = (Long) session.getAttribute("usuarioId");
			token = (String) session.getAttribute("token");
		}
		carregarDisponibilidades();
	}

	public void carregarDisponibilidades() {
		try {
			HttpRequest request = HttpRequest.newBuilder().uri(URI.create(API_URL + medicoId))
					.header("Authorization", "Bearer " + token).GET().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				disponibilidades = mapper.readValue(response.body(),
						mapper.getTypeFactory().constructCollectionType(List.class, Map.class));
			}
		} catch (Exception e) {
			addErro("Erro ao carregar disponibilidades: " + e.getMessage());
		}
	}

	public void salvar() {
	    if (diaSemana == null || horaInicio == null || horaFim == null 
	            || diaSemana.isBlank() || horaInicio.isBlank() || horaFim.isBlank()) {
	        addErro("Preencha todos os campos.");
	        return;
	    }
	    if (!horaInicio.matches("\\d{2}:\\d{2}") || !horaFim.matches("\\d{2}:\\d{2}")) {
	        addErro("Formato de horário inválido. Use HH:MM.");
	        return;
	    }
	    int minInicio = Integer.parseInt(horaInicio.split(":")[1]);
	    int minFim    = Integer.parseInt(horaFim.split(":")[1]);
	    if (minInicio % 30 != 0 || minFim % 30 != 0) {
	        addErro("Horários devem ser em intervalos de 30 minutos (ex: 08:00 ou 08:30).");
	        return;
	    }

	    if (horaInicio.compareTo("07:00") < 0 || horaFim.compareTo("19:00") > 0) {
	        addErro("Horários permitidos: 07:00 às 19:00.");
	        return;
	    }

	    if (horaFim.compareTo(horaInicio) <= 0) {
	        addErro("Horário de fim deve ser maior que o de início.");
	        return;
	    }

	    boolean jaExiste = disponibilidades.stream()
	            .anyMatch(d -> diaSemana.equals(d.get("diaSemana")));
	    if (jaExiste) {
	        addErro("Já existe um horário cadastrado para " + diaSemana + ". Remova antes de adicionar novo.");
	        return;
	    }

	    try {
	        HttpSession session = getSession();
	        String tokenAtual = session != null ? (String) session.getAttribute("token") : token;

	        String json = mapper.writeValueAsString(
	                Map.of("diaSemana", diaSemana, "horaInicio", horaInicio, "horaFim", horaFim));

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(API_URL + medicoId))
	                .header("Content-Type", "application/json")
	                .header("Authorization", "Bearer " + tokenAtual)
	                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        if (response.statusCode() == 200) {
	            addSucesso("Disponibilidade adicionada!");
	            diaSemana = null;
	            horaInicio = null;
	            horaFim = null;
	            carregarDisponibilidades();
	        } else {
	            addErro("Erro ao salvar: " + response.statusCode());
	        }
	    } catch (Exception e) {
	        addErro("Erro: " + e.getMessage());
	    }
	}

	public void remover(String id) {
	    try {
	        HttpSession session = getSession();
	        String tokenAtual = session != null ? (String) session.getAttribute("token") : token;

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(API_URL + id))
	                .header("Authorization", "Bearer " + tokenAtual)
	                .DELETE().build();

	        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

	        if (response.statusCode() == 204 || response.statusCode() == 200) {
	            addSucesso("Disponibilidade removida!");
	            carregarDisponibilidades();
	        } else {
	            addErro("Erro ao remover. Status: " + response.statusCode());
	        }
	    } catch (Exception e) {
	        addErro("Erro: " + e.getMessage());
	    }
	}
	
	

	private HttpSession getSession() {
		return ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest())
				.getSession(false);
	}

	private void addSucesso(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
	}

	private void addErro(String msg) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

	public Long getMedicoId() {
		return medicoId;
	}

	public String getToken() {
		return token;
	}

	public List<Map<String, String>> getDisponibilidades() {
		return disponibilidades;
	}

	public String getDiaSemana() {
		return diaSemana;
	}

	public void setDiaSemana(String diaSemana) {
		this.diaSemana = diaSemana;
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