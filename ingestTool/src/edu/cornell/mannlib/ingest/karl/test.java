package edu.cornell.mannlib.ingest.karl;

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

import java.util.*;
import java.io.*;


/**
 * compact all remarks into string, separating by a space
 * remove all line-breaks
 * get rid of quotation marks
 * convert forms of prerequisites into "PRE_REQ"
 * convert forms of corequisites into "CO_REQ"
 * convert words "W/" => WITH
 *  " & " => " AND "
 *  " @ " => " AT "
 *  " W/O " => " WITHOUT "
 *  "LECT:" => "LECTURE:"
 *  "LECT " => "LECTURE "
 * convert known periodized strings into token "*!*" (ex. "M. ARCH II" => "M*!* ARCH II")
 * break up string into sections by periods and remove periods
 * rebuild periodized strings "*!*" => "."
 *
 * periodized strings:
 * M.ARCH
 * M. ARCH
 * B.ARCH
 * M ARCH should have a period!
 * ART 481.04     DEPT #.# is periodized (sometimes 1st # doesn't exist, space is optional)
 *
 * forms of "PREREQUISITES"
 * PREREQUISITES
 * PRE-REQUISITE
 * PRE-REQ
 * PREREQ
 * PRE REQ'S
 * PRE-REG
 *  => convert all to PRE_REQ
 *  PERMISSION OF INSTRUCTOR REQUIRED. => PRE_REQ: PERMISSION OF INSTRUCTOR.
 *  ARCHITECTURE GRADUATE STUDENTS ONLY. => PRE_REQ: GRADUATE STUDENTS.
 *  LIMITED TO BFA STUDENTS ONLY. => PRE_REQ: BFA STUDENTS.
 *  LIMITED TO GRADUATE STUDENTS. => PRE_REQ: GRADUATE STUDENTS.
 *  PERMISSION OF INSTRUCTOR IS REQUIRED. => PRE_REQ: PERMISSION OF INSTRUCTOR
 *  REQUIRES WRITTEN PERMISSION OF INSTRUCTOR => PRE_REQ: PERMISSION OF INSTRUCTOR
 *  1ST YEAR MFA STUDENTS ONLY. => PRE_REQ: 1ST YEAR MFA STUDENTS.
 *  THIS COURSE IS LIMITED TO MFA STUDENTS. => PRE_REQ: MFA STUDENTS.
 *  PERMISSION ONLY => PRE_REQ: PERMISSION OF INSTRUCTOR
 *  PERMISSION OF INSTRUCTOR REQUIRED. => PRE_REQ: PERMISSION OF INSTRUCTOR.
 *  PERMISSION OF PROFESSOR REQUIRED. => PRE_REQ: PERMISSION OF INSTRUCTOR.
 *  PERM
 *  PERM. OF PROF
 *  PERM OF INSTRUC
 *  PERMISSION OF INSTRUCTOR IS REQUIRED.
 *  OPEN TO CRP STUDENTS ONLY => PRE_REQ: CRP STUDENTS.
 *  OPEN TO * ONLY => PRE_REQ: *.
 *  RESTRICTED TO MPS/REAL ESTATE STUDENTS. => PRE_REQ: MPS/REAL ESTATE STUDENTS.
 *  PREREQUISITE: COMPLETION OF CRP 492. => PRE_REQ: CRP 492.
 *  HISTORY OF ART MAJORS ONLY. => PRE_REQ: HISTORY OF ART MAJORS.
 *  NOT OPEN TO FRESHMEN OR SOPHOMORES. => PRE_REQ: NOT FRESHMEN AND NOT SOPHOMORES
 *
 *  NOT prereq:
 *  S/U OPTION IS OPEN TO OUT OF DEPARTMENT STUDENTS ONLY.
 *
 * CONCURRENT
 *
 * [HISTORY OF ART MAJORS ONLY. NOT OPEN TO FRESHMEN OR, SOPHOMORES W/O PERMISSION OF INSTRUCTOR., CLASS MEETS IN JOHNSON MUSEUM STUDY GALLERY.]
 * [REQUIRES WRITTEN PERMISSION OF INSTRUCTOR ON APPROVED, INDEPENDENT STUDY FORM.]
 * [PREREQUISITES: ART 161,261 & 263; 264/265/361 (1 OF 3).]
 * [PREREQ: ART 131/132/133 (2 OF 3);231/232/233 (1 OF 3); 331.]
 * [PREREQUISITES: ART 161, 171, AND ONE OF THE FOLLOWING:, ART 131/132/133/134 OR PERMISSION OF INSTRUCTOR. CLASS MEETS, IN 221D/TJ.]
 * [PREREQUISITES: ONE OF THE FOLLOWING: ART 131, 133, 161, 171,, 251 OR PERMISSION OF INSTRUCTOR.  COURSE FEE $95.]
 * [PRE-REG: ARCH 262, ARCH 263, ARCH 264, ARCH 363 OR, EQUIVALENT COURSES IN STRUCTURE AND TECHNOLOGY OR, PERM. INSTRUCTOR.]}
 * PREREQUISITE: PERMISSION OF INSTRUCTOR.
 *               PERMISSION OF GRADUATE COMMITTEE CHAIR.
 * PREREQ:
 * RECOMMENDED: ANSC 100, 150
 * PREREQ: AN SC 250
 *         AN SC 250 OR PERMISSION OF INSTRUCTOR.
 * PREREQUISITES: AN SC 300 OR PERMISSION OF INSTRUCTOR
 * PREREQUISITES: ENGRD 202 & ENGINEERING MATH SEQUENCE
 * PREREQ: MATH 293 AND FLUID MECHANICS (CO-REGISTRATION    PERMISSIBLE)
 * PREREQUISITES: BIOCHEMISTRY OR PERMISSION OF INSTRUCTOR.
 * FLUIDS OR A HYDROLOGY COURSE; MATH 191
 * PRE-REQUISITE: DIFFERENTIAL EQUATIONS, 2 SEMESTERS OF    PHYSICS, INTRO BIOLOGY, AND STATISTICS.
 * COMPUTER PROGRAMMING, 1 YEAR OF CALCULUS.
 * CO-REGISTRATION BEE 473 IN FALL OR BEE 450 IN SPRING.
 * PRE REQ'S:  1 YEAR CHEMISTRY, 1 YEAR MATH (KNOWLEDGE OF  CALCULUS) 1 YEAR PHYSICS OR PERMISSION OF INSTRUCTOR
 * CO-REGISTRATION IN BEE 435, BEE 473, BEE 478 OR BEE 481  REQUIRED.
 * VETMI 315
 * BIO G 105
 * BIO G 105.
 * PREREQUISITES: REQUIRED FOR ALL AEM MAJORS.
 * VTBMS 346    S/U GRADES BY PERMISSION.  EVENING PRELIMS. JAMES LAW AUDITORIUM.
 * X-LIST ANSC 427
 * CONCURRENT REGISTRATION IN BIOBM 330 OR WRITTEN  PERMISSION OF INSTRUCTOR REQUIRED.
 * PREREQUISITES: BIOBM 330, 333, OR 331 AND 332 OR WRITTEN PERMISSION OF INSTRUCTOR.
 * PREREQS: BIOGD 281. RECOMMENDED: BIOMI 290 & BIOBM 330 OR    331 AND 332 OR 333.
 * COREQUISITE: CONCURRENT ENROLLMENT IN BIOEE 154.
 * CONCURRENT OR PREVIOUS ENROLLMENT IN BIOEE 261.  REQUIRED WEEKEND FIELD TRIP.
 * PREREQUISITES: 200-LEVEL BIOLOGY COURSE.
 * 1 YEAR INTRODUCTORY BIOLOGY PLUS BIOGD 281 OR BIOBM 330 OR   333 OR 331/332 OR PERMISSION OF INSTRUCTOR.
 * PREREQUISITES: BIOBM 330, BIOBM 331 - 332, BIOBM 333 &   GENETICS, OR PERMISSION OF INSTRUCTOR.
 * PREREQ:VTMED560
 * FALL. 3 CREDITS. PREREQUISITE:  BIOPL 241 OR EQUIVALENT. TWO, ALL-DAY FIELD TRIPS.  A.G. TAYLOR, GENEVA EXPERIMENT STATION (ITHACA CONTACT, R.L. ODENDORF), GRADE OPTION: LETTER]}
 * [FALL. 4 CREDITS. PREREQUISITE:  CSS 260, S/U OR LETTER GRADE.  H.M. VAN ES]}
 * [FALL. 1 CREDIT. PREREQUISITE: PERMISSION OF INSTRUCTOR., S/U GRADE ONLY.  T.L. SETTER]}
 * [PREREQUISITES: PLANT PHYSIOLOGY]}
 * [PREREQUISITES: PL PA 301, ENTOM 241 OR CONSENT OF INSTRUCTOR]}
 * [PREREQS: AN SC 250 OR EQUIVALENT OR PERMISSION OF, INSTRUCTOR.  OFFERED FALL SEMESTERS ONLY.]
 * [PREREQS: BIOMI 290 AND 291.]
 * [PREREQS: FD SC 321]
 * [PREREQ: HIGH SCHOOL BIOLOGY.]
 * [LA491, PREREQUISITES: MAJOR IN PLANT SCIENCES OR LANDSCAPE, ARCHITECTURE OR PERMISSION OF INSTRUCTOR., LIMITED TO 48 STUDENTS., PRE-REGISTRATION REQUIRED.]
 * [PREREQS: LIMITED TO GRADUATE LEVEL STUDENTS OR WITH, PERMISSION OF INSTRUCTOR.]
 */

