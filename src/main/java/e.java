/*
 * e - Menu/text list item.
 *
 * One line of a scrolling text list: text, a selection id (-2 marks a header
 * line) and the number of rows it occupies.
 *
 * Naming: this is reconstructed, output-exact Java source (not recovered original source).
 * Member names are descriptive source names; config/d-preobf.map maps every one of them
 * back to its retail (obfuscated) runtime name. Renames are always group-complete (all
 * members sharing a retail name), which reproduces ProGuard's retail constant-pool
 * ordering; see cleanup/docs/NAMING_SCHEME.md. Library overrides keep their API names,
 * classes keep their retail names, and reconstruction devices keep their opus* names.
 */
public final class e {
    /** Line text. */
    public String text;
    /** Selection id (-2 = header line, -1 = plain text). */
    public int id;
    /** Number of display rows this item occupies. */
    public int rows;

    public e(Object text, int id, int rows, int index) {
        this.text = (String)text;
        this.id = id;
        this.rows = rows;
    }
}
