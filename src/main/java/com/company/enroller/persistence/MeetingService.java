package com.company.enroller.persistence;

import com.company.enroller.model.Meeting;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component("meetingService")
public class MeetingService {

	DatabaseConnector connector;

	public MeetingService() {
		connector = DatabaseConnector.getInstance();
	}

    public boolean isFieldValid(String fieldName) {
        try {
            connector.getSession().getMetamodel().entity(Meeting.class).getAttribute(fieldName);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

	public Collection<Meeting> getAll() {
		String hql = "FROM Meeting";
		Query query = connector.getSession().createQuery(hql);
		return query.list();
	}

    public Collection<Meeting> getAll(String sortBy, String sortOrder, String key) {
        String finalKey = (key == null || key.isEmpty()) ? "%" : "%" + key + "%";

        String hql = "FROM Meeting WHERE lower(title) LIKE lower(:ftitle) " +
                "ORDER BY " + sortBy + " " + sortOrder;

        Query query = connector.getSession().createQuery(hql);
        query.setParameter("ftitle", finalKey);
        return query.list();
    }

    public Meeting findById(long id) {
        return (Meeting) connector.getSession().get(Meeting.class, id);
    }

    public void addMeeting(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().save(meeting);
        transaction.commit();
    }

    public void updateMeeting(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().merge(meeting);
        transaction.commit();
    }

    public void deleteMeeting(Meeting meeting) {
        Transaction transaction = connector.getSession().beginTransaction();
        connector.getSession().delete(meeting);
        transaction.commit();
    }

}