public class test
{

    //frs.put( "(ANSC) (\\d+)((?:, ?(?:OR |AND )?\\1 (?:\\d+))*),? ?(OR|AND)? ?(\\d+)",
    //         "$1 $2$3,$4 $1 $5" ); // format lists of course numbers
    public static class CoursesIngest extends TabFileIngestDocument
    {
        /// All of the course department codes
        protected static final String COURSE_DEPARTMENTS[] =
            { "A&EP", "AAP", "AAS", "AEM", "AGSCI", "AIR S", "AIS", "ALS", "AM ST", "AN SC",
              "ANTHR", "ARCH", "ARKEO", "ART", "ART H", "AS&RC", "ASIAN", "ASTRO", "B&SOC", "BEE",
              "BENGL", "BIO G", "BIOAP", "BIOBM", "BIOEE", "BIOGD", "BIOMI", "BIONB", "BIOPL",
              "BIOSM", "BME", "BTRY", "BURM", "CAPS", "CEE", "CHEM", "CHEME", "CHIN", "CHLIT",
              "CIS", "CLASS", "COGST", "COLLS", "COM L", "COMM", "CRP", "CS", "CSS", "CZECH",
              "D SOC", "DANCE", "DEA", "DUTCH", "EAS", "ECE", "ECON", "EDUC", "ENGL", "ENGLB",
              "ENGLF", "ENGRC", "ENGRD", "ENGRG", "ENGRI", "ENTOM", "FD SC", "FGSS", "FILM",
              "FREN", "FSAD", "GERST", "GOVT", "GRAD", "GREEK", "H ADM", "HD", "HE", "HINDI",
              "HIST", "HORT", "HUNGR", "IARD", "ILRCB", "ILRHR", "ILRIC", "ILRID", "ILRLE",
              "ILROB", "ILRST", "IM", "INDO", "INFO", "ITAL", "JAPAN", "JPLIT", "JWST", "KHMER",
              "KOREA", "KRLIT", "LA", "LANAR", "LAT A", "LATIN", "LAW", "LING", "LSP", "M&AE",
              "MATH", "MEDVL", "MIL S", "MS&E", "MUSIC", "NAV S", "NBA", "NBAB", "NBAE", "NCC",
              "NCCB", "NCCE", "NEPAL", "NES", "NMI", "NRE", "NS", "NS&E", "NTRES", "OR&IE",
              "OVST", "P ED", "PALI", "PAM", "PHIL", "PHYS", "PL BR", "PL PA", "POLSH", "PORT",
              "PSYCH", "QUECH", "RELST", "ROM S", "RUSSA", "RUSSL", "S HUM", "S&TS", "SANSK",
              "SEBCR", "SINHA", "SNES", "SOC", "SPAN", "STSCI", "SWED", "SYSEN", "T&AM", "TAG",
              "TAMIL", "THAI", "THETR", "TOX", "UKRAN", "URDU", "VETCS", "VETMI", "VETMM", "VIET",
              "VISST", "VTBMS", "VTMED", "VTPMD", "WRIT" };


