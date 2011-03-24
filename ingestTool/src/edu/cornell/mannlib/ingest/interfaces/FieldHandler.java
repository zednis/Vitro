package edu.cornell.mannlib.ingest.interfaces;

/*
Copyright © 2003-2008 by the Cornell University and the Cornell
Research Foundation, Inc.  All Rights Reserved.

Permission to use, copy, modify and distribute any part of VITRO
("WORK") and its associated copyrights for educational, research and
non-profit purposes, without fee, and without a written agreement is
hereby granted, provided that the above copyright notice, this
paragraph and the following three paragraphs appear in all copies.

Those desiring to incorporate WORK into commercial products or use
WORK and its associated copyrights for commercial purposes should
contact the Cornell Center for Technology Enterprise and
Commercialization at 395 Pine Tree Road, Suite 310, Ithaca, NY 14850;
email:cctecconnect@cornell.edu; Tel: 607-254-4698; FAX: 607-254-5454
for a commercial license.

IN NO EVENT SHALL THE CORNELL RESEARCH FOUNDATION, INC. AND CORNELL
UNIVERSITY BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING
OUT OF THE USE OF WORK AND ITS ASSOCIATED COPYRIGHTS, EVEN IF THE
CORNELL RESEARCH FOUNDATION, INC. AND CORNELL UNIVERSITY MAY HAVE BEEN
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.  */

import org.dom4j.Element;

/**
 * Intended to take a Document and a Entity
 * and set some fields of the Entity to values
 * found in the Document.
 *
 * It could set multiple fields.
 *
 * It might look at a bunch of different xpaths and
 * make add a list ents2ents.
 *
 * @author bdc34
 *
 */
public interface FieldHandler {
    /**
     * Set fields in the Entity based on values in doc.
     * @param doc
     * @param targetEnt
     */
    public void setFields( Element doc, Object targetEnt );

    /**
     * This would be used if you needed to release resources
     * when the parsing ended.  This could be used to close
     * files for instance.
     *
     * In general you can ignore it.
     */
    public void endOfParsing();
}
