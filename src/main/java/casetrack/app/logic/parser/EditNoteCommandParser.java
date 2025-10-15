package casetrack.app.logic.parser;

import static casetrack.app.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static casetrack.app.logic.parser.CliSyntax.PREFIX_NOTE_TEXT;

import casetrack.app.commons.core.index.Index;
import casetrack.app.logic.commands.EditNoteCommand;
import casetrack.app.logic.parser.exceptions.ParseException;
import casetrack.app.model.person.Note;

/**
 * Parses input arguments and creates a new EditNoteCommand object.
 * Format: edit note <PERSON_INDEX> <NOTE_INDEX> t/<NOTE>
 */
public class EditNoteCommandParser implements Parser<EditNoteCommand> {

    @Override
    public EditNoteCommand parse(String args) throws ParseException {
        String trimmed = args.trim();

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(trimmed, PREFIX_NOTE_TEXT);

        if (!argMultimap.getValue(PREFIX_NOTE_TEXT).isPresent()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditNoteCommand.MESSAGE_USAGE));
        }

        String preamble = argMultimap.getPreamble();
        String[] parts = preamble.trim().split("\\s+");
        if (parts.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditNoteCommand.MESSAGE_USAGE));
        }

        Index personIndex = ParserUtil.parseIndex(parts[0]);
        Index noteIndex = ParserUtil.parseIndex(parts[1]);
        Note note = ParserUtil.parseNote(argMultimap.getValue(PREFIX_NOTE_TEXT).get());

        return new EditNoteCommand(personIndex, noteIndex, note);
    }
}