        protected TreeMap<Integer,String> myCourseList;
        protected TreeMap<Integer,ArrayList<Integer>> myCrossListings;
        protected TreeSet<String> myDepartments;

        public CoursesIngest( IngestEntityProcessor processor )
        {
            super( processor );
            myCourseList = new TreeMap<Integer,String>();
            myCrossListings = new TreeMap<Integer,ArrayList<Integer>>();
            myDepartments = new TreeSet<String>();
        }

        class RemarksFormatter
        {
            protected String myRemarks;

            public RemarksFormatter( ArrayList<String> remarks )
            {
                // Build a remarks string
                myRemarks = "";
                ArrayList<String> remarksArray = remarks;
                for( Iterator<String> r = remarksArray.iterator(); r.hasNext(); )
                    myRemarks = myRemarks + r.next() + (r.hasNext() ? " " : "");
            }

            public void format()
            {
                if( myRemarks.isEmpty() ) return;

                myRemarks = clean( myRemarks );
                myRemarks = fixTypos( myRemarks );
                myRemarks = replaceAbbreviations( myRemarks );
                myRemarks = exchangePhrases( myRemarks );
            }

            /**
             * Erases unusual characters and other things that would mess up our processing
             */
            public String clean( String remarks )
            {
                remarks = remarks.toUpperCase();
                remarks = remarks.replaceAll( "\"", "" );
                remarks = remarks.replaceAll( "\\:", ": " );
                remarks = remarks.replaceAll( " +", " " );
                return remarks;
            }

            /**
             * Fix mistyped entries and other weird things
             */
            public String fixTypos( String remarks )
            {
                remarks = remarks.replaceAll( "PERM PERMISSION", "PERMISSION" );
                remarks = remarks.replaceAll( "ADVISER", "ADVISOR" );
                remarks = remarks.replaceAll( "OFINSTRUCTOR", "OF INSTRUCTOR" ); // arch303
                remarks = remarks.replaceAll( "PERIMISION", "PERMISSION" ); // biosm378
                return remarks;
            }

