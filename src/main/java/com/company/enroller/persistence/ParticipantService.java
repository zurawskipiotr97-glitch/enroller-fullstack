package com.company.enroller.persistence;

import com.company.enroller.model.Participant;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("participantService")
public class ParticipantService {

	DatabaseConnector connector;

	public ParticipantService() {
		connector = DatabaseConnector.getInstance();
	}


    public Collection<Participant> getAll() {
        String hql = "FROM Participant";
        Query query = connector.getSession().createQuery(hql);
        return query.list();
    }
	public Collection<Participant> getAll(String sortBy, String sortOrder, String key) {
        String finalKey = (key == null || key.isEmpty()) ? "%" : "%" + key + "%";

        String hql = "FROM Participant WHERE lower(login) LIKE lower(:flogin) " +
                "ORDER BY " + sortBy + " " + sortOrder;

        Query query = connector.getSession().createQuery(hql);
        query.setParameter("flogin", finalKey);
        return query.list();
	}

    public Participant findByLogin(String login) {
        return (Participant) connector.getSession().get(Participant.class, login);
    }

    public void addParticipant(Participant participant) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().save(participant);
        transaction.commit();
    }

    public void updateParticipant(Participant participant) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().merge(participant);
        transaction.commit();
    }

    public void deleteParticipant(Participant participant) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().delete(participant);
        transaction.commit();
    }

    public boolean isFieldValid(String fieldName) {
        try {
            connector.getSession().getMetamodel().entity(Participant.class).getAttribute(fieldName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
