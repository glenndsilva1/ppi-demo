package com.ppidemo;

import java.io.IOException;
import java.util.List;

/**
 * A transaction about to be posted: what it is and which lines it has.
 */
public interface Entry {

    /**
     * What it does.
     * @return Operation
     */
    Operation operation();

    /**
     * Free text, e.g. a shop name.
     * @return Memo
     * @throws IOException If fails
     */
    String memo() throws IOException;

    /**
     * Journal lines, debits must equal credits.
     * @return Lines
     * @throws IOException If fails
     */
    List<Line> lines() throws IOException;

    /**
     * The transaction this one undoes.
     * @return Empty list, or one transaction
     */
    List<Txn> reversed();
}