            /**
             * Expand all the abbreviations
             */
            public String replaceAbbreviations( String remarks )
            {
                remarks = remarks.replaceAll( " & ", " AND " );
                remarks = remarks.replaceAll( " @ ", " AT " );
                remarks = remarks.replaceAll( " W/O ", " WITHOUT " );
                remarks = remarks.replaceAll( " W/OUT ", " WITHOUT " );
                remarks = remarks.replaceAll( " W/ ", " WITH " );
                remarks = remarks.replaceAll( "VET\\.", "VETERINARY" );
                remarks = remarks.replaceAll( "VET STUDENT", "VETERINARY STUDENT" );
                remarks = remarks.replaceAll( "AVAIL ", "AVAILABLE " );
                remarks = remarks.replaceAll( "PERM(?:\\.| ) OF", "PERMISSION OF" );
                remarks = remarks.replaceAll( "INSTRUC(?:\\.| )", "INSTRUCTOR" );
                remarks = remarks.replaceAll( "PROF(?:\\.| )", "PROFESSOR" );
                remarks = remarks.replaceAll( "IND(?:\\.| ) STUDY", "INDEPENDENT STUDY" );
                remarks = remarks.replaceAll( "EVRY(?:\\.| )", "EVERY" );
                // look at music 345
                // 9/7/07 - BIONB392 is not caught completely by this =>  {{PREREQUISITES}}S:
                remarks = remarks.replaceAll( "PRE(?: |-)?(?:(?:REQUISITE(?:(?:\\(S\\))|S)?)|(?:REG)|(?:REQ(?:\\'S)?)|Q)", "{{PREREQUISITES}}" );

                return remarks;
            }

            /**
             * Replace common phrases with phrases that are easier to decode
             */
            public String exchangePhrases( String remarks )
            {
                remarks = remarks.replaceAll( "REQUIRED OF AND LIMITED TO", "LIMITED TO" );
                remarks = remarks.replaceAll( "YOU MUST FILL OUT INDEPENDENT STUDY FORM IN ORDER TO ENROLL(?:\\.)?", "{{INDEPENDENT STUDY FORM}}" );

                // Handle the "{{EXPLICIT INSTRUCTOR PERMISSION}}" tag
                if( remarks.matches( "(?:PERMISSION )?PERMISSION(?: REQUIRED)?(?:\\.)?$" ) )
                    remarks = "{{EXPLICIT INSTRUCTOR PERMISSION}}";
                else
                {
                    // Reduce phrase complexity in stages
                    remarks = remarks.replaceAll( "(?:REQUIRE(?:S|D) )?WRITTEN PERMISSION", "PERMISSION" ); // biobm334, art 429
                    remarks = remarks.replaceAll( "ARE PERMITTED BY INSTRUCTOR", "AND PERMISSION OF INSTRUCTOR" ); // arch 458
                    remarks = remarks.replaceAll( "PERMISSION OF (?:(?:INSTRUCTOR)|(?:PROFESSOR))", "INSTRUCTOR PERMISSION" );
                    remarks = remarks.replaceAll( "PERMISSION ON APPROVED INDEPENDENT STUDY FORM.", "PERMISSION" ); // art 429
                    remarks = remarks.replaceAll( "PERMISSION (?:(?:IS )|(?:ARE ))?REQUIRED", "PERMISSION" ); // arch 458, biobm334
                    remarks = remarks.replaceAll( "(?:ENROLLMENT )?BY INSTRUCTOR PERMISSION", "INSTRUCTOR PERMISSION" );
                    remarks = remarks.replaceAll( "WITH PRIOR PERMISSION WRITTEN OF THE INSTRUCTOR", "INSTRUCTOR PERMISSION" );
                    remarks = remarks.replaceAll( "(?:\\))?INSTRUCTOR PERMISSION(?:(?: ONLY(?:\\.| )?)| |(?:\\.)|(?:\\))|$)", "{{EXPLICIT INSTRUCTOR PERMISSION}}" );


                    // not caught:
                    //ARCH349:  PREMISSION REQUIRED AND APPROVED INDEPENDENT STUDY FORM.
                    //ARCH349:  PERMISSION AND APPROVED INDEPENDENT STUDY FORM.
                    // ART263:  {{PREREQUISITES}}: ART 161 OR ARCH 251 OR {{EXPLICIT INSTRUCTOR PERMISSION}}IS REQUIRED. -> should be fixed
                    // ART429:  REQUIRED {{EXPLICIT INSTRUCTOR PERMISSION}} -> should be fixed
                    // CRP457:  INSTRUCTOR PERMISSION{{EXPLICIT INSTRUCTOR PERMISSION}}
                    //CRP493:  {{PREREQUISITES}}: COMPLETION OF CRP 492. PERMISSION OF INSTRUCTION REQUIRED.
                    // CRP497:  PERM OF INSTRUCTOR{{EXPLICIT INSTRUCTOR PERMISSION}}
                    //CRP497:  {{EXPLICIT INSTRUCTOR PERMISSION}}IS REQUIRED.
                    // CRP797:  PERM OF INSTRUCTOR{{EXPLICIT INSTRUCTOR PERMISSION}} LIMITED TO GRADUATE STUDENTS.
                    //ASTRO440:  {{EXPLICIT INSTRUCTOR PERMISSION}}NEEDED; INDEPENDENT STUDY FORM REQUIRED FOR REGISTRATION.
                    // LEFT OFF HERE!!!!

                    // more processing: bionb720, bioee764
                    // CRP649:  UNDERGRADUATES {{EXPLICIT INSTRUCTOR PERMISSION}}
                }


                /*
MUSIC439:  SUCCESSFUL AUDITION
MUSIC441:  AUDITION REQUIRED
MUSIC423:  PREREQUISITE: SUCCESSFUL AUDITION
MUSIC323:  BY AUDITION
MUSIC335:  AUDITION REQUIRED PLUS SECTION TBA
                 *
                 * */

                return remarks;
            }

