package controllers;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shiro.SecurityUtils;

import connectors.ChallengeUpdater;
import entities.Transaction;
import helpers.AuthenticationHelper;

@XmlRootElement
@Path("/transactions")
public class TransactionController {
	
	@GET
	@Path("")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Transaction> getTransactions(Transaction transaction) {
		final EntityManager em = EntityManagerService.createEntityManager();
		try {
			System.out.println(SecurityUtils.getSubject().isAuthenticated() ? "logged1" : "not logged1");
			final TypedQuery<Transaction> query =
				em.createNamedQuery(Transaction.QUERY_BY_USER, Transaction.class);
			query.setParameter("user", AuthenticationHelper.getCurrentUser());
			return query.getResultList();
		} finally {
			em.close();
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction getTransaction(@PathParam("id") long id) {
		final EntityManager em = EntityManagerService.createEntityManager();
		try {
			final Transaction result = em.find(Transaction.class, id);
			if (result == null) {
				throw new IllegalArgumentException(
						"No task with id: " + id);
			}
			return result;
		} finally {
			em.close();
		}
	}
	
	@POST
	@Path("")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction createTransaction(Transaction transaction) {
		final EntityManager em = EntityManagerService.createEntityManager();
		try {
			transaction.setUser(AuthenticationHelper.getCurrentUser());
			em.getTransaction().begin();
			em.persist(transaction);
			(new ChallengeUpdater()).onCreate(transaction);
			em.getTransaction().commit();
			
			return transaction;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@DELETE
	@Path("/{id}")
	public void deleteTransaction(@PathParam("id") long id) {
		final EntityManager em = EntityManagerService.createEntityManager();
		try {
			em.getTransaction().begin();
			final Transaction transaction = em.find(Transaction.class, id);
			if (transaction == null) {
				throw new IllegalArgumentException(
						"No task with id: " + id);
			}
			(new ChallengeUpdater()).onDelete(transaction);
			em.remove(transaction);	
			em.getTransaction().commit();
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
	
	@PUT
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Transaction updateTransaction(@PathParam("id") long id, Transaction transaction) {
		final EntityManager em = EntityManagerService.createEntityManager();
		final Transaction old = (Transaction) em.find(Transaction.class, id).clone();
		try {
			transaction.setId((int)id);
			em.getTransaction().begin();
			Transaction result = em.merge(transaction);
			(new ChallengeUpdater()).onUpdate(old, result);
			em.getTransaction().commit();
			return result;
		} finally {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
		}
	}
}
