import { useEffect, useState } from "react";
import { toast } from "react-toastify";

export default function MeetingsList({ meetings, username, onDelete }) {
    const [participantsByMeeting, setParticipantsByMeeting] = useState({});

    useEffect(() => {
        meetings.forEach(meeting => {
            fetchParticipants(meeting.id);
        });
    }, [meetings]);

    async function fetchParticipants(meetingId) {
        const response = await fetch(`/api/meetings/${meetingId}/participants`);

        if (response.ok) {
            const participants = await response.json();

            setParticipantsByMeeting(prev => ({
                ...prev,
                [meetingId]: participants
            }));
        } else {
            setParticipantsByMeeting(prev => ({
                ...prev,
                [meetingId]: []
            }));
        }
    }

    async function handleNewParticipant(meeting) {
        const response = await fetch(`/api/meetings/${meeting.id}/participants`, {
            method: "POST",
            body: JSON.stringify({
                login: username
            }),
            headers: {
                "Content-Type": "application/json"
            }
        });

        if (response.ok) {
            toast.success("Zapisano na spotkanie!");
            await fetchParticipants(meeting.id);
        }else{
            const message = await response.text();

            toast.error(message);

        }
    }

    async function handleDeleteParticipant(meeting) {
        const response = await fetch(
            `/api/meetings/${meeting.id}/participants/${username}`,
            {
                method: "DELETE"
            }
        );

        if (response.ok) {
            toast.info("Wypisano ze spotkania");

            await fetchParticipants(meeting.id);
        } else {
            toast.error("Nie udało się wypisać");
        }
    }

    function handleDeleteMeeting(meeting) {
        const participants = participantsByMeeting[meeting.id] || [];

        if (participants.length === 0) {
            toast.warning("Usuwanie spotkania...");

            onDelete(meeting);
        } else {
            toast.error("Nie można usunąć spotkania z uczestnikami");
        }
    }

    return (
        <table>
            <thead>
            <tr>
                <th>Nazwa spotkania</th>
                <th>Opis</th>
                <th>Data</th>
                <th>Uczestnicy</th>
                <th></th>
            </tr>
            </thead>

            <tbody>
            {meetings.map((meeting, index) => {
                const participants = participantsByMeeting[meeting.id] || [];

                const isUserParticipant = participants.some(
                    participant => participant.login === username
                );

                return (
                    <tr key={meeting.id || index}>
                        <td>{meeting.title}</td>
                        <td>{meeting.description}</td>
                        <td>{meeting.date}</td>

                        <td>
                            {participants.length > 0 ? (
                                <ul style={{ margin: 0, padding: "20px" }}>
                                    {participants.map((participant, index) => (
                                        <li key={participant.login || index}>
                                            {participant.login}
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <em style={{ color: "gray" }}>Brak uczestników</em>
                            )}
                        </td>

                        <td>
                            <div className="float-right buttons-to-right">
                                {!isUserParticipant ? (
                                    <button
                                        className="button button-outline"
                                        onClick={() => handleNewParticipant(meeting)}
                                    >
                                        Zapisz się
                                    </button>
                                ) : (
                                    <button
                                        className="button button-outline"
                                        onClick={() => handleDeleteParticipant(meeting)}
                                    >
                                        Wypisz się
                                    </button>
                                )}

                                {participants.length === 0 && (
                                    <button
                                        className="button"
                                        style={{ margin: "0px" }}
                                        onClick={() => handleDeleteMeeting(meeting)}
                                    >
                                        Usuń puste spotkanie
                                    </button>
                                )}
                            </div>
                        </td>
                    </tr>
                );
            })}
            </tbody>
        </table>
    );
}