            public String toString()
            {
                return myRemarks;
            }
        }

        public void handleEntity( IngestEntity entity )
        {/*
            String dept = entity.getProperty( "Course Dept" ).get(0);
            String num = entity.getProperty( "Course Num" ).get(0);

            RemarksFormatter remarksFormatter = new RemarksFormatter( entity.getProperty( "Remarks" ) );
            remarksFormatter.format();
            String remarks = remarksFormatter.toString();
            if( !remarks.isEmpty() )
                System.out.println( dept + num + ":  " + remarks );*/

            process( entity );

            /*
            // Build cross-listings and the department set
            String cid = entity.getProperty( "Cid" ).get(0);
            String dept = entity.getProperty( "Course Dept" ).get(0);
            String num = entity.getProperty( "Course Num" ).get(0);
            Integer cidInt = Integer.valueOf( cid );
            myCourseList.put( cidInt, dept + num );
            myDepartments.add( dept );

            // Format the remarks for this entity
            RemarksFormatter remarksFormatter = new RemarksFormatter( entity.getProperty( "Remarks" ) );
            remarksFormatter.format();
            String remarks = remarksFormatter.toString();
            if( !remarks.isEmpty() )
                System.out.println( dept + num + ":  " + remarks );

            // Add the cross-listings
            ArrayList<Integer> values = new ArrayList<Integer>();
            for( Iterator<String> i = entity.getProperty( "Xlist" ).iterator(); i.hasNext(); )
            {
                values.add( Integer.valueOf( i.next() ) );
            }

            // Put these into the cross-list
            ArrayList<Integer> currentValues = myCrossListings.get( cidInt );

            // Make sure the list exists
            if( currentValues == null )
            {
                // Create a list
                currentValues = new ArrayList<Integer>();

                // Put it into the map
                myCrossListings.put( cidInt, currentValues );
            }

            // Add this value to the current list
            currentValues.addAll( values );*/

        }

        /**
         * Displays all of the crosslistings for courses
         */
        public void printCrosslisting()
        {
            Iterator<Map.Entry<Integer,ArrayList<Integer>>> i = myCrossListings.entrySet().iterator();
            while( i.hasNext() )
            {
                Map.Entry<Integer,ArrayList<Integer>> entry = i.next();
                ArrayList<Integer> listings = entry.getValue();
                if( listings.isEmpty() ) continue;
                String list = "";
                for( Iterator<Integer> listIter = listings.iterator(); listIter.hasNext(); )
                {
                    Integer r = listIter.next();
                    if( listIter.hasNext() )
                        list = list + myCourseList.get( r ) + ", ";
                    else
                        list = list + myCourseList.get( r );
                }
                System.out.println( "" + myCourseList.get( entry.getKey() ) + " is cross-listed with " + list );
            }

        }

        public void printDepartments()
        {
            String departments = "{ ";
            for( Iterator<String> i = myDepartments.iterator(); i.hasNext(); )
            {
                String department = i.next();
                if( i.hasNext() )
                    departments = departments + "\"" + department + "\", ";
                else
                    departments = departments + "\"" + department + "\" }";
            }

            System.out.println( departments );
        }

        public void ingest( InputStream data ) throws Exception
        {
            // Get the data stream
            open( data );

            // Build the primary action list for top-level objects
            HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
            actions.put( "row", new ParseRow() );

            // Read the document
            parse( actions );
        }


