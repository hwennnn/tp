package casetrack.app.logic.commands;

import static casetrack.app.logic.parser.CliSyntax.PREFIX_NAME;
import static casetrack.app.logic.parser.CliSyntax.PREFIX_NOTE_TEXT;
import static casetrack.app.logic.parser.CliSyntax.PREFIX_PHONE;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import casetrack.app.commons.core.index.Index;
import casetrack.app.commons.util.ToStringBuilder;
import casetrack.app.logic.Messages;
import casetrack.app.logic.commands.exceptions.CommandException;
import casetrack.app.model.Model;
import casetrack.app.model.person.Name;
import casetrack.app.model.person.Note;
import casetrack.app.model.person.Person;
import casetrack.app.model.person.Phone;

/**
 * Adds a note to an existing person in the address book.
 */
public class NoteCommand extends Command {

    public static final String COMMAND_WORD = "note";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a note to a person. "
            + "Parameters: "
            + "INDEX " + PREFIX_NOTE_TEXT + "TEXT\n"
            + "OR "
            + PREFIX_NAME + "NAME " + PREFIX_PHONE + "PHONE " + PREFIX_NOTE_TEXT + "TEXT\n"
            + "Example: " + COMMAND_WORD + " "
            + "1 " + PREFIX_NOTE_TEXT + "Follow-up in 2 weeks about housing support\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe " + PREFIX_PHONE + "91234567 "
            + PREFIX_NOTE_TEXT + "Mother mentioned financial difficulties";

    public static final String MESSAGE_SUCCESS = "Note added for %1$s (%2$s): %3$s";
    public static final String MESSAGE_PERSON_NOT_FOUND = "No patient found with the given details.";
    public static final String MESSAGE_MISSING_PATIENT_REFERENCE = "Either index or (Name and Phone) is required.";

    private final Optional<Index> targetIndex;
    private final Optional<Name> name;
    private final Optional<Phone> phone;
    private final Note note;

    /**
     * Creates a NoteCommand to add a note to a person identified by index.
     */
    public NoteCommand(Index targetIndex, Note note) {
        requireNonNull(targetIndex);
        requireNonNull(note);
        this.targetIndex = Optional.of(targetIndex);
        this.name = Optional.empty();
        this.phone = Optional.empty();
        this.note = note;
    }

    /**
     * Creates a NoteCommand to add a note to a person identified by name and phone.
     */
    public NoteCommand(Name name, Phone phone, Note note) {
        requireNonNull(name);
        requireNonNull(phone);
        requireNonNull(note);
        this.targetIndex = Optional.empty();
        this.name = Optional.of(name);
        this.phone = Optional.of(phone);
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        Person personToUpdate = findPerson(model);
        Person updatedPerson = personToUpdate.addNote(note);

        model.setPerson(personToUpdate, updatedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                updatedPerson.getName().fullName,
                updatedPerson.getPhone().value,
                note.value));
    }

    /**
     * Finds the person to update based on the provided identification.
     */
    private Person findPerson(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.isPresent()) {
            if (targetIndex.get().getZeroBased() >= lastShownList.size()) {
                throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            return lastShownList.get(targetIndex.get().getZeroBased());
        } else if (name.isPresent() && phone.isPresent()) {
            return lastShownList.stream()
                    .filter(person -> person.getName().equals(name.get())
                            && person.getPhone().equals(phone.get()))
                    .findFirst()
                    .orElseThrow(() -> new CommandException(MESSAGE_PERSON_NOT_FOUND));
        } else {
            throw new CommandException(MESSAGE_MISSING_PATIENT_REFERENCE);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof NoteCommand)) {
            return false;
        }

        NoteCommand otherNoteCommand = (NoteCommand) other;
        return targetIndex.equals(otherNoteCommand.targetIndex)
                && name.equals(otherNoteCommand.name)
                && phone.equals(otherNoteCommand.phone)
                && note.equals(otherNoteCommand.note);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("name", name)
                .add("phone", phone)
                .add("note", note)
                .toString();
    }
}
