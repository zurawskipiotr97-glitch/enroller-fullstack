package com.company.enroller.controllers;

import com.company.enroller.model.Meeting;
import com.company.enroller.model.Participant;
import com.company.enroller.persistence.MeetingService;
import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/meetings")
public class MeetingRestController {

    @Autowired
    MeetingService meetingService;

    @Autowired
    ParticipantService participantService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getMeetings(
            @RequestParam(value = "sortBy", defaultValue = "title") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "ASC") String sortOrder,
            @RequestParam(defaultValue = "%") String key
    ) {
        if (!meetingService.isFieldValid(sortBy)) {
            return new ResponseEntity<>("Błąd: Pole '" + sortBy + "' nie istnieje!", HttpStatus.BAD_REQUEST);
        }

        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String searchKey = (key == null) ? "" : key;

        Collection<Meeting> meetings = meetingService.getAll(sortBy, sortOrder, searchKey);
        if (meetings.isEmpty()) {
            return new ResponseEntity<>("Brak wyników dla: " + searchKey, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<Meeting>>(meetings, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getMeeting(@PathVariable("id") long id){
       Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Meeting>(meeting, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipants(@PathVariable("id") long id){
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(
                    "Nie ma takiego spotkania",
                    HttpStatus.NOT_FOUND
            );
        }

        Collection<Participant> participants = meeting.getParticipants();
        if (participants.isEmpty()) {
            return new ResponseEntity<>(
                    "Spotkanie: " + meeting.getTitle()
                    + " nie ma przypisanych uczestników: ",
                    HttpStatus.NOT_FOUND
            );
        }
        return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.POST)
    public ResponseEntity<?> addParticipants(
            @PathVariable("id") long id,
            @RequestBody Participant participantData
    ){
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(
                    "Nie ma takiego spotkania",
                    HttpStatus.NOT_FOUND
            );
        }

        String login = participantData.getLogin();
        Participant participant = participantService.findByLogin(login);
        if (participant == null) {
            return new ResponseEntity<>(
                    "Uczestnik o loginie "
                    + login + " nie jest zarejestrowany w systemie.",
                    HttpStatus.NOT_FOUND
            );
        }

        if (meeting.getParticipants().contains(participant)) {
            return new ResponseEntity<>(
                    "Użytkownik już jest dodany do spotkania",
                    HttpStatus.CONFLICT
            );
        }
        meeting.addParticipant(participant);
        meetingService.updateMeeting(meeting);

        return new ResponseEntity<>("Użytkownik: " + participant.getLogin()
                + " został dodany do spotkania",
                HttpStatus.OK
        );
    }

    @RequestMapping(value = "/{id}/participants/{login}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteParticipant(@PathVariable("id") long id,
                                                @PathVariable("login") String login){
        Meeting meeting = meetingService.findById(id);
        if (meeting == null) {
            return new ResponseEntity<>(
                    "Nie ma takiego spotkania",
                    HttpStatus.NOT_FOUND
            );
        }

        Participant deletedParticipant = participantService.findByLogin(login);

        if (deletedParticipant == null ||!meeting.getParticipants().contains(deletedParticipant)) {
            return new ResponseEntity<>(
                    "Użytkownik nie był dodany do spotkania",
                    HttpStatus.CONFLICT
            );
        }

        meeting.removeParticipant(deletedParticipant);
        meetingService.updateMeeting(meeting);

        return new ResponseEntity<>("Użytkownik: " + deletedParticipant.getLogin()
                + " został usunięty ze spotkania",
                HttpStatus.OK
        );
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> registerNewMeeting(@RequestBody Meeting meeting) {
            meetingService.addMeeting(meeting);
            return new ResponseEntity<>(
                    "Meeting: " + meeting.getTitle() + " added successfully",
                    HttpStatus.CREATED
            );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteMeeting(@PathVariable("id") long id) {
        Meeting existingMeeting = meetingService.findById(id);

        if (existingMeeting != null) {
            meetingService.deleteMeeting(existingMeeting);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>("Nie było takiego spotkania", HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity<?> updateMeeting(@RequestBody Meeting meeting) {
        Meeting existingMeeting = meetingService.findById(meeting.getId());
        if (existingMeeting == null) {
            return new ResponseEntity<>(
                    "Nie znaleziono spotkania do aktualizacji",
                    HttpStatus.NOT_FOUND
            );
        }
        meetingService.updateMeeting(meeting);
        return new ResponseEntity<>("Spotkanie zostało zaktualizowane", HttpStatus.ACCEPTED);
    }
}