        class ParseRow implements IngestAction
        {
            public void perform( IngestDocument document ) throws Exception
            {

/*
                // tool:   http://www.fileformat.info/tool/regex.htm
                FormatReplaceStrings frs = new FormatReplaceStrings();
                frs.put( "\"", "" ); // erase quotation marks
                frs.put( "PRE(?:\\S| )*REQ(?:\\S| |')*:", "PRE_REQ:" ); // convert forms of prerequisites
                frs.put( "PRE-REG", "PRE_REQ" ); // pre-reg isn't caught by last regex
                frs.put( " & ", " AND " );
                frs.put( " @ ", " AT " );
                frs.put( " W/O ", " WITHOUT " );
                frs.put( " W/ ", " WITH " );
                frs.put( "VET\\.", "VETERINARY" );
                frs.put( "VET STUDENT", "VETERINARY STUDENT" );
                frs.put( "(?:THIS COURSE IS )?(?:LIMITED|RESTRICTED)? TO (.+) STUDENTS(?: ONLY)?\\.?",
                         "PRE_REQ: $1 STUDENT." );
                *
                * H ADM
                *
                * BIOBM 330, 333, OR 331 AND 332
*/
                // Build a list of actions
                HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
                IngestEntity entity = new IngestEntity( "Course Information" );

                FormatString formatter = new FormatString();
                actions.put( "Course College", new ParseField( "Course College", entity, formatter ) );
                actions.put( "Course Dept", new ParseField( "Course Dept", entity, formatter ) );
                actions.put( "Course Num", new ParseField( "Course Num", entity, formatter ) );
                actions.put( "Course Sect Type", new ParseField( "Course Sect Type", entity, formatter ) );
                actions.put( "Course Sect Num", new ParseField( "Course Sect Num", entity, formatter ) );
                actions.put( "Subcourse Sect Type", new ParseField( "Subcourse Sect Type", entity, formatter ) );
                actions.put( "Subcourse Sect Num", new ParseField( "Subcourse Sect Num", entity, formatter ) );
                actions.put( "Year", new ParseField( "Year", entity, formatter ) );
                actions.put( "Term", new ParseField( "Term", entity, formatter ) );
                actions.put( "Cid", new ParseField( "Cid", entity, formatter ) );
                actions.put( "College", new ParseField( "College", entity, formatter ) );
                actions.put( "Dept", new ParseField( "Dept", entity, formatter ) );
                actions.put( "Num", new ParseField( "Num", entity, formatter ) );
                actions.put( "Sect Type", new ParseField( "Sect Type", entity, formatter ) );
                actions.put( "Sect Num", new ParseField( "Sect Num", entity, formatter ) );
                actions.put( "Course Title", new ParseField( "Course Title", entity, formatter ) );
                actions.put( "Parent Cid", new ParseField( "Parent Cid", entity, formatter ) );
                actions.put( "Credit Hours", new ParseField( "Credit Hours", entity, formatter ) );
                actions.put( "Variable Hours Flag", new ParseField( "Variable Hours Flag", entity, formatter ) );
                actions.put( "Grade Opt", new ParseField( "Grade Opt", entity, formatter ) );
                actions.put( "Beg Time1", new ParseField( "Beg Time1", entity, formatter ) );
                actions.put( "End Time1", new ParseField( "End Time1", entity, formatter ) );
                actions.put( "Day1", new ParseField( "Day1", entity, formatter ) );
                actions.put( "Build1", new ParseField( "Build1", entity, formatter ) );
                actions.put( "Room1", new ParseField( "Room1", entity, formatter ) );
                actions.put( "Beg Time2", new ParseField( "Beg Time2", entity, formatter ) );
                actions.put( "End Time2", new ParseField( "End Time2", entity, formatter ) );
                actions.put( "Day2", new ParseField( "Day2", entity, formatter ) );
                actions.put( "Build2", new ParseField( "Build2", entity, formatter ) );
                actions.put( "Room2", new ParseField( "Room2", entity, formatter ) );
                actions.put( "Beg Time3", new ParseField( "Beg Time3", entity, formatter ) );
                actions.put( "End Time3", new ParseField( "End Time3", entity, formatter ) );
                actions.put( "Day3", new ParseField( "Day3", entity, formatter ) );
                actions.put( "Build3", new ParseField( "Build3", entity, formatter ) );
                actions.put( "Room3", new ParseField( "Room3", entity, formatter ) );
                actions.put( "Beg Time4", new ParseField( "Beg Time4", entity, formatter ) );
                actions.put( "End Time4", new ParseField( "End Time4", entity, formatter ) );
                actions.put( "Day4", new ParseField( "Day4", entity, formatter ) );
                actions.put( "Build4", new ParseField( "Build4", entity, formatter ) );
                actions.put( "Room4", new ParseField( "Room4", entity, formatter ) );
                actions.put( "Beg Time5", new ParseField( "Beg Time5", entity, formatter ) );
                actions.put( "End Time5", new ParseField( "End Time5", entity, formatter ) );
                actions.put( "Day5", new ParseField( "Day5", entity, formatter ) );
                actions.put( "Build5", new ParseField( "Build5", entity, formatter ) );
                actions.put( "Room5", new ParseField( "Room5", entity, formatter ) );
                actions.put( "Beg Time6", new ParseField( "Beg Time6", entity, formatter ) );
                actions.put( "End Time6", new ParseField( "End Time6", entity, formatter ) );
                actions.put( "Day6", new ParseField( "Day6", entity, formatter ) );
                actions.put( "Build6", new ParseField( "Build6", entity, formatter ) );
                actions.put( "Room6", new ParseField( "Room6", entity, formatter ) );
                actions.put( "Public Time", new ParseField( "Public Time", entity, formatter ) );
                actions.put( "Public Building", new ParseField( "Public Building", entity, formatter ) );
                actions.put( "Public Room", new ParseField( "Public Room", entity, formatter ) );
                actions.put( "Instructor Ssn", new ParseField( "Instructor Ssn", entity, formatter ) );
                actions.put( "Instructor Name", new ParseField( "Instructor Name", entity, formatter ) );
                actions.put( "Total Enrolled", new ParseField( "Total Enrolled", entity, formatter ) );
                actions.put( "Coll1", new ParseField( "Coll1", entity, formatter ) );
                actions.put( "Enroll1", new ParseField( "Enroll1", entity, formatter ) );
                actions.put( "Coll2", new ParseField( "Coll2", entity, formatter ) );
                actions.put( "Enroll2", new ParseField( "Enroll2", entity, formatter ) );
                actions.put( "Coll3", new ParseField( "Coll3", entity, formatter ) );
                actions.put( "Enroll3", new ParseField( "Enroll3", entity, formatter ) );
                actions.put( "Coll4", new ParseField( "Coll4", entity, formatter ) );
                actions.put( "Enroll4", new ParseField( "Enroll4", entity, formatter ) );
                actions.put( "Coll5", new ParseField( "Coll5", entity, formatter ) );
                actions.put( "Enroll5", new ParseField( "Enroll5", entity, formatter ) );
                actions.put( "Coll6", new ParseField( "Coll6", entity, formatter ) );
                actions.put( "Enroll6", new ParseField( "Enroll6", entity, formatter ) );
                actions.put( "Coll7", new ParseField( "Coll7", entity, formatter ) );
                actions.put( "Enroll7", new ParseField( "Enroll7", entity, formatter ) );
                actions.put( "Coll8", new ParseField( "Coll8", entity, formatter ) );
                actions.put( "Enroll8", new ParseField( "Enroll8", entity, formatter ) );
                actions.put( "Coll9", new ParseField( "Coll9", entity, formatter ) );
                actions.put( "Enroll9", new ParseField( "Enroll9", entity, formatter ) );
                actions.put( "Coll10", new ParseField( "Coll10", entity, formatter ) );
                actions.put( "Enroll10", new ParseField( "Enroll10", entity, formatter ) );
                actions.put( "Coll11", new ParseField( "Coll11", entity, formatter ) );
                actions.put( "Enroll11", new ParseField( "Enroll11", entity, formatter ) );
                actions.put( "Coll12", new ParseField( "Coll12", entity, formatter ) );
                actions.put( "Enroll12", new ParseField( "Enroll12", entity, formatter ) );
                actions.put( "Coll13", new ParseField( "Coll13", entity, formatter ) );
                actions.put( "Enroll13", new ParseField( "Enroll13", entity, formatter ) );
                actions.put( "Coll14", new ParseField( "Coll14", entity, formatter ) );
                actions.put( "Enroll14", new ParseField( "Enroll14", entity, formatter ) );
                actions.put( "Coll15", new ParseField( "Coll15", entity, formatter ) );
                actions.put( "Enroll15", new ParseField( "Enroll15", entity, formatter ) );
                actions.put( "Coll16", new ParseField( "Coll16", entity, formatter ) );
                actions.put( "Enroll16", new ParseField( "Enroll16", entity, formatter ) );
                actions.put( "Coll17", new ParseField( "Coll17", entity, formatter ) );
                actions.put( "Enroll17", new ParseField( "Enroll17", entity, formatter ) );
                actions.put( "Coll18", new ParseField( "Coll18", entity, formatter ) );
                actions.put( "Enroll18", new ParseField( "Enroll18", entity, formatter ) );
                actions.put( "Coll19", new ParseField( "Coll19", entity, formatter ) );
                actions.put( "Enroll19", new ParseField( "Enroll19", entity, formatter ) );
                actions.put( "Coll20", new ParseField( "Coll20", entity, formatter ) );
                actions.put( "Enroll20", new ParseField( "Enroll20", entity, formatter ) );
                actions.put( "Coll21", new ParseField( "Coll21", entity, formatter ) );
                actions.put( "Enroll21", new ParseField( "Enroll21", entity, formatter ) );
                actions.put( "Coll22", new ParseField( "Coll22", entity, formatter ) );
                actions.put( "Enroll22", new ParseField( "Enroll22", entity, formatter ) );
                actions.put( "Coll23", new ParseField( "Coll23", entity, formatter ) );
                actions.put( "Enroll23", new ParseField( "Enroll23", entity, formatter ) );
                actions.put( "Coll24", new ParseField( "Coll24", entity, formatter ) );
                actions.put( "Enroll24", new ParseField( "Enroll24", entity, formatter ) );
                actions.put( "Coll25", new ParseField( "Coll25", entity, formatter ) );
                actions.put( "Enroll25", new ParseField( "Enroll25", entity, formatter ) );
                actions.put( "Final Exam", new ParseField( "Final Exam", entity, formatter ) );
                actions.put( "Alias", new ParseField( "Alias", entity, formatter ) );
                actions.put( "Grade Sheet Code", new ParseField( "Grade Sheet Code", entity, formatter ) );
                actions.put( "Remarks1", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Remarks2", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Remarks3", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Remarks4", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Remarks5", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Remarks6", new ParseField( "Remarks", entity, formatter ) );
                actions.put( "Section Status", new ParseField( "Section Status", entity, formatter ) );
                actions.put( "Xlist1", new ParseField( "Xlist", entity, formatter ) );
                actions.put( "Xlist2", new ParseField( "Xlist", entity, formatter ) );
                actions.put( "Xlist3", new ParseField( "Xlist", entity, formatter ) );
                actions.put( "Xlist4", new ParseField( "Xlist", entity, formatter ) );
                actions.put( "Xlist5", new ParseField( "Xlist", entity, formatter ) );
                actions.put( "Special Program", new ParseField( "Special Program", entity, formatter ) );
                actions.put( "State Date", new ParseField( "State Date", entity, formatter ) );
                actions.put( "End Date", new ParseField( "End Date", entity, formatter ) );
                actions.put( "Filler", new ParseField( "Filler", entity, formatter ) );
                actions.put( "Roster Print Override", new ParseField( "Roster Print Override", entity, formatter ) );
                actions.put( "Max Allowed", new ParseField( "Max Allowed", entity, formatter ) );
                actions.put( "Xlist Profile", new ParseField( "Xlist Profile", entity, formatter ) );
                actions.put( "Room Assignment Required", new ParseField( "Room Assignment Required", entity, formatter ) );
                actions.put( "Requested Partition", new ParseField( "Requested Partition", entity, formatter ) );
                actions.put( "Requested Characteristic", new ParseField( "Requested Characteristic", entity, formatter ) );
                actions.put( "College Allowed", new ParseField( "College Allowed", entity, formatter ) );
                actions.put( "Enroll Limit", new ParseField( "Enroll Limit", entity, formatter ) );
                actions.put( "Capacity Check", new ParseField( "Capacity Check", entity, formatter ) );
                actions.put( "Cross Enroll Limit", new ParseField( "Cross Enroll Limit", entity, formatter ) );
                actions.put( "Grand Capacity Check", new ParseField( "Grand Capacity Check", entity, formatter ) );
                actions.put( "College Capacity Check", new ParseField( "College Capacity Check", entity, formatter ) );
                actions.put( "Enrollment Open", new ParseField( "Enrollment Open", entity, formatter ) );
                actions.put( "Loaddatetime", new ParseField( "Loaddatetime", entity, formatter ) );


                // Parse the person's attributes
                document.parse( actions );

                // We made a new object!
                if( entity.hasProperties() )
                    ((CoursesIngest)document).handleEntity( entity );
            }
        }

