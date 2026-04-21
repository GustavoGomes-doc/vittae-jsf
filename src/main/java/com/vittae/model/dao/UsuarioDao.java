package com.vittae.model.dao;

import java.io.Serializable;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;

import com.vittae.model.Usuario;
import com.vittae.util.jpa.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsuarioDao implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Inject
	private EntityManager manager;

	private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioDao.class);

	public Usuario buscarPorEmail(String email) {
		try {
			return manager.createQuery("SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class)
					.setParameter("email", email).getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@Transactional
	public Usuario salvar(Usuario usuario) throws PersistenceException {
		LOGGER.info("salvar DAO... usuario = " + usuario);
		try {
			return manager.merge(usuario);
		} catch (PersistenceException e) {
			e.printStackTrace();
			throw e;
		}
	}

	// 🌟 Método de Autenticação implementado!
	public Usuario autenticar(String cpfLimpo, String senha) {
		try {
			return manager.createQuery("SELECT u FROM Usuario u WHERE u.cpf = :cpf AND u.senha = :senha", Usuario.class)
					.setParameter("cpf", cpfLimpo)
					.setParameter("senha", senha)
					.getSingleResult();
					
		} catch (NoResultException e) {
			// Não achou ninguém com esse CPF + Senha. Retorna null para o LoginBean avisar o front.
			return null;
		} catch (Exception e) {
			// Qualquer outro erro no banco
			LOGGER.error("Erro ao autenticar usuário: ", e);
			return null;
		}
	}

}