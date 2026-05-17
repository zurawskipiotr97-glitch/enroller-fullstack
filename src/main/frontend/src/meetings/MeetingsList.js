import {useState} from "react";

export default function MeetingsList({meetings, username, onDelete}) {
    const [contestants, setContestants] = useState([])
    // const [contestantName, setContestantNmae]

    function handleNewContestant(contestant) {
        const nextContestants = [...contestants, contestant];
        setContestants(nextContestants)
    }

    function handleDeleteContestant() {

    }

    function handleDeleteMeeting(meeting){
        if (contestants.length === 0) {
            onDelete(meeting)
        }
    }

    return (
        <table>
            <thead>
            <tr>
                <th>Nazwa spotkania</th>
                <th>Opis</th>
                <th>Uczestnicy</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            {
                meetings.map((meeting, index) => <tr key={index}>
                        <td>{meeting.title}</td>
                        <td>{meeting.description}</td>
                        <td>

                            {contestants.length > 0 ? (
                                <ul style={{ margin: 0, padding: "20px" }}>
                                    {contestants.map((contestant, index) => (
                                        <li key={index}>{contestant}</li>
                                    ))}
                                </ul>
                            ) : (
                                <em style={{ color: "gray" }}>Brak uczestników</em>
                            )}
                        </td>

                        <td float-left>
                            {<div className="float-right buttons-to-right">
                                {
                                    true
                                        // TODO
                                        //        dopisz jakiś sensowny warunek

                                        ? <button className="button button-outline" onClick={handleNewContestant}>Zapisz się</button>
                                        : <button className="button button-outline">Wypisz się</button>
                                }

                                {
                                    contestants.length === 0 && (
                                        <button
                                            className="button"
                                            style={{ margin: '0px' }}
                                            onClick={() => handleDeleteMeeting(meeting)}
                                        >
                                            Usuń puste spotkanie
                                        </button>
                                    )
                                }
                            </div>
                            }

                        </td>
                    </tr>
                )
            }
            </tbody>
        </table>
    );
}