        class FormatString extends IngestFormatter
        {
            public ArrayList<String> format( String input )
            {
                // Get rid of whitespace around the input
                String inputTrimmed = input.trim();

                // Only return a result if there was something in the input string
                if( inputTrimmed.length() > 0 )
                    return listOf( input.trim() );
                else
                    return emptyList();
            }
        }

        class FormatReplaceStrings extends IngestFormatter
        {
            private class Pair
            {
                String myKey;
                String myValue;

                public Pair( String key, String value )
                {
                    myKey = key;
                    myValue = value;
                }

                public String getKey() { return myKey; }
                public String getValue() { return myValue; }
            }

            private ArrayList<Pair> myMap = new ArrayList<Pair>();

            void put( String regexKey, String regexValue )
            {
                myMap.add( new Pair( regexKey, regexValue ) );
            }

            /**
             * Takes an input string and converts all entries found explicitly in the map
             * to their corresponding tokens and returns the input string with those
             * changes.
             */
            public ArrayList<String> format( String input )
            {
                String result = new String( input.trim() );

                // If there isn't anything to format, don't return any output
                if( result.length() == 0 )
                    return emptyList();


                // Iterate through each entry and reaplce strings
                for( Iterator<Pair> i = myMap.iterator(); i.hasNext(); )
                {
                    Pair entry = i.next();
                    String lastResult;
                    // Replace each entry continually from the start
                    do
                    {
                        lastResult = result;
                        result = result.replaceAll( entry.getKey(), entry.getValue() );
                    } while( !result.equals( lastResult ) );
                }

                // Return the resulting formatted string
                return listOf( result );
            }
        }
    }

