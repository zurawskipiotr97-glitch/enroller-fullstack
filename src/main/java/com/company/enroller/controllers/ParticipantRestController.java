package com.company.enroller.controllers;

import com.company.enroller.model.Participant;
import com.company.enroller.persistence.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/api/participants")
public class ParticipantRestController {

    @Autowired
    ParticipantService participantService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipants(
            @RequestParam(value = "sortBy", defaultValue = "login") String sortBy,
            @RequestParam(value = "sortOrder", defaultValue = "ASC") String sortOrder,
            @RequestParam(defaultValue = "%") String key
    ) {
        if (!participantService.isFieldValid(sortBy)) {
            return new ResponseEntity<>("Błąd: Pole '" + sortBy + "' nie istnieje!", HttpStatus.BAD_REQUEST);
        }

        if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
            sortOrder = "ASC";
        }

        String searchKey = (key == null) ? "" : key;

        Collection<Participant> participants = participantService.getAll(sortBy, sortOrder, key);
        if (participants.isEmpty()) {
            return new ResponseEntity<>("Brak wyników dla: " + searchKey, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Collection<Participant>>(participants, HttpStatus.OK);
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> getParticipant(@PathVariable("id") String login) {
        Participant participant = participantService.findByLogin(login);
        if (participant == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<Participant>(participant, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<?> registerNewParticipant(@RequestBody Participant participant) {
        Participant existingParticipant = participantService.findByLogin(participant.getLogin());

            if (existingParticipant == null) {

                String hashedPassword = passwordEncoder.encode(participant.getPassword());
                participant.setPassword(hashedPassword);
                participantService.addParticipant(participant);
                return new ResponseEntity<>(
                        "User: " + participant.getLogin() + " registered successfully",
                        HttpStatus.CREATED
                );
            }

            return new ResponseEntity<>(
                    "Unable to create. A participant with login " + participant.getLogin()
                    + " already exist.",
                    HttpStatus.CONFLICT
            );
            }

    @DeleteMapping("")
    public ResponseEntity<?> deleteParticipant(@RequestBody Participant participant) {
        Participant existingParticipant = participantService.findByLogin(participant.getLogin());

        if (existingParticipant != null) {
            participantService.deleteParticipant(existingParticipant);
            return new ResponseEntity<>("Użytkownik został usunięty", HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>("Nie było takiego użytkownika", HttpStatus.NOT_FOUND);
    }

    @PutMapping("")
    public ResponseEntity<?> updateParticipant(@RequestBody Participant participant) {
        Participant existingParticipant = participantService.findByLogin(participant.getLogin());
        if (existingParticipant == null) {
            return new ResponseEntity<>(
                    "Nie znaleziono użytkownika do aktualizacji",
                    HttpStatus.NOT_FOUND
            );
        }
        participantService.updateParticipant(participant);
        return new ResponseEntity<>("Użytkownik został zaktualizowany", HttpStatus.ACCEPTED);
    }
}
