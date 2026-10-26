package com.ppidemo;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Posted transaction. It never changes; a mistake is fixed by a reversal.
 */
public interface Txn {

    /**
     * Unique number.
     * @return Number
     */
    long id();

    /**
     * What it did.
     * @return Operation
     * @throws IOException If fails
     */
    Operation operation() throws IOException;

    /**
     * Free text.
     * @return Memo
     * @throws IOException If fails
     */
    String memo() throws IOException;

    /**
     * When it was posted.
     * @return Time
     * @throws IOException If fails
     */
    OffsetDateTime created() throws IOException;

    /**
     * Journal lines, in the order posted.
     * @return Lines
     * @throws IOException If fails
     */
    List<Line> lines() throws IOException;

    /**
     * Total moved: the sum of the debits.
     * @return Amount, in paise
     * @throws IOException If fails
     */
    long amount() throws IOException;

    /**
     * The transaction this one undoes.
     * @return Empty list, or one number
     * @throws IOException If fails
     */
    List<Long> reverses() throws IOException;

    /**
     * The transaction that undid this one.
     * @return Empty list, or one number
     * @throws IOException If fails
     */
    List<Long> reversedBy() throws IOException;
}