    /**
     * @param args
     */
    public static void main( String[] args ) throws Exception
    {/*
        // Open up the requested stream
        InputStream stream = new BufferedInputStream( new FileInputStream( "C:\\Karl\\ingest src data\\2006Reporting.mod.xml" ) );
        test.CalsImpact impact = new test.CalsImpact();
        impact.ingest( stream );*/

        // Open up the requested stream
        InputStream stream = new BufferedInputStream( new FileInputStream( "C:\\Karl\\ingest src data\\VIVO FA07 Course.txt" ) );
        test.CoursesIngest impact = new test.CoursesIngest( null );
        impact.ingest( stream );
        //impact.printCrosslisting();
        //impact.printDepartments();
    }

}







































/*
public static class CalsImpact extends XMLFileIngestDocument
{
    public void handleEntity( IngestEntity entity )
    {

        System.out.println( entity.toString() + "\n\n" );
    }

    public void ingest( InputStream data ) throws Exception
    {
        // Get the data stream
        open( data );

        // Build the primary action list for top-level objects
        HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
        actions.put( "Person", new ParsePerson() );

        // Read the document
        parse( actions );
    }

    class ParsePerson implements IngestAction
    {
        public void perform( IngestDocument document ) throws Exception
        {
            // Build a list of actions
            HashMap<String,IngestAction> actions = new HashMap<String,IngestAction>();
            IngestEntity entity = new IngestEntity( "impact statement" );

            // Parse the net ID
            actions.put( "Title", new ParseField( "title", entity, new IngestFormatter.Title() ) );
            actions.put( "NetId", new ParseField( "net_id", entity, new FormatNetID() ) );
            actions.put( "USDA_Topic_Areas", new ParseField( "usda_topic_areas", entity, new FormatUSDATopicAreas() ) );

            // Parse the person's attributes
            document.parse( actions );

            // We made a new object!
            ((CalsImpact)document).handleEntity( entity );
        }
    }

    class FormatNetID extends IngestFormatter
    {
        public ArrayList<String> format( String input )
        {
            return listOf( input.trim() );
        }
    }


    class FormatUSDATopicAreas extends IngestFormatter
    {
        public ArrayList<String> format( String input )
        {
            return tokenize( input, "," );
        }
    }
}*/
